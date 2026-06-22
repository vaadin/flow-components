# Block: SheetMetadataOperations

> Phase 3 spec. Drafted from the source
> (`SheetMetadataOperations.java`) and the characterization tests
> (`SheetMetadataOperationsTest.java`, 12 tests, slice 74).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Renames sheets on the workbook with validation cascade
(non-empty, unique, valid index) and keeps the visible-sheet-names
mirror in sync.

The visible-names mirror is conditionally updated — only for
sheets that are neither hidden nor very-hidden — preserving the
"hidden sheets don't appear in the visible-names array" contract.

Sibling helpers: [`SheetLifecycleOperations`](SheetLifecycleOperations.md),
[`ActiveSheetSelector`](ActiveSheetSelector.md).

## Inputs

### `setSheetName(int sheetIndex, String sheetName)` (visible-index variant)

- Validates `sheetIndex` against the visible-names array length.
- Translates to POI index and delegates to
  `setSheetNameWithPOIIndex`.

### `setSheetNameWithPOIIndex(int sheetIndex, String sheetName)`

- Validates `sheetIndex` against
  `workbook.getNumberOfSheets()`.
- Validates `sheetName` is non-null and non-empty.
- Validates `sheetName` is unique via
  `spreadsheet.isSheetNameExisting`.
- Calls `workbook.setSheetName(sheetIndex, sheetName)` — fires
  unconditionally, even for hidden / very-hidden sheets.
- **Conditional mirror update**: if the sheet is neither hidden
  nor very-hidden, defensively copies the visible-names array,
  updates the entry at the visible index, and calls
  `spreadsheet.setSheetNames(names)`.

## Outputs

`void`. Side effects on the workbook + on the visible-names
array.

## Side effects

- POI `Workbook.setSheetName` (unconditional).
- Visible-names mirror via defensive `Arrays.copyOf` + setter
  (conditional on visibility).

## Invariants

- **POI rename always fires; visible-names mirror conditionally.**
  A hidden sheet's name still mutates in POI (so reading the
  workbook later reflects the rename); the visible-names array
  doesn't include hidden sheets, so its entry doesn't need
  updating.
- **Defensive copy on the visible-names array.** The setter
  always passes a fresh array, not the original. A regression
  that mutated in place would break the "names array is
  immutable from the consumer's POV" contract.
- **Validation BEFORE mutation.** All four validation branches
  throw before `setSheetName` fires.
- **Uniqueness is workbook-global**, same as
  [`SheetLifecycleOperations`](SheetLifecycleOperations.md)'s
  `createNewSheet`.

## Open questions

- The `setSheetName(visible-index)` variant exists as a thin
  wrapper. A future refactor could fold it into the POI-index
  variant by translating in `Spreadsheet`'s caller. Current
  shape is the result of slice 74's "preserve all the legacy
  call sites" extraction.
- The error message "Sheet Name cannot be null or an empty
  String, or contain backslash \" mentions backslash but the
  current validation doesn't check for backslash. Stale message;
  worth a follow-up to either add the check or trim the message.
