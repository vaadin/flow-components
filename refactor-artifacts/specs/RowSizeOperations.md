# Block: RowSizeOperations

> Phase 3 spec. Drafted from the source (`RowSizeOperations.java`)
> and the characterization tests (`RowSizeOperationsTest.java`, 13
> tests, slice 71). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Set row heights and row visibility on the active sheet. Owns the
same `height == 0` ↔ "row is hidden" entanglement as
[`ColumnSizeOperations`](ColumnSizeOperations.md) (twin helper,
row axis).

## Inputs

### `setRowHeight(int rowIndex, float heightInPoints)`

- Height in points (POI's native unit for rows). `0` is the
  hide sentinel.

### `setRowHidden(int rowIndex, boolean hidden)`

- Toggles. ALWAYS calls POI; conditionally adjusts the 1-based
  `hiddenRowIndexes` list.

### `setDefaultRowHeightInPoints(float height)`

- Updates POI + the Spreadsheet mirror.

## Outputs

`void`. Side effects on POI + the Spreadsheet's row-related
arrays.

## Side effects

- POI `Sheet.setHeightInPoints` / `setZeroHeight`.
- `rowH[r] = height` with defensive copy.
- 1-based membership in `hiddenRowIndexes`.
- `reloadSheetStyles(true, true)` — both args hardcoded `true`
  (different from column ops; row hide affects more state).

## Invariants

- **`setHeight(0)` routes through `applyHidden(true)`** —
  pinned semantic same as column ops.
- **`setHeight(positive)` on previously-hidden row clears
  `setZeroHeight(false)` AND removes from `hiddenRowIndexes`.**
  Both steps are necessary; pinned by slice 71.
- **1-based `hiddenRowIndexes` storage**. A test that put POI
  row 2 (1-based: 3) doesn't unhide an entry "2" (which would
  correspond to POI row 1). Same convention as column ops.
- **`reloadSheetStyles(true, true)` is HARDCODED**. Both
  arguments fire regardless of direction. Column ops uses
  `reloadSheetStyles(true, false)` for some paths;
  the row axis's `true, true` is intentional.
- **`setDefaultRowHeightInPoints` updates the POI sheet AND
  the Spreadsheet `defRowH` field BUT does NOT trigger
  reloadSheetStyles** — the default-height change isn't a
  per-row change, doesn't need restyling.

## Open questions

- The `reloadSheetStyles(true, true)` asymmetry vs column ops
  isn't documented anywhere except the slice 71 test that pins
  it. A code-archaeology pass might surface the original
  motivation; without that, the test is the only thing keeping
  the behavior stable.
