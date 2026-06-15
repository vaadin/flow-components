# Block: ClientCellCache

> Second Phase 3 spec. Dogfoods the template a second time, this time on a
> state-holding block (not a pure-logic block like `SequenceIncrement`).
> Drafted from the source (`ClientCellCache.java`, 140 LOC), the 13 unit tests
> in `ClientCellCacheTest.java`, and the git history of the slice that
> introduced it (slice 13b, journal entry 2026-06-05).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

A server-side mirror of which cells the Spreadsheet has already pushed to the
client. The client maintains its own cache of cell DOM nodes; this block lets
the server avoid re-pushing cells that haven't changed and to drop cache
entries when cells are deleted, hidden, or otherwise no longer relevant.

Two parallel sets — one for plain-value cells, one for formula cells. The
split matters because formula cells get re-pushed every flush (their cached
formula-result may change even if the formula text doesn't), whereas plain
cells only get pushed when their value is marked dirty. Used by
`ClientCacheFlusher` to decide what to include in the next `cellsUpdated`
batch, and by `CellValueManager.loadCellDataForRowAndColumnRange` to skip
cells already covered.

## Inputs

All inputs are cell-key strings in the `SpreadsheetUtil.toKey` format
(`"col<N> row<M>"`) and indexes derived from them. The block has no Spreadsheet
or POI dependency — it's a pure data structure.

### `markNormalSent(String key)` / `markFormulaSent(String key)`

- `key` — cell key string. May not be null (HashSet#add NPEs on null).

### `containsNormal(String key)` / `containsFormula(String key)`

- `key` — cell key string. May not be null. Returns `false` for any
  key not previously marked.

### `dropSent(String key)`

- `key` — cell key string. May not be null. Returns whether the key was
  found and removed.

### `pruneRowsRange(int startRow, int endRow)`

- `startRow`, `endRow` — 1-based row indices, inclusive. The block iterates
  `i` from `startRow` to `endRow` and removes every cached key ending with
  `"row" + i`.

### `pruneColumn(int indexColumn)`

- `indexColumn` — 1-based column index. The block removes every cached key
  starting with `"col" + indexColumn + " r"` (note the trailing space + `r`
  — this is the discipline that distinguishes column 3 from column 30).

### `clear()`

- No arguments. Empties both sets.

## Outputs

### `containsNormal(String key)` / `containsFormula(String key)`

- `boolean` — true iff the key was previously marked in the respective
  set and not yet dropped.

### `dropSent(String key)`

- `boolean` — true if the key was found and removed (from either set);
  false if it wasn't in either set.

### `pruneRowsRange(int startRow, int endRow)`

- `List<CellData>` — one `CellData` per removed key. Each carries the
  matched row index (the loop's `i`) and the column index extracted from
  the cell key. Order: all matching plain-cell keys for row `i` first,
  then all matching formula-cell keys for row `i`, then the next row. The
  list is never null; an empty range or no-match returns `[]`.

### `markNormalSent` / `markFormulaSent` / `pruneColumn` / `clear`

- `void`. Side effects only.

## Side effects

None outside the block's own state. The block mutates its two internal
`HashSet<String>` instances. It does not call into Spreadsheet, POI, the
client, or any other helper. The `pruneRowsRange` return value can be fed
into `ClientFlushQueue.enqueueRemoval` by the caller, but that's the caller's
choice — the block itself doesn't enqueue anything.

## Invariants

- `containsNormal(key)` is true iff `markNormalSent(key)` was called more
  recently than `dropSent(key)` / `clear()` / any prune that matched `key`.
- `containsFormula(key)` likewise for the formula set.
- A key can be in both sets if the caller has marked it via both
  `markNormalSent` and `markFormulaSent` — the sets are not mutually
  exclusive. The deletion idiom `dropSent` short-circuits after the first
  set hit, so the same key in both sets requires two `dropSent` calls to
  fully clear (or one `clear()`).
- After `pruneRowsRange(s, e)`, no key in either set ends with `"row" + i`
  for any `i ∈ [s, e]`.
- After `pruneColumn(c)`, no key in either set starts with `"col" + c + " r"`.
- After `clear()`, both sets are empty.

## Edge cases

- **Key in both sets, `dropSent` called once** → drops from plain set
  only; formula set entry remains. Documented in
  `dropSent_triesPlainSetFirst_thenFormulaSet`.
- **Prune over an empty range** (e.g. `pruneRowsRange(10, 5)`) → no-op,
  returns `[]`. (The loop body never executes.)
- **Prune with no matches** → no-op, returns `[]`.
- **Prune of column 3 vs column 30** → the `"col3 r"` prefix
  intentionally includes the trailing `" r"` to prevent `pruneColumn(3)`
  from also removing `col30` entries. Pinned in
  `pruneColumn_distinguishesColumnByExactPrefix`.
- **Empty cache** → all `contains*` return false, `dropSent` returns
  false, prunes return `[]`, `clear` is a no-op.

## Error modes

- **null `key`** to any method → `NullPointerException` from
  `HashSet.add` / `HashSet.contains` / `HashSet.remove` (HashSet semantics).
  Not swallowed.
- **null `startRow`/`endRow`** — N/A (primitive ints).
- **`Integer.MIN_VALUE` or `MAX_VALUE` row/column index** → no error;
  the string concatenation works for any int.
- **Cache key with malformed format** (e.g. a key not matching the
  `"col<N> row<M>"` shape) — the cache stores any string; only the prune
  operations care about the format. A malformed key would never match a
  prune predicate, so it would persist until `dropSent` or `clear`.

## Non-obvious quirks

- **`dropSent` short-circuits** after the first hit (plain set first).
  This is faithful to the original
  `if (!sentCells.remove(key)) sentFormulaCells.remove(key)` idiom but is
  observable as a different end state when the same key is in both sets.
  Documented; intentional.
- **`pruneRowsRange` builds `CellData` with the row from the loop, not
  from the key.** This is mathematically equivalent because every matched
  key ends with `"row" + i` (so the key's row IS `i`), but the row index
  source is the iteration variable. If the cache contained a malformed key
  that matched the suffix by accident, the CellData would carry the loop
  index, not the key's actual row — but malformed keys shouldn't exist.
- **No interaction with `ClientFlushQueue`.** The cache is a pure data
  structure; the queue is a separate block. Callers that need
  prune-then-enqueue do both calls themselves. This separation is
  deliberate so the cache stays single-purpose.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/ClientCellCache.java`
- Tests: `ClientCellCacheTest.java` (13 tests, slice 21 — pin every behavior in this spec)
- Slice that introduced it: journal entry 2026-06-05, slice 13b (`ClientCellCache + ClientFlushQueue + ClientCacheFlusher`)
- Companion blocks: `ClientFlushQueue` (the pending-changes queue this feeds via prune-return-values), `ClientCacheFlusher` (drains both into the next RPC batch), `CellValueManager` (the original owner, now hosts these as helper fields)

## Contract status

Every invariant maps to a passing test in `ClientCellCacheTest.java`. The
prune-by-row-range pinning of `CellData.row` coming from the loop index is
explicitly covered by
`pruneRowsRange_multipleRows_returnsRemovalsWithCorrectRowAndCol`. The
column-prefix discipline is explicitly covered by
`pruneColumn_distinguishesColumnByExactPrefix`. The short-circuit drop is
explicitly covered by `dropSent_triesPlainSetFirst_thenFormulaSet`.

## What the spec doesn't cover

- Performance characteristics. The block is O(1) for mark / contains /
  drop and O(n) for prune (where n is total cache size). Not relevant for
  the cell-cache use case (caches stay small relative to sheet size
  because only visible-range cells get marked).
- Thread safety. The block is not thread-safe; `HashSet` is not concurrent.
  The Vaadin Flow request-thread model means concurrent access doesn't
  happen in practice.
- Serializable behavior. The block is `Serializable` (the class declares
  it), but serialization edge cases (e.g. restoring after a class
  signature change) aren't covered. Same caveat as the rest of the
  Spreadsheet codebase.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Is the "key can be in both sets" scenario actually expected, or is it a
   programming error that the cache should defend against? Currently it's
   tolerated; the spec calls it "intentional" but the original author may
   disagree.
2. Is the column-prefix `" r"` discipline documented anywhere else, or is
   it implicit knowledge in the cell-key format? If only documented here,
   `SpreadsheetUtil.toKey` should probably get a javadoc reference back to
   this spec.
3. Are there any callers I missed that would surface additional contract
   points? (Phase 1 grep showed only `CellValueManager` + flusher use this
   block, but spec writers should re-grep before approving.)
