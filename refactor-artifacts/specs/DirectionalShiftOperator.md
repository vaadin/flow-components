# Block: DirectionalShiftOperator

> Phase 3 spec. Drafted from the source
> (`DirectionalShiftOperator.java`) and the characterization tests
> (`DirectionalShiftOperatorTest.java`, 14 tests, slice 76).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Move cell values in one of four directions (rows-up, rows-down,
columns-left, columns-right) when the user fills a selection by
dragging the autofill handle. Each method delegates per-cell
copying to `CellValueShifter.shift(...)` (which handles the
cell-type and formula adjustment) and handles the orchestration
around it: auto-creating destination rows/cells, deciding what to
do when the source is null, and clearing the source cell with the
right ordering.

Sibling to `RangeSequenceDetector` (which decides "is this a
sequence?") and `SequenceIncrement` (which computes the step).
This class IS the dispatcher; the sibling classes are pure
predicates / value extractors.

## Inputs

All four directional methods share the same arg shape:

### `shiftRowsDown(int srcStartRow, int srcEndRow, int dstStartRow, int dstEndRow, Sheet sheet)`

- The four indices describe a source row range and a destination
  row range. Both ranges are inclusive, 0-based.
- `sheet` — the active POI sheet.
- Walks each row position in the destination range, finds the
  corresponding source-row position (modular if the destination
  is larger than the source — that's `RangeSequenceDetector`'s
  job to flag in advance, but this class handles the cycle).

### `shiftRowsUp(...)` / `shiftColumnsRight(...)` / `shiftColumnsLeft(...)`

- Same shape, different direction. Row variants iterate rows;
  column variants iterate columns within each affected row.

## Outputs

`void`. Side effects on the sheet's cells, on the value manager's
cache, on the selection manager's painted-range tracking, on the
spreadsheet style factory's pending-style queue, and on the
workbook (cell mutations propagate up).

## Side effects

For each (src, dst) pair, executes via `CellValueShifter.shift(srcCell, dstCell)`:

- If `srcCell` is non-null: shift its value + style to `dstCell`.
  The shifter handles formula adjustment, cell-type switching,
  and value clearing.
- If `srcCell` IS null on a ROW variant: call
  `valueManager.removeCells(...)` for the full destination row
  (because the source row didn't have a cell to copy from, the
  destination row should be cleared).
- If `srcCell` IS null on a COLUMN variant: SILENTLY SKIP the row
  (no removeCells). This is the asymmetry pinned by slice 76.

After the per-cell loop, clears the SOURCE cells:

- Row variants use `clearCell` order: setCellStyle(null) →
  cellStyleUpdated(true) → setCellValue(null) → cellDeleted.
- Column variants use `clearCellReversed` order:
  setCellValue(null) → cellDeleted → setCellStyle(null) →
  cellStyleUpdated(true).

The ordering difference matters for the style cache: the style
factory rebuilds when notified, so style-first-then-value flushes
a different cache state than value-first-then-style. Slice 76
pinned this with an `InOrder` test covering both code paths.

## Invariants

- **Per-cell dispatch goes through `CellValueShifter.shift`.** All
  four directional methods share that one shifter — its
  behavior (formula adjustment, type switching, locale-aware
  formatting) is shared.
- **Null source row vs null source cell are handled
  asymmetrically.** Row variants `removeCells` the destination;
  column variants silently skip. This is the **load-bearing
  asymmetry** preserved from the legacy code — a regression that
  unified them would silently break user expectations on
  partially-empty source ranges.
- **The clear-order asymmetry between row and column variants is
  load-bearing.** Style-first clear (row variants) vs value-first
  clear (column variants) produces observable differences in
  style-cache invalidation. The InOrder test in slice 76 pins
  both code paths; a regression that unified the orderings would
  surface as stale CSS rules on certain shift operations.
- **Auto-create missing destination rows/cells.** The shifter
  expects a non-null destination cell. `getOrCreateRow` /
  `getOrCreateCell` are called inside the loop; the test fixture
  pins both paths (pre-existing row/cell AND newly-created
  row/cell).
- **Sequence-increment detection happens in the CALLER, not
  here.** This class doesn't decide whether to copy literally or
  to increment a sequence — that's
  `RangeSequenceDetector.isSequence(...)` upstream. Slice 76's
  audit-fix tests pin the `MockedStatic<RangeSequenceDetector>`
  call across all four directions.

## Open questions

- The clear-order asymmetry is "load-bearing" by test pin, but
  the ORIGINAL reason for the asymmetry isn't documented anywhere
  the journal could find. A spelunking pass through the legacy
  spreadsheet history (vaadin/spreadsheet repo) might find the
  bug that motivated the difference; without it, the test is the
  only thing keeping the ordering stable.
- `removeCells` on null-source ROW variants is a soft clear (POI
  sets cell value to blank). On null-source COLUMN variants, the
  destination cell keeps whatever it had — which IS the
  legacy-behavior pin, but worth flagging as user-visible
  asymmetry.
