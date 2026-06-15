# Block: ClientCacheFlusher

> Fifth Phase 3 spec. Rounds out the slice-13b trio of helpers
> (`ClientCellCache`, `ClientFlushQueue`, `ClientCacheFlusher`). Drafted from
> `ClientCacheFlusher.java` (123 LOC) and the integration coverage that
> exercises it indirectly (`updateMarkedCellValues` IT tests). No dedicated
> unit tests exist yet — this block is exercised end-to-end only.
> **Status:** initial draft, not yet reviewed by a human operator. **Test
> coverage gap** — recommend a Phase 2 follow-up.

## Purpose

Drains the `ClientFlushQueue` and pushes the resulting cell-data batch to
the client. The third leg of the slice-13b trio: `ClientCellCache` decides
what's already on the client, `ClientFlushQueue` decides what should be
pushed next, and this block actually does the push.

The flush is sheet-wide: every cell in the active sheet is visited and one
of three things happens per cell:

- If conditional formatting applies, the cell is marked for update so its
  conditional-format style index reaches the client.
- If the cell is a formula and either was previously sent or is currently
  marked, its re-evaluated value is captured into the batch (formula cells
  always re-flush because POI may have produced a new value).
- If the cell is non-formula and marked, its value is formatted and
  captured into the batch.

After the visit pass, the queue's `removedCells` are appended to the batch.
Marked / removed queues then clear. If any formula values actually changed,
a `FormulaValueChangeEvent` fires via the parent `CellValueManager`.

## Inputs

### `flush()`

- No arguments. Operates on the four fields captured at construction
  (`spreadsheet`, `parent`, `cache`, `queue`).

### Constructor

- `spreadsheet` — required, non-null. Source of the active sheet, the
  conditional formatter, and the RPC proxy.
- `parent` — the owning `CellValueManager`, used to call
  `createCellDataForCell(cell)` and `fireFormulaValueChangeEvent(set)`.
- `cache` — the `ClientCellCache` this flusher drains alongside the queue.
- `queue` — the `ClientFlushQueue` this flusher drains.

## Outputs

- `flush()` returns `void`. Effects only.
- The flusher itself has no getters; it doesn't expose state.

## Side effects

`flush()` produces these observable side effects, in this order:

1. **Sheet traversal**: every cell in the active sheet is visited via
   `Spreadsheet.getActiveSheet().rowIterator()` / `Row.cellIterator()`.
2. **Conditional-formatting marks**: for each cell with active
   conditional formatting, `queue.markForUpdate(key)` is called. This
   mutates the queue *during* the traversal — subsequent
   `isMarkedForUpdate` checks inside the same flush see the new marks.
3. **Cache mutation**: cells whose values are captured into the batch get
   their keys added to `cache.markFormulaSent(key)` or
   `cache.markNormalSent(key)` so subsequent flushes skip them.
4. **Formula-change event**: if `queue.hasChangedFormulaCells()`, the
   parent's `fireFormulaValueChangeEvent(drained-set)` fires. Drained from
   the queue, replaced with an empty set.
5. **RPC push**: if the batch is non-empty, `spreadsheet.getRpcProxy()
   .cellsUpdated(updatedCellData)` AND `.refreshCellStyles()` fire (both,
   not just one).
6. **Queue clear**: `queue.clear()` at the very end empties marked and
   removed. (`changedFormulaCells` was already drained in step 4.)

## Invariants

- After `flush()`, `queue.isMarkedForUpdate(anything) == false` and
  `queue.getRemovedCells().isEmpty() == true`.
- After `flush()`, `queue.hasChangedFormulaCells() == false`.
- Cells captured into the batch have their keys present in the cache
  (`cache.containsNormal` or `cache.containsFormula`).
- The RPC push happens **at most once** per `flush()` call. If the batch
  ends up empty (no cells marked, no removals, no formula changes), no
  RPC fires at all — pinned by the `if (!updatedCellData.isEmpty())` guard.
- The formula-change event fires **before** the RPC push, not after. This
  ordering matters because event listeners may modify cell state, and
  those modifications should be reflected in the RPC push.

## Edge cases

- **Empty sheet** → traversal visits zero cells, no marks happen,
  removedCells is empty, no RPC fires. Clear still runs but is a no-op.
- **All formula cells with stale cache** → every formula cell's
  re-evaluated value is captured; `cache.markFormulaSent` is called for
  each. Batch may be large.
- **Conditional formatting on every cell** → every cell gets marked for
  update during traversal, even cells with no value change. The batch
  includes them all.
- **Formula cell with null `createCellDataForCell` result** → the flusher
  constructs a minimal `CellData` (col, row, cellStyle) and includes it.
  Pinned in the source comment ("formula cell value has changed to null
  or empty; probably quite rare").
- **`changedFormulaCells` populated but no other changes** → the event
  fires, but the batch is empty unless removedCells is also non-empty;
  no RPC.

## Error modes

- **`spreadsheet.getActiveSheet()` returns null** → NPE on
  `.rowIterator()`. Not swallowed.
- **`createCellDataForCell(cell)` throws** → propagates up; flush aborts.
  The queue's marked / removed are NOT cleared in this case (state stays
  partially mutated by the conditional-formatting marks earlier in the
  pass).
- **RPC proxy throws** → propagates up; clear() doesn't run.
- The flusher has no other failure modes — it's a pure orchestrator.

## Non-obvious quirks

- **Conditional-format marking is in-band with the traversal.** The
  flusher marks cells via `queue.markForUpdate(key)` during the same loop
  that reads `queue.isMarkedForUpdate(key)`. The conditional-format mark
  for cell X is visible to the formula/marked check for cell X within the
  same iteration. This is intentional — CF-only updates get the
  conditional-format style index pushed even when the cell value didn't
  change.
- **Formula cells re-flush eagerly.** Any formula cell with a previously-sent
  cache entry OR a current mark gets re-evaluated and pushed, even when
  the formula text hasn't changed. POI's evaluator may produce a new value
  due to dependent-cell changes; the flusher doesn't track formula
  dependencies, it just re-pushes.
- **Both `cellsUpdated` AND `refreshCellStyles` fire.** Two separate RPC
  calls in sequence, not one. The client handles them as separate
  events.
- **`changedFormulaCells` and the flush queues have separate lifecycles.**
  `drainChangedFormulaCells()` drains-and-replaces; `clear()` zeroes the
  marked/removed sets. Different lifecycles by design (see
  `ClientFlushQueue.md` for the full reasoning).

## Source links

- Source: `ClientCacheFlusher.java`
- Tests: NONE dedicated. Exercised indirectly via
  `CellValueManager.updateMarkedCellValues()` IT tests + golden-master
  payload tests.
- Slice that introduced it: slice 13b (with `ClientCellCache` +
  `ClientFlushQueue`)
- Companion blocks: `ClientCellCache` (decides what's already sent),
  `ClientFlushQueue` (decides what to push next), `CellValueManager`
  (the host, owns all three), `SpreadsheetClientRpc.cellsUpdated` (the
  client-side recipient)

## Contract status

**Tests do not yet pin every invariant in this spec.** This block is
exercised end-to-end via `updateMarkedCellValues` ITs but has no
dedicated unit test. Phase 2 should add a `ClientCacheFlusherTest` with
Mockito + XSSFWorkbook fixture covering at minimum:

- Empty sheet → no RPC fires.
- One marked plain cell → one batch entry, one RPC push, queue clears.
- One formula cell with stale cache → one batch entry, one RPC push.
- Conditional formatting on a non-marked cell → cell gets marked, gets
  pushed.
- `changedFormulaCells` non-empty → event fires before RPC.
- `createCellDataForCell` returns null for formula → fallback CellData
  constructed with col/row/cellStyle.

Once those tests exist, every invariant maps to an assertion.

## What the spec doesn't cover

- Performance characteristics. Sheet traversal is O(populated cells).
  Not relevant for typical sheet sizes.
- Thread safety. Not thread-safe; relies on Vaadin Flow's request-thread
  serialization.
- Serializable behavior. The block is `Serializable`; the four fields
  serialize via their own contracts.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Is the in-band conditional-formatting marking (mid-traversal) really
   intentional, or is it accidental? If a future flush should batch CF
   marks first and then traverse, the contract would change.
2. Is the ordering "fire formula-change event → then RPC push" load-bearing?
   Event listeners that modify cell state during the event would see those
   modifications in the same RPC. A test pinning the ordering should make
   this explicit.
3. The fallback `CellData` constructed when `createCellDataForCell`
   returns null — does the client handle a cellData with just col/row/
   cellStyle (no value) the same as a removal? Or is this a "blank cell"
   marker?
