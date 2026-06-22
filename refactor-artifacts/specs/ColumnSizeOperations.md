# Block: ColumnSizeOperations

> Phase 3 spec. Drafted from the source (`ColumnSizeOperations.java`)
> and the characterization tests (`ColumnSizeOperationsTest.java`,
> 16 tests, slice 72). **Status:** initial draft, not yet reviewed
> by a human operator.

## Purpose

Set column widths and column visibility on the active sheet.
Owns the entanglement between **`width == 0` and "column is
hidden"** — width-zero routes through `applyHidden(true)`; a
positive width un-hides the column AND reloads its cell data.

Cousin to [`RowSizeOperations`](RowSizeOperations.md) (same
shape, row axis).

## Inputs

### `setColumnWidth(int columnIndex, int width)` / `setColumnWidthAtIndex(int, int)`

- Width in pixels. `0` is a sentinel for "hide this column."
- Updates the Spreadsheet's `colW` array (defensive copy) AND
  the POI sheet.

### `setColumnHidden(int columnIndex, boolean hidden)`

- Toggles hidden state. ALWAYS calls
  `setColumnHidden(...)` on POI; conditionally clears
  `hiddenColumnIndexes` membership (the 1-based list).

### `setDefaultColumnWidth(int width)`

- Updates POI's default column width AND the mirror.

## Outputs

`void`. Side effects on the workbook + the Spreadsheet's
column-related arrays + the cell-value-manager cache.

## Side effects

- POI `Sheet.setColumnWidth(...)` (always).
- `colW[col] = newWidth` with defensive `Arrays.copyOf`.
- Membership add / remove in `hiddenColumnIndexes` (1-based).
- For un-hide: `valueManager.clearCacheForColumn(col)` +
  `valueManager.loadCellData(...)` — cells need to repopulate
  client-side.
- `XSSFSheet.getColumnHelper().cleanColumns()` after the POI
  write to drop stale internal POI state.
- `reloadSheetStyles(...)` always.

## Invariants

- **width == 0 routes through applyHidden(true)**. Not just
  "POI's setColumnWidth(0)" — the full hide cascade fires so
  the Spreadsheet's hidden-set, the POI hidden flag, and the
  client all see the column as hidden.
- **Un-hide reloads cell data** but **re-hide does NOT**.
  Asymmetry: going from hidden to visible reveals cells that
  may not yet be in the cell cache; going from visible to
  hidden doesn't need a load (cells were already there). Pinned
  by slice 72.
- **`setColumnHidden(false)` on a column that wasn't actually
  hidden still calls POI but skips the cache invalidation
  branch.** Defensive: if the state matches, no need to invalidate
  the cache, but the POI write fires for consistency.
- **`XSSFSheet.getColumnHelper().cleanColumns()`** is called
  AFTER the POI width write — POI maintains internal state that
  goes stale otherwise.

## Open questions

- The `hiddenColumnIndexes` list uses 1-based indices. POI is
  0-based. This is the Spreadsheet's UI convention (also pinned
  in [`SheetDimensions`](SheetDimensions.md)).
- The XSSF-only `cleanColumns()` call wouldn't run on HSSF
  workbooks (HSSFSheet doesn't have it). The cast is guarded by
  an `instanceof` check.
