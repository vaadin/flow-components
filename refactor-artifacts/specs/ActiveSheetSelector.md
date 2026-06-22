# Block: ActiveSheetSelector

> Phase 3 spec. Drafted from the source (`ActiveSheetSelector.java`)
> and the characterization tests (`ActiveSheetSelectorTest.java`,
> 13 tests, slice 75). **Status:** initial draft, not yet reviewed
> by a human operator.

## Purpose

Translate between the Spreadsheet's **visible-sheet index** (the
1-based-then-minus-one number the UI exposes) and the **POI sheet
index** (0-based, includes hidden / very-hidden sheets), and
activate a sheet on the underlying `Workbook` with the cache
invalidation + reload cascade that has to follow.

## Inputs

### `getActiveSheetIndex()`

- No arguments. Returns `spreadsheet.getSheetIndex() - 1`. The
  Spreadsheet's stored `sheetIndex` is **1-based** for historical
  reasons (the UI exposes it that way); the visible-index
  convention internally is 0-based.

### `getActiveSheetPOIIndex()`

- No arguments. Translates the visible index to the POI index via
  `spreadsheet.getVisibleSheetPOIIndex(...)`.

### `setActiveSheetIndex(int sheetIndex)` (visible-index variant)

- `sheetIndex` — 0-based visible-sheet index.
- Validates: `< 0` or `>= sheetNames.length` throws
  `IllegalArgumentException`.
- Translates to POI index and delegates to `setActiveSheetWithPOIIndex`.

### `setActiveSheetWithPOIIndex(int sheetIndex)` (POI-index variant)

- `sheetIndex` — 0-based POI sheet index.
- Validates: `< 0` or `>= workbook.getNumberOfSheets()` throws
  `IllegalArgumentException`.
- Validates: sheet is NOT hidden / very-hidden — throws
  `IllegalArgumentException` if it is.

## Outputs

- `getActiveSheetIndex()` and `getActiveSheetPOIIndex()` return
  `int`.
- Setters return `void`. Side effects on the workbook + on
  evaluator caches + on the Spreadsheet's reload state.

## Side effects

Both setters, on successful validation, execute this sequence:

1. `workbook.setActiveSheet(poiIndex)`
2. `workbook.setSelectedTab(poiIndex)` — assumption documented in
   the source: the UI doesn't allow multiple sheet selections, so
   active and selected always match.
3. `spreadsheet.getFormulaEvaluator().clearAllCachedResultValues()`
4. `spreadsheet.getConditionalFormattingEvaluator().clearAllCachedValues()`
5. `spreadsheet.reloadActiveSheetData()`
6. `SpreadsheetFactory.reloadSpreadsheetData(spreadsheet, newSheet)`
7. `spreadsheet.reloadActiveSheetStyles()`
8. `spreadsheet.loadPopupButtons()`

## Invariants

- **Validation BEFORE any side effect.** All three validation
  branches throw before reaching `setActiveSheet` — slice 75 tests
  pin this by mocking the validators and asserting NO downstream
  side effects fire when the throw triggers.
- **Both evaluator caches must be cleared.** A regression that
  drops one of the two clear calls would surface as stale formula
  results / conditional-formatting results on sheet change. The
  test that pins this uses `InOrder` to ensure cache clears happen
  BEFORE the reload.
- **`setActiveSheet` and `setSelectedTab` are paired**. The
  comment in the source ("assume since the UI doesn't allow
  multiple sheet selections") is the *justification*; the code is
  the *contract*. If multi-tab selection ever becomes possible,
  this invariant breaks.
- **`reloadActiveSheetData` runs before the static factory
  reload, which runs before `reloadActiveSheetStyles`**. The
  order matters: styles depend on the sheet's loaded data; data
  depends on the factory's sheet-level setup. Pinned via
  `InOrder`.
- **`loadPopupButtons` is the last step** — popup buttons are
  cell-anchored, so they need every other state in place before
  they can be loaded.

## Open questions

- The visible-index → POI-index translation lives on
  `Spreadsheet.getVisibleSheetPOIIndex` (not on this class). A
  future refactor could move the translation here for cohesion.
- The "active sheet == selected tab" assumption is captured in a
  comment; if it ever needs to be lifted, this class is the
  single place to update.
