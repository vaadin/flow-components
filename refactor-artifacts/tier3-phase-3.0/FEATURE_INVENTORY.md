# Tier 3 / Phase 3.0 — Feature inventory (parity bar)

> Drafted 2026-06-22 by slice 117. Cross-references the 13 GWT
> subsystems from slice 116's survey, the 77 IT test classes,
> the 56 Phase 3 specs under `refactor-artifacts/specs/`, and the
> 162 `.xlsx` fixtures under
> `vaadin-spreadsheet-flow-integration-tests/src/main/resources/`.
> **Status:** initial draft. The list is the "you must support all
> of this" parity bar for the new client.

## How to read this

Every feature has:

- **Spec link** — the Phase 3 spec(s) covering the server side.
- **IT(s)** — the existing TestBench integration test class(es).
- **Fixture(s)** — example `.xlsx` files exercising the feature.
- **Acceptance criterion** — what "the new client supports this"
  means concretely. The golden-screenshot diff is the universal
  acceptance gate; the criterion below names what the SPECIFIC
  behavioral check should look for beyond pixel diff.

Group order matches the slice-116 subsystem map. The IT classes
listed are at
`vaadin-spreadsheet-flow-integration-tests/src/test/java/com/vaadin/flow/component/spreadsheet/test/`.

## Subsystem A — Virtual scrolling + viewport

### A.1 Basic scroll (vertical + horizontal)
- **Specs**: `SheetScrollMemory`, `SheetDimensions`
- **ITs**: `ScrollingIT`, `WrongHashesOnScrollIT`
- **Fixtures**: `100_000_rows.xlsx`, `500x200test.xlsx`
- **Acceptance**: scrolling 100K rows is smooth (no white-flash);
  scroll position is preserved across sheet switches.

### A.2 Frozen panes (vertical, horizontal, both)
- **Specs**: `FreezePaneState`
- **ITs**: `FreezePaneIT`, `FreezePaneLocaleIT`,
  `LoadFileWithFrozenPaneScrolledIT`, `MergedCellFrozenIT`,
  `HiddenAndFrozenIT`
- **Fixtures**: `scrolled_frozen.xlsx`, `hidden_and_frozen.xlsx`,
  `randomly_hidden_and_frozen.xlsx`, `more_hidden_than_frozen.xlsx`
- **Acceptance**: borders align pixel-precisely on split lines;
  scroll in main pane doesn't affect frozen panes; frozen panes
  scroll horizontally as a unit with the main if there's an
  unfrozen-row split too.

### A.3 Scrolling with merged cells
- **Specs**: `MergedRegionLookup`, `MergedRegionOperations`
- **ITs**: `ScrollingWithMergedIT`, `MergedCellFrozenIT`
- **Fixtures**: `merged_cells.xlsx`, `mergedA1B2.xlsx`,
  `mergedAY1AZ2.xlsx`
- **Acceptance**: merged region anchored in a frozen pane renders
  correctly when its tail extends into the main viewport.

### A.4 Hidden rows / columns
- **Specs**: `RowSizeOperations`, `ColumnSizeOperations`
- **ITs**: `HideIT`, `HiddenAndFrozenIT`
- **Fixtures**: `Data_On_Hidden_Rows.xlsx`,
  `HiddenColumnChartTest.xlsx`
- **Acceptance**: hidden rows are completely invisible (height 0,
  not "very short"); scrolling skips them; the row-number column
  shows the correct non-sequential indices.

## Subsystem B — Cell rendering

### B.1 Numeric / string / boolean cell display
- **Specs**: `CellNumericFormatter`, `CellDataBuilder`
- **ITs**: `GenericIT`, `ValueHandlerIT`
- **Fixtures**: `Simple Invoice.xlsx`, `Formulas.xlsx`
- **Acceptance**: each cell displays its formatted value
  (locale-aware decimal separator, applied number format,
  applied text alignment).

### B.2 Overflow detection (display ### when value too wide)
- **Specs**: `CellFitCalculator`
- **ITs**: `CellOverflowIT`, `MergedCellOverflowIT`,
  `MergedCellNarrowFirstColumnIT`, `WrongHashesOnScrollIT`
- **Fixtures**: `merged_overflow.xlsx`, `merged_narrow_column.xlsx`,
  `wrong_hashes.xlsx`
- **Acceptance**: when formatted text is wider than the column
  (4px padding), display ###; merged-cell width counts; the
  threshold doesn't flicker on scroll.

### B.3 Number formats (date, percentage, currency, scientific)
- **Specs**: `CellNumericFormatter`, `FormulaFormatter`
- **ITs**: `NumberFormatIT`, `CustomFormatIT`,
  `CustomNumberFormatColorsIT`, `FormulaFieldFormatIT`,
  `FormulaFormatIT`, `GeneralRoundingIT`,
  `GeneralSmallRoundingIT`, `RoundingToIntegerIT`,
  `LeadingQuoteIT`
- **Fixtures**: `number_format.xlsx`, `date_format.xlsx`,
  `date_format_locale.xlsx`, `rounding.xlsx`,
  `negative_general_round.xlsx`, `small_general_round.xlsx`,
  `leading_quotes.xlsx`
- **Acceptance**: every Excel-defined number format produces the
  same display string (locale + Excel format pattern → text).

### B.4 Cell styling (fill, font, alignment, indent)
- **Specs**: `CellBorderStyleBuilder`, `FontStyleComposer`,
  `DefaultStyleBaseline`, `XSSFColorConverter`
- **ITs**: `StyleIT`, `StylingPartsIT`, `XSSFColorConverterIT`
- **Fixtures**: `spreadsheet_styles.xlsx`,
  `StyleSample - Background and Border.xlsx`,
  `StyleSample - Custom Font.xlsx`, `indent.xlsx`,
  `wrap_text_test.xlsx`, `test_theme_colors.xlsx`,
  `colors.xlsx`, `null_color.xlsx`, `wrong_color.xlsx`
- **Acceptance**: each cell-style index maps to the same CSS rules
  the server emits; CSS interpretation by the browser produces
  the same visual result (mod Level-2 pixel-render diffs).

### B.5 Borders (per-side, conditional-formatting overrides)
- **Specs**: `CellBorderStyleBuilder`, `RuleBorderTracker`
- **ITs**: (covered by Style, ConditionalFormatting)
- **Fixtures**: `borders_simple_test.xlsx`,
  `borders_simple_test.xls`, `cell_borders_vertical.xls`,
  `test_borders.xlsx`, `merged_borders.xlsx`
- **Acceptance**: each border renders on the correct cell (Excel's
  "top border belongs to the cell above's bottom" convention is
  preserved); explicitly-set NONE in CF rules overrides previous
  rule's border with the grey default.

### B.6 Conditional formatting
- **Specs**: `ConditionalFormatter`, `RuleEvaluator`,
  `RuleBorderTracker`
- **ITs**: `ConditionalFormatterIT`, `ConditionalFormattingIT`,
  `ConditionalFormattingBasedOnFormulaIT`,
  `ConditionalFormattingBasedOnSharedFormulaIT`,
  `ConditionalFormattingCellValueIsIT`,
  `ConditionalFormattingOnCellDeletionIT`
- **Fixtures**: `ConditionalFormatterSamples.xlsx`,
  `conditionalformater_nobackground.xlsx`,
  `conditional_formatting_cell_is.xlsx`,
  `conditional_formatting_with_Invalid_formula.xlsx`,
  `test_conditional_formatting.xlsx`
- **Acceptance**: each rule type (cell-value-is, formula-based,
  shared-formula) produces the same CSS class as the GWT widget;
  the `stopIfTrue` semantic is honored.

## Subsystem C — Selection

### C.1 Single-cell selection
- **Specs**: `CellSelectionManager` (no spec yet — orchestrator)
- **ITs**: `SelectionIT`, `NavigationIT`,
  `CellArrowKeyNavigationIT`
- **Fixtures**: any
- **Acceptance**: click selects; arrows navigate; selection
  outline is on the right cell; formula bar updates.

### C.2 Range selection (drag, shift-click)
- **ITs**: `SelectionIT`
- **Fixtures**: any
- **Acceptance**: drag-select extends from anchor;
  shift-click expands; 8-div outline geometry matches.

### C.3 Multi-range selection (Ctrl/Cmd-click)
- **ITs**: `SelectionIT`
- **Fixtures**: any
- **Acceptance**: Ctrl+click adds a new range without losing the
  previous; each range has its own outline.

### C.4 Selection over merged cells
- **Specs**: `MergedRegionLookup`, `MergedRegionOperations`
- **ITs**: `MergeIT`, `MergedCellWithCustomEditorIT`,
  `ScrollingWithMergedIT`
- **Fixtures**: `merged_cells.xlsx`, `mergedA1B2.xlsx`
- **Acceptance**: selection cannot split a merged region; the
  range expands to include the full merged region.

### C.5 Row / column header selection
- **ITs**: `RowHeaderDoubleClickIT`, `SelectionIT`
- **Fixtures**: any
- **Acceptance**: clicking a row header selects the full row;
  clicking a column header selects the full column;
  shift-click extends; double-click autofits.

## Subsystem D — Formula bar + editing

### D.1 Formula display + edit
- **Specs**: `FormulaFormatter`, `FormulaValueUnformatter`,
  `InvalidFormulaTracker`
- **ITs**: `FormulaIT`, `FormulaFieldFormatIT`,
  `FormulaFormatIT`, `ValueHandlerIT`
- **Fixtures**: `Formulas.xlsx`, `FormulaValues.xlsx`,
  `unparsed_formula.xlsx`
- **Acceptance**: formulas display in the formula bar; user can
  edit; invalid formula triggers the error indicator.

### D.2 Formula reference highlighting (16-color cycle)
- **ITs**: covered indirectly by `FormulaIT`
- **Fixtures**: `Formulas.xlsx`
- **Acceptance**: typing `=A1+B2` highlights A1 in color 1, B2 in
  color 2; cycling through 16 colors for >16 refs in the same
  formula.

### D.3 Named ranges
- **Specs**: `RangeSequenceDetector`, `SequenceIncrement`,
  `SequenceValueExtractor`
- **ITs**: `NamedRangeIT`
- **Fixtures**: `named_ranges.xlsx`,
  `Named Ranges Chart.xlsx`
- **Acceptance**: named range appears in the dropdown; selecting
  it navigates the viewport + selects the range.

### D.4 Cell deletion (single, range, individual cells)
- **Specs**: `CellDeletionGuard`
- **ITs**: `CellDeletionIT`
- **Fixtures**: any
- **Acceptance**: delete clears value but preserves style;
  custom handler can intercept; selection clear after.

### D.5 Cell shifting (insert / remove rows / columns)
- **Specs**: `CellValueShifter`, `DirectionalShiftOperator`,
  `RowShiftOrchestrator`, `CellRangeNormalizer`
- **ITs**: `CellShiftingIT`, `CellShiftValuesUndoRedoIT`,
  `RowShiftIT`, `RemoveInsertRowIT`
- **Fixtures**: `row_shifting_test.xlsx`,
  `row_shifting_test.xls`, `shifting test.xlsx`,
  `test_remove_row.xlsx`
- **Acceptance**: row insert / remove updates the layout; formula
  references shift correctly; merged regions adjust; undo restores.

### D.6 Undo / redo
- **Specs**: (CommandStack + CommandReplayer — covered by their
  own specs)
- **ITs**: `UndoRedoIT`, `CellShiftValuesUndoRedoIT`
- **Fixtures**: any
- **Acceptance**: Ctrl+Z / Ctrl+Y reliably reverts and reapplies;
  history doesn't drift on sheet changes.

## Subsystem E — Merged cells (covered above in B, C; specific
fixtures already listed)

## Subsystem F — Sheet tabs

### F.1 Sheet switching
- **Specs**: `SheetChangeDispatcher`, `ActiveSheetSelector`
- **ITs**: `SheetTabSheetIT`, `SpreadsheetTabSheetIT`,
  `MultipleSheetIT`
- **Fixtures**: `multiple_sheets.xlsx`,
  `multiple_sheets_protected.xlsx`
- **Acceptance**: clicking a tab activates the sheet;
  scroll position restored from `SheetScrollMemory`.

### F.2 Sheet rename
- **Specs**: `SheetMetadataOperations`
- **ITs**: `SheetTabSheetIT`
- **Fixtures**: any with multiple sheets
- **Acceptance**: double-click tab opens contentEditable; enter
  saves; duplicate / empty names rejected.

### F.3 New sheet creation
- **Specs**: `SheetLifecycleOperations`
- **ITs**: `CreateNewIT`, `NewSpreadsheetEditIT`
- **Fixtures**: any
- **Acceptance**: + button appends a new sheet; it becomes
  active.

### F.4 Sheet visibility (hide / show / very-hidden)
- **Specs**: `SheetVisibilityOperations`
- **ITs**: (no dedicated IT — covered by tab tests)
- **Fixtures**: `multiple_sheets.xlsx`,
  `sheet_protection.xlsx`
- **Acceptance**: hidden sheets don't appear in the tab bar;
  hiding the active sheet shifts to the previous.

## Subsystem G — Grouping (outline)

### G.1 Row / column grouping (collapse / expand)
- **Specs**: `SheetGrouping`
- **ITs**: `GroupingIT`, `UngroupingIT`, `ClosedGroupNumbersIT`
- **Fixtures**: `Grouping.xlsx`, `Groupingtest.xlsx`,
  `large-grouped.xlsx`, `group_image.xlsx`,
  `ungrouping_cellUpdating.xlsx`
- **Acceptance**: +/- icons toggle visibility; level headers
  (1, 2, 3...) collapse / expand all at level N; nested groups
  respect inversed flag.

## Subsystem H — Popup buttons + filter tables

### H.1 Filter table popup
- **Specs**: `PopupButtonRegistry`, `PopupButtonBinding`,
  `PopupButtonRefresher`, `PopupButtonRowShifter`,
  `TableRegistry`, `TableLoader`, `TableLifecycle`
- **ITs**: `PopupButtonIT`, `SheetFilterTableIT`
- **Fixtures**: `data_filters.xlsx`,
  `autofilter_with_active_column.xlsx`,
  `table-subtotals-ranges.xlsx`,
  `table_with_disabled_autofilter.xlsx`,
  `tables_on_multiple_sheets.xlsx`
- **Acceptance**: dropdown button appears in column header;
  filter UI opens correctly positioned; filter applies and
  hides rows.

## Subsystem I — Copy / paste

### I.1 Copy single cell + paste
- **ITs**: `CopyPasteCellsIT`, `CopyPasteCellLayoutSizeFullIT`
- **Fixtures**: any
- **Acceptance**: Ctrl+C copies; Ctrl+V pastes; the contentEditable
  trick produces TSV in clipboard for cross-app paste.

### I.2 Copy range + paste
- **ITs**: `CopyPasteCellsIT`
- **Fixtures**: any
- **Acceptance**: range copy preserves cell layout; paste into a
  destination range applies the values + styles cell-for-cell.

## Subsystem J — Comments

### J.1 Cell comment display
- **Specs**: `CellCommentLoader`, `CellCommentRangeScanner`
- **ITs**: `CommentIT`
- **Fixtures**: any with comments
- **Acceptance**: red triangle in the cell's top-right corner;
  hover (300ms) opens a popup with author + text.

### J.2 Comment edit
- **ITs**: `CommentIT`
- **Fixtures**: any
- **Acceptance**: right-click "Edit comment" opens the edit
  dialog; saving updates the comment.

## Subsystem K — Custom cell editors + components

### K.1 Custom cell editors
- **Specs**: `CustomComponentLoader`, `CustomComponentRangeScanner`,
  `CustomComponentRowSizer`
- **ITs**: `CustomEditorIT`, `CustomComponentIT`,
  `MergedCellWithCustomEditorIT`,
  `LockedCellsWithCustomEditorIT`
- **Fixtures**: `locked_cell_next_to_custom_editor.xlsx`
- **Acceptance**: cell with a registered `SpreadsheetComponentFactory`
  shows the Vaadin component when focused; value updates flow
  bidirectionally; component lifecycle (attach, detach) is honored.

### K.2 Custom cell rendering (always-on Vaadin component)
- **ITs**: `CustomComponentIT`
- **Acceptance**: component is always rendered in the cell
  (not just on focus); row height grows to fit
  `minimumRowHeightForComponents`.

## Subsystem L — Sheet overlays (images, charts, embedded
components)

### L.1 Embedded images
- **Specs**: `SheetOverlayStorage`, `SheetOverlayVisibility`,
  `SheetOverlayInfoBuilder`, `SheetOverlayManager`
- **ITs**: `ImageIT`
- **Fixtures**: `picture sheet.xlsx`, `picture_sheet.xlsx`,
  `image-locked-cell.xlsx`, `group_image.xlsx`
- **Acceptance**: image renders at the correct cell anchor;
  `DONT_MOVE_AND_RESIZE` anchor uses absolute pixel
  positioning.

### L.2 Embedded charts
- **ITs**: `ChartsIT`
- **Fixtures**: 28 chart-themed files
  (`TypeSample - *.xlsx`, `MyCharts*.xlsx`,
  `3DChart.xlsx`, `Bubble.xlsx`, `Scatter.xlsx`, etc.)
- **Acceptance**: chart renders inline with the correct type
  (bar / line / pie / etc.); data updates when cells change.

### L.3 Embedded Vaadin components on the sheet (non-cell)
- **ITs**: `CustomComponentIT`
- **Acceptance**: a Vaadin component placed via
  `setComponentInCell` or sheet-overlay rendering appears
  correctly positioned.

## Subsystem M — Connector / state delegation

### M.1 State reconciliation across reloads
- **Specs**: `ClientReloadFlag`, `ClientCellCache`,
  `ClientCacheFlusher`, `ClientFlushQueue`,
  `CellMarkUpdater`
- **ITs**: `PreserveOnRefreshIT`, `BigExcelFileIT`,
  `GenericIT`, `ReportModeIT`, `SpreadsheetDialogIT`
- **Fixtures**: `100_000_rows.xlsx`,
  `big_report_sample.xlsx`,
  `RG_DH_nav1_noformulas.xlsx`, multiple `Tagetik *.xlsx`
- **Acceptance**: page reload preserves current state;
  big files load without timeouts; modal-dialog spreadsheet
  still works.

### M.2 Initial load + first paint
- **ITs**: `NewSpreadsheetEditIT`, `CreateNewIT`,
  `LoadFileWithFrozenPaneScrolledIT`
- **Fixtures**: many
- **Acceptance**: first paint after upload shows the correct
  active sheet + scroll position; no console errors.

## Other features (not subsystem-scoped)

### Other.1 Sheet protection + cell locking
- **Specs**: (covered by `Spreadsheet` itself — see Step 4
  `@Nullable` work)
- **ITs**: `LockIT`, `ProtectedCellsTBIT`,
  `ResizeProtectedIT`
- **Fixtures**: `sheet_protection.xlsx`,
  `protected_format_columns.xlsx`,
  `protected_sheet_examples.xlsx`,
  `image-locked-cell.xlsx`
- **Acceptance**: locked cells cannot be edited; protected sheets
  enforce locking; password-unprotect works.

### Other.2 Hyperlinks
- **Specs**: `HyperlinkLoader`, `HyperlinkRangeScanner`
- **ITs**: `HyperlinkIT`
- **Fixtures**: `spreadsheet_hyperlinks.xlsx`,
  `hyper_links.xls`
- **Acceptance**: link cells show pointer cursor on hover;
  tooltip on hover shows link target; click opens the link;
  HYPERLINK formula is resolved.

### Other.3 Context menu
- **Specs**: `ContextMenuManager` (no dedicated spec yet — Phase 1
  helper)
- **ITs**: `ContextMenuIT`
- **Fixtures**: any
- **Acceptance**: right-click opens the context menu; actions
  fire the right server-side handler.

### Other.4 Column / row resize (drag)
- **Specs**: `ColumnSizeOperations`, `RowSizeOperations`,
  `ColumnAutofitter`
- **ITs**: `ResizeIT`, `ResizeProtectedIT`, `SizingIT`
- **Fixtures**: any
- **Acceptance**: dragging column / row separator resizes
  smoothly; double-click autofits; protected cells block
  resize.

### Other.5 Report mode (read-only display, no editing)
- **ITs**: `ReportModeIT`
- **Fixtures**: any
- **Acceptance**: `reportStyle = true` hides the formula bar +
  sheet tabs; locked cells stay locked; styling intact.

### Other.6 Locale-aware formatting + RTL
- **Specs**: `CellNumericFormatter`, `FormulaFormatter`
- **ITs**: `FreezePaneLocaleIT`
- **Fixtures**: `date_format_locale.xlsx`
- **Acceptance**: changing the Spreadsheet's locale updates the
  decimal separator, date format, parameter separator in
  formulas.

### Other.7 Performance + large files
- **ITs**: `BigExcelFileIT`
- **Fixtures**: `100_000_rows.xlsx`,
  `500x200test.xlsx`, multiple `Tagetik *.xlsx`,
  `big_report_sample.xlsx`
- **Acceptance**: loading 100K rows completes in under 5 seconds
  on a baseline laptop; scrolling stays at 60 FPS;
  no memory leaks on prolonged use.

### Other.8 Show zero cells (Excel "show zero values" setting)
- **ITs**: `ZeroCellAsBlankIT`
- **Fixtures**: `show-zero-cells.xlsx`
- **Acceptance**: when the spreadsheet's "show zero values"
  flag is off, cells containing 0 display as blank.

## Parity summary

| Subsystem | Feature count | IT coverage | Fixture coverage |
|---|---|---|---|
| A — Virtual scrolling | 4 | 4/4 | 6 files |
| B — Cell rendering | 6 | 6/6 | ~30 files |
| C — Selection | 5 | 5/5 | many |
| D — Formula bar + editing | 6 | 6/6 | ~10 files |
| E — Merged cells | (in A, C) | — | ~10 files |
| F — Sheet tabs | 4 | 3/4 (visibility weak) | 3 files |
| G — Grouping | 1 | 1/1 | 5 files |
| H — Popup buttons | 1 | 2/2 | 5 files |
| I — Copy / paste | 2 | 2/2 | many |
| J — Comments | 2 | 1/1 | (need to verify) |
| K — Custom editors | 2 | 4/4 | 1 file (gap) |
| L — Overlays | 3 | 2/3 | 32 files |
| M — Connector / state | 2 | 5/5 | many |
| Other | 8 | 7/8 (context-menu fixture weak) | many |
| **Total** | **~50 features** | **48/50 = 96%** | **~150 unique fixtures** |

## Gaps identified

1. **Sheet visibility (F.4)** — no dedicated IT for hide / unhide
   / very-hidden state. The current ITs touch it tangentially.
   ➜ ADD `SheetVisibilityIT`.

2. **Custom-editor fixtures (K.1)** — only 1 dedicated fixture
   (`locked_cell_next_to_custom_editor.xlsx`). The IT tests use
   programmatically-built spreadsheets. For golden-screenshot
   tests, we need at least one fixture per custom-editor
   scenario.
   ➜ ADD 3-5 fixtures: combo-box in cell, date-picker in cell,
   text-field with validation.

3. **Embedded-Vaadin-component overlays (L.3)** — no fixture; the
   IT builds it programmatically.
   ➜ ADD 1 fixture with a representative Vaadin component
   overlay.

4. **Context menu (Other.3)** — fixture-agnostic; the IT runs
   against any sheet. Acceptable; no fixture gap.

5. **Performance regression baseline** — no dedicated "this is
   what 'good enough' looks like" benchmark. The ITs verify
   feature behavior, not performance.
   ➜ ADD perf-baseline test that opens `100_000_rows.xlsx` and
   measures (a) time-to-first-paint, (b) scroll-FPS over a
   10-second window, (c) memory after 5 minutes of interaction.

Each gap is a small fixture / test addition during Phase 3.0 —
not a blocker.
