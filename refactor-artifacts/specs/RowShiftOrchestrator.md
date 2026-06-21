# Block: RowShiftOrchestrator

> Phase 3 spec. Drafted from the source (`RowShiftOrchestrator.java`),
> the characterization tests (`RowShiftOrchestratorTest.java`, 16 tests),
> and the slice that introduced it (slice 97, journal entry 2026-06-21).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Orchestrate a POI row-shift on the active sheet and propagate every
server-side bookkeeping operation that POI does not know about:

- cached row heights (`getRowH` / `setRowH`)
- hidden-row indexes (`getHiddenRowIndexes` / `setHiddenRowIndexes`)
- cell styles for cells in the affected range PLUS the border-spill
  rows above and below (`styler.clearCellStyle`,
  `styler.cellStyleUpdated`, `styler.loadCustomBorderStylesToState`)
- formula and conditional-formatting evaluator caches
- value-manager client-cache row range
- selection address (if the selected cell is in the affected range)
- merged regions intersected by removed rows (only n<0)
- popup buttons rooted in shifted rows
- invalid-formula tracker per-cell key mapping
- overlay reload flag (when overlays exist)

Single concern, single home. Pre-extraction this was a ~155-LOC inline
body in `Spreadsheet.shiftRows(int, int, int, boolean, boolean)` plus a
20-LOC `rowsMoved` helper and a 15-LOC `getLastNonBlankRow` scan. The
orchestrator preserves the behavior verbatim and exposes one public
method (`shift`) plus two private support methods.

## Inputs

### `shift(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight)`

- `startRow`, `endRow` — 0-based POI row indices defining the source
  range to shift.
- `n` — signed shift delta. Positive = rows move down; negative = rows
  move up (which overwrites the rows at `[first+n, first]`).
- `copyRowHeight` — if true, the cached row heights for the affected
  range are updated from the post-shift POI state.
- `resetOriginalRowHeight` — if true, also rebuilds the row heights
  (same effect for the rowH-array branch).

## Outputs

`void`. All outputs are side effects on the host Spreadsheet's
collaborators.

## Side effects

The orchestration runs in this order:

1. **POI dispatch + lastNonBlank scan.** `getLastNonBlankRow(sheet)`
   scans bottom-up for the last row containing a non-blank cell; then
   `sheet.shiftRows(startRow, endRow, n, copyRowHeight,
   resetOriginalRowHeight)` performs the actual POI mutation.
2. **Evaluator-cache invalidation.**
   `formulaEvaluator.clearAllCachedResultValues()` AND
   `conditionalFormattingEvaluator.clearAllCachedValues()`.
3. **Client-cache row range.** `valueManager.updateDeletedRowsInClientCache(start+1, end+1)`
   where:
   - `start = n >= 0 ? startRow : max(lastNonBlankRow, startRow)`
   - `end = n >= 0 ? startRow + n - 1 : endRow`
4. **Affected-row range computation.**
   `firstAffectedRow = n >= 0 ? startRow : startRow + n`
   `lastAffectedRow = n >= 0 ? endRow + n : endRow`
5. **Row-height array refresh** (only if `copyRowHeight ||
   resetOriginalRowHeight`). Grows the cached `rowH` array if positive
   `n` extended past its current length; reads each affected row's
   height from POI (or default).
6. **Overlay reload-from-POI marker** — if `sheetOverlayManager.hasOverlays()`,
   calls `markReloadFromPoi()`.
7. **Per-row style cleanup walk** over `[firstAffectedRow - 1,
   lastAffectedRow + 1]` (extra ±1 to cover shifted border edges):
   - `null` row → clear all column styles, drop the row from
     hidden-row indexes, and `updateDeletedRowsInClientCache` for that
     row.
   - Non-null row → toggle hidden-row indexes based on
     `row.getZeroHeight()`. For each cell: if null AND row is at or
     below `lastNonBlankRow + n` (so client may still hold a stale
     value), create the cell and queue for update. If non-null, queue
     for update.
8. **`rowsMoved` post-shift hook** (private helper):
   - Negative `n` only: scan merged regions on the active sheet and
     remove any whose `firstRow` falls in `[first+n, first]` (per the
     SUT's `for (int row = first+n; row <= first; ++row)` loop). POI
     since version 3.15 already removes invalidated regions; this scan
     covers older POI behavior.
   - Always: `popupButtonRowShifter.shift(first, last, n)`.
   - Always: `invalidFormulaTracker.shiftActiveSheetRows(first, last, n)`.
9. **Cell-style updates for queued cells.** For each cell in the
   queued list: `styler.cellStyleUpdated(cell, false)`,
   `markCellAsUpdated(cell, false)`.
10. **Custom border state reload.** `styler.loadCustomBorderStylesToState()`.
11. **Post-shift orchestration sequence.**
    `updateMarkedCells()` → `updateRowAndColumnRangeCellData(viewport)`
    → `updateMergedRegions()` — in that order. Pinned by InOrder.
12. **Selection address refresh** — if the selected cell's row is in
    `[firstAffectedRow, lastAffectedRow]`, call
    `selectionManager.onSheetAddressChanged(address, false)`.
    Otherwise (or null selection), skip.

## Invariants

- **POI before bookkeeping.** The POI `Sheet.shiftRows` call happens
  BEFORE any cache invalidation or row-height/style work, so the
  post-shift POI state is what the bookkeeping reads.
- **Evaluator caches cleared before any reload.** Stale formula
  results cannot survive into the value-manager's
  `updateMarkedCellValues` phase.
- **`firstAffectedRow` differs from `startRow` for negative `n`.**
  `firstAffectedRow = startRow + n` shifts down by `|n|` to cover the
  rows that were OVERWRITTEN. The `rowsMoved` scan range uses
  `firstAffectedRow`, NOT `startRow`. (This was an arithmetic gotcha
  when writing the test — see the test's comment for the merged-region
  case.)
- **`lastNonBlankRow + n` is the cell-creation boundary.** A null cell
  on a non-null row only gets created (and queued for update) when
  its row is at or below the post-shift last-non-blank position —
  cells beyond that boundary are blank and don't need to be pushed.
- **The ±1 border-spill walk.** The style cleanup walks one row above
  `firstAffectedRow` and one row below `lastAffectedRow` because
  shifted borders bleed into adjacent rows; clearing them avoids
  stale border edges on the client.

## Edge cases

- **Positive n on a sheet with overlays** — `markReloadFromPoi` fires.
- **Positive n on a sheet without overlays** — overlay-related branch
  skipped entirely.
- **Negative n with merged region inside the deleted range** — the
  region is removed via `removeMergedRegion(index)`. Pinned by the
  mocked-Sheet test (real POI removes such regions before the SUT
  scan runs).
- **Positive n with merged region inside the destination range** — POI
  handles the shift; the SUT's `if (n < 0)` guard skips the SUT-level
  removal entirely.
- **Selected cell row IS in affected range** —
  `selectionManager.onSheetAddressChanged(address, false)`. The
  `false` flag means "not from text-field input"; the same arg the
  pre-extraction code passed.
- **Selected cell row NOT in affected range** — selection not touched.
- **No selected cell** (`getSelectedCellReference() == null`) —
  selection branch skipped.
- **`copyRowHeight == false && resetOriginalRowHeight == false`** —
  the rowH array is never touched.
- **`copyRowHeight == true && n > 0 && endRow + n + 1 > rowH.length`**
  — the array is grown via `Arrays.copyOf` to `endRow + n + 1`. Pinned
  by `shift_copyRowHeightTrue_growsRowHArrayWhenNeeded`.

## Error modes

- **`null` active sheet** — NPE at `sheet.shiftRows` and downstream.
  Not guarded.
- **Invalid `startRow`/`endRow`** — POI's `Sheet.shiftRows` may throw
  `IllegalArgumentException`. Not caught.
- **Collaborator failures** — propagate.

## Non-obvious quirks

- **The merged-region branch is dead with modern POI.** POI since
  version 3.15 calls `removeMergedRegion` itself for any merged region
  invalidated by `shiftRows`. The SUT's `if (n < 0)` scan is defensive
  code from earlier POI versions. The characterization test specifically
  swaps to a mock `Sheet` to verify this branch — a real-POI fixture
  would not reach it. Worth keeping in case of POI behavior changes;
  worth knowing it's dead in current POI.
- **The order of styler vs valueManager dispatch matters.** The
  `styler.cellStyleUpdated` call MUST precede `markCellAsUpdated`
  because the value manager reads through the styler when serializing
  cells. Pinned indirectly through `loadCustomBorderStylesToState` +
  `updateMarkedCells` sequencing.
- **`updateRowAndColumnRangeCellData` is passed VIEWPORT bounds, not
  the shifted range.** The reload is for the visible cells, which may
  be far away from the actual shift. The SUT's pre-extraction code
  used `viewportBounds.getFirstRow()` etc.; the helper preserves this.
- **The lastNonBlankRow scan is O(sheet.lastRowNum × max-cells-per-row).**
  For sheets with many sparse cells, this can be expensive. Not
  optimized in the SUT; preserved verbatim.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/RowShiftOrchestrator.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/RowShiftOrchestratorTest.java` (16 tests; every contract paragraph here is pinned by at least one — except some defensive branches noted as "dead with modern POI").
- Slice that introduced it: journal entry 2026-06-21, slice 97 (`Phase 1: extract RowShiftOrchestrator`).
- Callers: `Spreadsheet.shiftRows(int, int, int)` (1-arg overload, delegates to 5-arg) and `Spreadsheet.shiftRows(int, int, int, boolean, boolean)` (5-arg, delegates to `rowShiftOrchestrator.shift`).

## Contract status

Every observable behavior in this spec maps to at least one passing
test in `RowShiftOrchestratorTest.java`. The merged-region removal
branch specifically uses a mocked `Sheet` because real POI would
remove the region before the SUT scan runs; this is the documented
limitation of the real-fixture approach for this branch.

## What the spec doesn't cover

- **Performance.** The lastNonBlankRow scan and the ±1 border walk
  can be expensive for large/sparse sheets. Not optimized; preserved
  for behavior fidelity.
- **Concurrency.** Not thread-safe. The shift operation mutates many
  caches in sequence; concurrent calls would race.
- **POI version compatibility.** The merged-region scan is defensive
  code for pre-3.15 POI. Modern POI removes those regions itself, so
  the branch is effectively dead but harmless.

## Human review

**Status: not yet reviewed.** Three open questions:

1. **Is the merged-region scan still load-bearing?** With POI 3.15+,
   the scan should never find anything. Is it safe to delete? Worth
   confirming the project's POI version + checking whether the SUT
   ever runs against pre-3.15 POI in production.
2. **The ±1 border walk's range may be excessive.** For shifts with
   `endRow - startRow` huge, the walk visits many rows. Is the ±1
   spillover a worst-case bound, or could it be optimized?
3. **Selection-update branch only checks `selectedCellReference.getRow()`.**
   If a multi-cell range is selected (painted range), only the
   anchor cell's row is checked. Is this intentional, or should the
   painted range also trigger a refresh? Worth confirming with a
   manual UI test.
