# Block: ClientFlushQueue

> Fourth Phase 3 spec. Companion to `ClientCellCache.md` — both are slice-13b
> helpers used in tandem by `ClientCacheFlusher`. Drafted from
> `ClientFlushQueue.java` + `ClientFlushQueueTest.java` (9 tests) + the
> journal entry for slice 13b (2026-06-05).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

A server-side buffer of cells that need to be pushed (or unpushed) on the
next call to `updateMarkedCells` on the parent `CellValueManager`. Three
independent queues:

- `markedCells` — set of cell-key strings whose values have changed and
  need to be re-sent to the client.
- `removedCells` — set of `CellData` entries for cells that should
  disappear from the client cache.
- `changedFormulaCells` — set of `CellReference` objects for formula
  cells whose re-evaluated value differs from the previously-cached value.
  Drained-and-replaced (not cleared in place) so the caller can iterate
  the snapshot without concurrent-modification risk.

`ClientCellCache` decides what *was* sent; `ClientFlushQueue` decides what
*will* be sent. The flusher pairs them: prune the cache, enqueue the
removals.

## Inputs

### `markForUpdate(String key)`

- `key` — cell key string. May not be null (`HashSet#add` NPEs on null).

### `isMarkedForUpdate(String key)`

- `key` — cell key string. May not be null. Returns `false` for any key
  not previously marked.

### `enqueueRemoval(CellData cd)`

- `cd` — a `com.vaadin.flow.component.spreadsheet.client.CellData`
  instance. Typically carries only `row` and `col` (a "this cell no
  longer exists" marker). May not be null.

### `addChangedFormulaCell(CellReference ref)`

- `ref` — a POI `CellReference`. May not be null.

### `getRemovedCells()`

- No arguments. Returns the live `Set<CellData>` — a snapshot reference,
  not a defensive copy.

### `drainChangedFormulaCells()`

- No arguments. Returns the current `changedFormulaCells` set AND replaces
  the internal field with a fresh empty set.

### `hasChangedFormulaCells()`

- No arguments. Returns whether the current `changedFormulaCells` set is
  non-empty.

### `clear()`

- No arguments. Empties `markedCells` AND `removedCells`. Does **NOT**
  touch `changedFormulaCells` — that field has its own drain-and-replace
  lifecycle.

## Outputs

- Predicates (`isMarkedForUpdate`, `hasChangedFormulaCells`) return
  `boolean`.
- `getRemovedCells` returns `Set<CellData>` — the live internal set.
- `drainChangedFormulaCells` returns `Set<CellReference>` — the prior
  snapshot.
- `markForUpdate`, `enqueueRemoval`, `addChangedFormulaCell`, `clear`
  return `void`.

## Side effects

None outside the block's own three sets. The block does not touch the
client, the cache, or any other helper — it's a pure buffer.

## Invariants

- `isMarkedForUpdate(key)` is true iff `markForUpdate(key)` was called
  since the last `clear()` (and no overriding `clear()` happened since).
- After `clear()`, `isMarkedForUpdate(anything)` is false and
  `getRemovedCells()` is empty — but `changedFormulaCells` survives.
- After `drainChangedFormulaCells()`, the returned snapshot contains the
  prior contents AND `hasChangedFormulaCells()` returns `false`. New
  `addChangedFormulaCell(...)` calls populate the fresh internal set, not
  the returned snapshot.
- `markForUpdate` is idempotent on the same key — HashSet semantics.
- The three queues are independent. Adding to one does not affect the
  others.

## Edge cases

- **Calling `drainChangedFormulaCells()` on an empty queue** → returns
  an empty set; `hasChangedFormulaCells()` remains false; no exception.
- **Calling `clear()` on an empty queue** → no-op.
- **Same `CellData` instance enqueued twice** → HashSet
  semantics: if `CellData.equals/hashCode` treats them as the same
  entry, only one survives; otherwise both. The block doesn't dedup
  beyond that.
- **`markForUpdate(key)` followed by `clear()` followed by
  `isMarkedForUpdate(key)`** → returns `false` (clear drained the set).

## Error modes

- **null argument** to any method → `NullPointerException` from the
  underlying `HashSet`. Not swallowed.
- The block has no IO, no overflow, no validation — there's nothing else
  that can fail.

## Non-obvious quirks

- **`clear()` does NOT touch `changedFormulaCells`.** This is the most
  surprising thing about the block; pinned in
  `clear_drainsMarkedAndRemoved_butLeavesChangedFormulaCells`. The
  reasoning: the formula-change queue has its own drain lifecycle — the
  flusher calls `drainChangedFormulaCells()` to get a snapshot and fire
  an event; clearing it from `clear()` would conflict with that. Treat
  the three queues as having two lifecycles: marked/removed are paired
  (clear together) and changedFormula is independent (drain-replace).
- **`drainChangedFormulaCells` returns the prior reference.** Callers
  that mutate the returned set can — but the block has already moved on
  to a fresh empty set, so the mutation has no effect on subsequent
  `addChangedFormulaCell` calls. Pinned in
  `drainChangedFormulaCells_returnsSnapshotAndResetsToEmpty`.
- **`getRemovedCells()` is NOT a copy.** Callers can iterate or even
  mutate it; the block sees the mutations. Pin not in a test today, but
  documented here.

## Source links

- Source: `ClientFlushQueue.java`
- Tests: `ClientFlushQueueTest.java` (9 tests, slice 21)
- Slice that introduced it: slice 13b (companion to `ClientCellCache`,
  `ClientCacheFlusher`)
- Companion blocks: `ClientCellCache` (decides what's already sent),
  `ClientCacheFlusher` (drains both this queue and the cache into the
  next RPC batch), `CellValueManager` (the host)

## Contract status

Every invariant maps to a passing test in `ClientFlushQueueTest.java`. The
asymmetric `clear()` semantic — drains marked/removed but leaves
changedFormulaCells — is explicitly covered.

## What the spec doesn't cover

- Performance characteristics. O(1) for mark/contains/enqueue, O(set
  size) for drain. Trivially fast at any practical sheet size.
- Thread safety. Not thread-safe; HashSet is not concurrent. Vaadin
  Flow's request-thread model means it doesn't need to be.
- Serializable behavior. The block is `Serializable` (the class declares
  it). The `changedFormulaCells` reassignment in `drainChangedFormulaCells`
  is a normal field write — no serialization implication.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Is the "clear() doesn't touch changedFormulaCells" semantic really
   intentional, or is it a bug that has been preserved by accident? The
   `clear()` method's javadoc says "Drains the marked and removed
   queues" which is accurate but doesn't explain why formula-change is
   excluded.
2. Is `getRemovedCells()` returning a live reference (no defensive copy)
   intentional? If a caller mutates it, behavior is observable but
   undocumented. Worth pinning either way.
3. The companion `ClientCellCache` and `ClientCacheFlusher` specs (the
   former exists; the latter doesn't yet) should cross-reference each
   other in their "Companion blocks" sections.
