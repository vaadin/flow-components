# Block: SheetLifecycleOperations

> Phase 3 spec. Drafted from the source (`SheetLifecycleOperations.java`)
> and the characterization tests (`SheetLifecycleOperationsTest.java`,
> slice 85). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Create and delete sheets on the underlying `Workbook`, with the
cascade that follows: name validation, active-sheet shift on
delete, reload of active-sheet data, and the
sheet-change-event fire.

Sibling helpers: [`ActiveSheetSelector`](ActiveSheetSelector.md),
[`SheetMetadataOperations`](SheetMetadataOperations.md),
[`SheetVisibilityOperations`](SheetVisibilityOperations.md).

## Inputs

### `createNewSheet(String sheetName, int rows, int columns)`

- `sheetName` — optional sheet name. Three validations:
  - Empty string → `IllegalArgumentException`.
  - Length > 31 chars → `IllegalArgumentException`.
  - Duplicate name (via `spreadsheet.isSheetNameExisting`) →
    `IllegalArgumentException`.
- `rows`, `columns` — initial sheet dimensions.
- Records `previousSheet`, calls `SpreadsheetFactory.addNewSheet`,
  fires `spreadsheet.fireSheetChangeEvent(prev, new)`.

### `deleteSheetWithPOIIndex(int poiSheetIndex)`

- Validates: at least 2 visible sheets must exist
  (`getNumberOfVisibleSheets() >= 2`); otherwise throws.
- Removes the sheet from POI.
- **Active-sheet shift logic**: if the removed sheet was at or
  before the current visible index, the workbook's active-sheet
  index needs to be adjusted. The current code has a TODO comment
  noting "POI doesn't seem to shift the active sheet index... this
  no longer seems to be the case." The logic is retained
  defensively.
- Triggers `spreadsheet.reloadActiveSheetData()` ALWAYS, regardless
  of whether the removed sheet was the active one — formulas on
  the active sheet may reference the deleted sheet.

### `deleteSheet(int sheetIndex)` (visible-index variant)

- Validates the visible-index range, translates to POI index, and
  delegates to `deleteSheetWithPOIIndex`.

## Outputs

`void`. Side effects on the workbook + on the Spreadsheet's
listener registrations.

## Side effects

- POI mutations (`addNewSheet`, `removeSheetAt`,
  `setActiveSheet`).
- `spreadsheet.reloadActiveSheetData()` — ALWAYS on delete.
- `spreadsheet.fireSheetChangeEvent(prev, new)` — ONLY on create
  (delete doesn't fire a change event, by design).

## Invariants

- **Validation BEFORE mutation.** All three validation branches
  in `createNewSheet` throw before any workbook write. Same for
  `deleteSheet` / `deleteSheetWithPOIIndex` (visible-count check
  first).
- **Always-reload on delete.** Even when the removed sheet was
  not the active one, the reload runs because formulas can
  cross-reference. The TODO comment names this defensively.
- **31-char sheet-name limit is from Excel**, not from POI.
  POI tolerates longer names but Excel itself doesn't open them.
  Validating server-side preserves the file-format-back-compat
  invariant (an .xlsx file with a 32-char sheet name would fail
  to open in Excel).
- **Sheet-name uniqueness is global to the workbook**, not
  per-visible-set. A hidden sheet's name counts.

## Open questions

- The TODO around POI's active-sheet-shift behavior: the
  defensive `if (removedVisibleIndex <= oldIndex)` branch may be
  dead code on modern POI versions. A future investigation would
  remove it (or pin it with a test if it's still active).
- `createNewSheet` allows a `null` sheetName (POI picks one).
  Validations only fire on non-null names. The
  `spreadsheet.isSheetNameExisting` check would short-circuit
  on null, but the order of the null check matters.
