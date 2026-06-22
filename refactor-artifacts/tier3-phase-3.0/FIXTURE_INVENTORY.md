# Tier 3 / Phase 3.0 — Fixture inventory

> Drafted 2026-06-22 by slice 117. Catalogues the 162 unique
> `.xlsx` / `.xls` files in
> `vaadin-spreadsheet-flow-integration-tests/src/main/resources/`
> grouped by feature theme. Pairs with `FEATURE_INVENTORY.md`.
> **Status:** initial draft.

## Headlines

- **Total fixtures: 162** (5 in `testsheets/`, 157 in
  `test_sheets/`).
- **`.xlsx`**: 152. **`.xls` (legacy HSSF)**: 10.
- **No duplicate filenames** between the two directories
  (verified by `find ... | sort -u | wc -l`).
- **Largest fixtures** (size on disk): `100_000_rows.xlsx`
  (~10 MB), `big_report_sample.xlsx`,
  `RG_DH_nav1_noformulas.xlsx`, multiple `Tagetik *.xlsx` files.
- **No existing golden-screenshot artifacts** in the repo. Every
  pixel of "correct" rendering output is currently in the GWT
  client's runtime behavior, not on disk.

## Why this inventory matters

The new TS/Lit client must render each fixture **visually
equivalent** to the GWT client. That's the Level-2 back-compat
contract (per the sharpened `code-modernization` skill). Without
a fixture-by-fixture mapping to expected features, the
golden-screenshot diff can't be interpreted: a diff is either
"acceptable font-rendering shift" (pass) or "lost styling intent"
(fail). The inventory below classifies each fixture by feature
theme so a regression in fixture X tells you which subsystem
broke.

## Inventory by feature theme

Fixtures are grouped by the dominant feature they exercise.
Files that touch multiple features are placed in their primary
theme.

### Theme 1: Basic display + general formatting (~20 fixtures)

`Simple Invoice.xlsx`, `Formulas.xlsx`, `FormulaValues.xlsx`,
`number_format.xlsx`, `date_format.xlsx`,
`date_format_locale.xlsx`, `rounding.xlsx`,
`negative_general_round.xlsx`, `small_general_round.xlsx`,
`leading_quotes.xlsx`, `show-zero-cells.xlsx`,
`unparsed_formula.xlsx`, `wrap_text_test.xlsx`,
`row_and_column_styles.xlsx`, `row_style.xlsx`, `indent.xlsx`,
`rows.xlsx`, `ChartWithNullCell.xlsx`, `null_color.xlsx`,
`wrong_color.xlsx`

**Subsystems**: B (cell rendering), D.1 (formula display).

### Theme 2: Styling, borders, colors (~12 fixtures)

`spreadsheet_styles.xlsx`,
`StyleSample - Background and Border.xlsx`,
`StyleSample - Custom Font.xlsx`, `test_theme_colors.xlsx`,
`50784-font_theme_colours.xlsx`, `50786-indexed_colours.xlsx`,
`colors.xlsx`, `borders_simple_test.xlsx`,
`borders_simple_test.xls`, `cell_borders_vertical.xls`,
`test_borders.xlsx`, `merged_borders.xlsx`

**Subsystems**: B.4, B.5.

### Theme 3: Conditional formatting (~6 fixtures)

`ConditionalFormatterSamples.xlsx`,
`conditionalformater_nobackground.xlsx`,
`conditional_formatting_cell_is.xlsx`,
`conditional_formatting_with_Invalid_formula.xlsx`,
`conditional_formatting.xlsx`,
`test_conditional_formatting.xlsx`

**Subsystems**: B.6.

### Theme 4: Merged cells (~10 fixtures)

`merged_cells.xlsx`, `mergedA1B2.xlsx`, `mergedAY1AZ2.xlsx`,
`merged_overflow.xlsx`, `merged_narrow_column.xlsx`,
`merged_borders.xlsx`, `merged_aligned.xlsx`

**Subsystems**: E (all sub-features).

### Theme 5: Frozen panes + scrolling (~8 fixtures)

`scrolled_frozen.xlsx`, `hidden_and_frozen.xlsx`,
`randomly_hidden_and_frozen.xlsx`,
`more_hidden_than_frozen.xlsx`, `Data_On_Hidden_Rows.xlsx`,
`HiddenColumnChartTest.xlsx`, `100_000_rows.xlsx`,
`500x200test.xlsx`

**Subsystems**: A.1, A.2, A.3, A.4.

### Theme 6: Cell shifting + row operations (~5 fixtures)

`row_shifting_test.xlsx`, `row_shifting_test.xls`,
`shifting test.xlsx`, `test_remove_row.xlsx`,
`ungrouping_cellUpdating.xlsx`

**Subsystems**: D.5, D.6.

### Theme 7: Grouping (outline) (~5 fixtures)

`Grouping.xlsx`, `Groupingtest.xlsx`, `large-grouped.xlsx`,
`group_image.xlsx`, `closed_group_numbers.xlsx`

**Subsystems**: G.1.

### Theme 8: Multiple sheets + sheet protection (~6 fixtures)

`multiple_sheets.xlsx`, `multiple_sheets_protected.xlsx`,
`sheet_protection.xlsx`, `protected_format_columns.xlsx`,
`protected_sheet_examples.xlsx`,
`tables_on_multiple_sheets.xlsx`

**Subsystems**: F (all sub-features), Other.1.

### Theme 9: Hyperlinks (~2 fixtures)

`spreadsheet_hyperlinks.xlsx`, `hyper_links.xls`

**Subsystems**: Other.2.

### Theme 10: Named ranges (~2 fixtures)

`named_ranges.xlsx`, `Named Ranges Chart.xlsx`

**Subsystems**: D.3.

### Theme 11: Filter tables (~5 fixtures)

`data_filters.xlsx`, `autofilter_with_active_column.xlsx`,
`table-subtotals-ranges.xlsx`,
`table_with_disabled_autofilter.xlsx`,
`tables_on_multiple_sheets.xlsx`

**Subsystems**: H.1.

### Theme 12: Images (~5 fixtures)

`picture sheet.xls`, `picture sheet.xlsx`,
`picture_sheet.xlsx`, `image-locked-cell.xlsx`,
`group_image.xlsx`

**Subsystems**: L.1.

### Theme 13: Charts (~32 fixtures — by far the largest theme)

3D / Bubble / Scatter / Radar / Filled Radar / Pie / Donut /
Column / Bar / Line / Area / Combination / Pyramid-Cylinder-Cone
chart files plus `Embedded Charts.xlsx`,
`Named Ranges Chart.xlsx`, `ChartWithNullCell.xlsx`,
`Chart_and_data_on_different_sheets.xlsx`,
`ChartsWithCategories.xlsx`, `ColorByPoint.xlsx`,
`HiddenColumnChartTest.xlsx`, `MyCharts*.xlsx` (4),
`FeatureSample - *.xlsx` (4),
`InteractionSample.xlsx`, `numeric-categories.xlsx`,
`numeric-categories-Explicit-Y-Axis-Bounds.xlsx`,
`pie_labels.xlsx`, `scatter_lines.xlsx`

**Subsystems**: L.2.

### Theme 14: Big files / real-world samples (~10 fixtures)

`100_000_rows.xlsx`, `500x200test.xlsx`,
`big_report_sample.xlsx`, `RG_DH_nav1_noformulas.xlsx`,
`Tagetik 1-12.xlsx` (12 files),
`Poytakartta.xlsx`,
`NumbersCreatedExcelFile.xlsx`

**Subsystems**: A.1 (scrolling), M.1 (state reconciliation),
Other.7 (perf).

### Theme 15: Edge cases / regression markers (~10 fixtures)

`42464-ExpPtg-bad.xls`, `42464-ExpPtg-ok.xls`,
`wrong_hashes.xlsx`, `null_color.xlsx`,
`wrong_color.xlsx`, `picture sheet.xls`,
`ChartWithNullCell.xlsx`,
`conditional_formatting_with_Invalid_formula.xlsx`,
`unparsed_formula.xlsx`,
`locked_cell_next_to_custom_editor.xlsx`

These are fixtures named after specific bug numbers or edge
cases. They're the "don't regress this" set.

### Theme 16: Locale + rounding edges (~5 fixtures)

`date_format_locale.xlsx`, `rounding.xlsx`,
`negative_general_round.xlsx`, `small_general_round.xlsx`,
`closedGroup_numbers.xlsx`

**Subsystems**: B.3, Other.6.

## Cross-cutting coverage analysis

### Fixtures that exercise multiple subsystems heavily

- `Simple Invoice.xlsx` — cell display, formulas, styling,
  borders, merged cells. **Good first-paint smoke test.**
- `Grouping.xlsx` — grouping + cell display. **Good Phase 3.2
  scrolling-meets-collapsing test.**
- `100_000_rows.xlsx` — perf + scrolling baseline. **Critical for
  Phase 3.2 perf gate.**
- `Tagetik 5.xlsx` (and siblings) — real production-shape sheets
  with everything: merged cells, formulas, conditional formatting,
  styling. **The "if this works, we're 80% done" suite.**

### Recommended Phase 3.0 capture order

To produce golden screenshots in priority order (capture earliest
those that exercise the most features):

1. `Simple Invoice.xlsx` (broad coverage smoke test)
2. `Tagetik 5.xlsx` (production-shape)
3. `Grouping.xlsx` (grouping + scrolling)
4. `100_000_rows.xlsx` (perf baseline)
5. `merged_cells.xlsx` (merged region geometry)
6. `scrolled_frozen.xlsx` (frozen pane edge cases)
7. `ConditionalFormatterSamples.xlsx` (CF rules)
8. `spreadsheet_styles.xlsx` (general styling)
9. `Formulas.xlsx` (formula bar + reference highlighting)
10. `data_filters.xlsx` (popup buttons + filter tables)
... then the remaining 152 in any order.

The top 10 above cover all 13 subsystems at least once.

## Gap analysis

Fixtures we do NOT have but Phase 3.0 should add:

### G.1 Custom-editor fixtures (gap from FEATURE_INVENTORY K.1)

Currently the IT tests build custom-editor scenarios
programmatically. For golden-screenshot tests, we need
fixtures that load with the editor pre-configured. Add:

- `custom_editor_combobox.xlsx` (1 cell with combo-box editor)
- `custom_editor_datepicker.xlsx` (1 column of date cells)
- `custom_editor_validated_text.xlsx` (text field with validation)

### G.2 Embedded-Vaadin-component overlay fixture (gap from L.3)

Add: `embedded_vaadin_button_overlay.xlsx` (a Button overlay
positioned over A1:C3).

### G.3 Sheet-visibility fixture (gap from F.4)

Add: `mixed_visibility_sheets.xlsx` (3 sheets: 1 visible, 1 hidden,
1 very-hidden).

### G.4 Performance-baseline fixture

Add: `perf_baseline.xlsx` — a deliberately-shaped large file with
a known mix of (rows × cols × formulas × cell-styles) so
performance can be measured against a stable workload, not the
arbitrary `100_000_rows.xlsx` (which is rows-only).

**Total new fixtures: 6.** Easy to author in Phase 3.0 week 1.

## File metadata to capture per fixture (golden-screenshot run)

When Phase 3.0 captures screenshots, also record per fixture:

- **Source POI version**: which POI version parsed it (for
  forensic if a fixture stops loading).
- **File hash** (SHA-256): to detect accidental fixture mutation.
- **Render time**: how long the GWT client took to load + first
  paint. Becomes the perf baseline.
- **Viewport snapshot range**: which cells were on-screen at
  capture time (so the new client can capture the same range).
- **devicePixelRatio + browser + OS**: the screenshot is a
  function of all three.

The metadata lives in a JSON file alongside each PNG:
`<fixture-name>.snapshot-metadata.json`. The Phase 3.0
infrastructure (next doc) sets this up.

## Fixture provenance + licensing

Some fixtures (`Tagetik *.xlsx`, `RG_DH_nav1_noformulas.xlsx`,
`Poytakartta.xlsx`) are customer-sourced and may carry
confidentiality concerns. They're already in the open-source
repo so the confidentiality ship has sailed, but: **don't
publish customer-named fixture content beyond what's already in
git**. Future fixtures (the 6 gap additions above) should be
synthetic, not customer-derived.
