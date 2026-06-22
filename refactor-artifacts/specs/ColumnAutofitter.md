# Block: ColumnAutofitter

> Phase 3 spec. Drafted from the source (`ColumnAutofitter.java`)
> and the characterization tests (`ColumnAutofitterTest.java`, 12
> tests, slice 73). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Run POI's `Sheet.autoSizeColumn(...)` on a column and apply the
resulting pixel width to the Spreadsheet. Adds extra padding for
columns that host a `SpreadsheetFilterTable` popup button so the
button doesn't get truncated by the autofit.

Owns the per-Spreadsheet cache of autofitted widths keyed by
absolute `CellReference` (anchored at row 0). The cache is a
`WeakHashMap` so cell references held only by the cache don't
prevent GC.

## Inputs

### `autofitColumn(int columnIndex)`

- 0-based column index.
- Calls `Sheet.autoSizeColumn(columnIndex)`, then reads the new
  `getColumnWidth(...)` and converts pixels.
- Catches `NullPointerException` from POI (known issue on
  certain workbooks) and silently aborts.
- Adds `+16px` if the column hosts a filter-table popup button.
- Writes the new width to the Spreadsheet via the size-change
  command.

### `refreshAutofittedColumnsInTable(SpreadsheetFilterTable table)`

- Re-autofits columns in the table's range that STILL match
  their recorded autofit width (i.e., user hasn't manually
  resized).

## Outputs

`void`. Side effects on the Spreadsheet's `colW` array + the
WeakHashMap cache + the undo-redo command history.

## Side effects

- `Sheet.autoSizeColumn(...)` — POI mutation.
- Cache update with the new autofitted width.
- `SizeChangeCommand` issued for the undo / redo stack.

## Invariants

- **POI NPE silently aborts.** A regression that propagated the
  exception would crash on certain workbooks. Pinned by slice 73.
- **Filter-button padding is 14px button + 2px margin = +16px**.
  Hardcoded; tested with the exact `SpreadsheetUtil.pixel2WidthUnits(88)`
  expectation in slice 73.
- **`refreshAutofittedColumnsInTable` skips user-resized columns.**
  Comparison is against the recorded autofit width; if it
  doesn't match, the user touched it and we don't re-autofit.
- **`WeakHashMap` cache lets cell-reference keys be GC'd**. If
  a sheet is removed, its column references can be collected
  without leaking through this cache.

## Open questions

- The NPE-catch is a known POI bug workaround. A future POI
  version may fix it; the catch could be removed if a corpus
  audit confirms no real workbook still triggers it.
- The `+16px` padding is the filter button's exact pixel width.
  If the button design ever changes, this constant needs to
  follow.
