/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * Builds the {@link CellData} transport object for a single POI cell. Reads
 * through {@link CellValueManager} for the formatter chain
 * ({@code numericFormatter}, {@code fitCalculator}, {@code formulaFormatter},
 * {@code flushQueue}) and through {@link Spreadsheet} for context (locale,
 * locked / hidden checks, invalid-formula marks, conditional-formatting
 * indexes, column widths).
 *
 * <p>
 * One concern, one place. Pre-extraction this was a 148-LOC method inline on
 * {@code CellValueManager} plus an 11-LOC private
 * {@code handleIsDisplayZeroPreference} helper. The orchestrator preserves the
 * behavior verbatim and exposes one package-private entry point
 * ({@link #createCellDataForCell}).
 *
 * <p>
 * Called from two sites in the codebase:
 * {@code CellValueManager.loadCellDataForRowAndColumnRange} (internal) and
 * {@code ClientCacheFlusher.flush} (external, via a delegator on
 * {@code CellValueManager}). The delegator preserves the subclass extension
 * surface — the public {@code createCellDataForCell} on
 * {@code CellValueManager} remains as a {@code protected} delegating wrapper.
 */
final class CellDataBuilder implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(CellDataBuilder.class);

    private static final String ZERO_AS_STRING = "0";

    private final CellValueManager manager;

    CellDataBuilder(CellValueManager manager) {
        this.manager = manager;
    }

    CellData createCellDataForCell(Cell cell) {
        Spreadsheet spreadsheet = manager.spreadsheet;
        CellData cellData = new CellData();
        cellData.row = cell.getRowIndex() + 1;
        cellData.col = cell.getColumnIndex() + 1;
        CellStyle cellStyle = cell.getCellStyle();
        boolean valueOverflows = false;
        cellData.cellStyle = "cs" + cellStyle.getIndex();
        cellData.locked = spreadsheet.isCellLocked(cell.getAddress());
        try {
            if (!spreadsheet.isCellHidden(cell)) {
                if (cell.getCellType() == CellType.FORMULA) {
                    cellData.formulaValue = manager.formulaFormatter
                            .reFormatFormulaValue(cell.getCellFormula(),
                                    spreadsheet.getLocale());
                    try {
                        String oldValue = manager.numericFormatter
                                .getCachedFormulaCellValue(cell);
                        String newValue = manager.numericFormatter
                                .getDataFormatter().formatCellValue(cell,
                                        manager.getFormulaEvaluator(),
                                        manager.getConditionalFormattingEvaluator());
                        if (!newValue.equals(oldValue)) {
                            manager.flushQueue.addChangedFormulaCell(
                                    new CellReference(cell));
                        }
                    } catch (RuntimeException rte) {
                        // Apache POI throws RuntimeExceptions for an invalid
                        // formula from POI model
                        String formulaValue = cell.getCellFormula();
                        cell.setCellValue(formulaValue);
                        spreadsheet.markInvalidFormula(
                                cell.getColumnIndex() + 1,
                                cell.getRowIndex() + 1);
                    }

                }
            }

            DataFormatter df = manager.numericFormatter.getDataFormatter();
            if (df instanceof CustomDataFormatter customDf) {
                String color = customDf.getCellTextColor(cell);
                if (color != null) {
                    cellData.textColor = color;
                }
            }

            if (cell.getCellStyle().getDataFormatString().contains("%")) {
                cellData.isPercentage = true;
            }

            String formattedCellValue = df.formatCellValue(cell,
                    manager.getFormulaEvaluator(),
                    manager.getConditionalFormattingEvaluator());

            if (!spreadsheet.isCellHidden(cell)) {
                if (cell.getCellType() == CellType.FORMULA
                        || cell.getCellType() == CellType.NUMERIC) {
                    formattedCellValue = formattedCellValue
                            .replaceAll("^-(?=0(.0*)?$)", "");
                }
            }
            if (spreadsheet.isMarkedAsInvalidFormula(cellData.col,
                    cellData.row)) {
                // The prefix '=' or '+' should not be included in formula
                // value
                if (cell.getStringCellValue().charAt(0) == '+'
                        || cell.getStringCellValue().charAt(0) == '=') {
                    cellData.formulaValue = cell.getStringCellValue()
                            .substring(1);
                }
                formattedCellValue = "#VALUE!";
            }

            if (formattedCellValue != null && !formattedCellValue.isEmpty()
                    || cellStyle.getIndex() != 0) {
                // if the cell is not wrapping text, and is of type numeric
                // or formula (but not date), calculate if formatted cell
                // value fits the column width and possibly use scientific
                // notation.
                cellData.value = formattedCellValue;
                cellData.needsMeasure = false;
                if (!cellStyle.getWrapText()
                        && (!SpreadsheetUtil.cellContainsDate(cell)
                                && cell.getCellType() == CellType.NUMERIC
                                || cell.getCellType() == CellType.STRING
                                || (cell.getCellType() == CellType.FORMULA
                                        && !cell.getCellFormula()
                                                .startsWith("HYPERLINK")))) {
                    if (!manager.doesValueFit(cell, formattedCellValue)) {
                        if (manager.numericFormatter
                                .valueContainsOnlyNumbers(formattedCellValue)
                                && manager.numericFormatter
                                        .isGeneralCell(cell)) {
                            cellData.value = manager.numericFormatter
                                    .getCellValueFormatter()
                                    .getScientificNotationStringForNumericCell(
                                            cell.getNumericCellValue(),
                                            formattedCellValue,
                                            manager.fitCalculator
                                                    .getRatioForStyleIndex(
                                                            (int) cell
                                                                    .getCellStyle()
                                                                    .getIndex()),
                                            manager.getCellWidth(cell) - 10);
                        } else if (cell.getCellType() != CellType.STRING
                                && (cell.getCellType() == CellType.FORMULA
                                        && cell.getCachedFormulaResultType() != CellType.STRING)) {
                            cellData.needsMeasure = true;
                        } else {
                            // Text. A number too wide for its column is
                            // shortened or measured; text runs on into the
                            // empty cells beside it instead, and how far is
                            // settled below, once the alignment is known.
                            valueOverflows = true;
                        }
                    }
                } else if (!cellStyle.getWrapText() && runsOnAsText(cell)
                        && !manager.doesValueFit(cell, formattedCellValue)) {
                    // A link's label, TRUE or FALSE, an error: never
                    // shortened or hashed the way a number is, and the GWT
                    // client let them run on like any text.
                    valueOverflows = true;
                }

                if (cellStyle.getAlignment() == HorizontalAlignment.RIGHT) {
                    cellData.cellStyle = cellData.cellStyle + " r";
                } else if (cellStyle
                        .getAlignment() == HorizontalAlignment.GENERAL) {
                    if (SpreadsheetUtil.cellContainsDate(cell)
                            || cell.getCellType() == CellType.NUMERIC
                            || (cell.getCellType() == CellType.FORMULA
                                    && !cell.getCellFormula()
                                            .startsWith("HYPERLINK")
                                    && !(cell
                                            .getCachedFormulaResultType() == CellType.STRING))) {
                        cellData.cellStyle = cellData.cellStyle + " r";
                    }
                }

                if (valueOverflows) {
                    cellData.overflowWidth = spillWidth(cell,
                            formattedCellValue,
                            cellData.cellStyle.endsWith(" r"));
                }
            }

            // conditional formatting might be applied even if there isn't a
            // value (such as borders for the cell to the right)
            Set<Integer> cellFormattingIndexes = spreadsheet
                    .getConditionalFormatter().getCellFormattingIndex(cell);
            if (cellFormattingIndexes != null) {

                for (Integer i : cellFormattingIndexes) {
                    cellData.cellStyle = cellData.cellStyle + " cf" + i;
                }

                manager.flushQueue.markForUpdate(SpreadsheetUtil.toKey(cell));
            }

            if (cell.getCellType() == CellType.NUMERIC
                    && DateUtil.isCellDateFormatted(cell)) {
                cellData.originalValue = cellData.value;
            } else {
                cellData.originalValue = manager.getOriginalCellValue(cell);
            }

            handleIsDisplayZeroPreference(cell, cellData);
        } catch (RuntimeException rte) {
            LOGGER.trace(rte.getMessage(), rte);
            cellData.value = "#VALUE!";
        }

        return cellData;
    }

    /**
     * A value that is not text but reads like it: a link, a truth value, an
     * error.
     */
    private static boolean runsOnAsText(Cell cell) {
        return switch (cell.getCellType()) {
        case BOOLEAN, ERROR -> true;
        case FORMULA -> cell.getCellFormula().startsWith("HYPERLINK");
        default -> false;
        };
    }

    /**
     * How wide this cell's text is drawn: its own column and the empty cells to
     * its right that it runs into, or its own column alone when it has nowhere
     * to go. {@code 0} when no width can be estimated yet or the cell is
     * merged.
     *
     * @param cell
     *            the cell whose text does not fit its column
     * @param value
     *            the text as it will be displayed
     * @param rightAligned
     *            whether the cell is right-aligned, which keeps it put
     * @return the width in pixels, or {@code 0} to draw the cell as it is
     */
    private int spillWidth(Cell cell, String value, boolean rightAligned) {
        Spreadsheet spreadsheet = manager.spreadsheet;
        int textWidth = manager.fitCalculator.estimateWidth(cell, value);
        if (textWidth < 0) {
            // No width ratio reported yet, so there is nothing to measure
            // against. The client asks again once it has one.
            return 0;
        }
        for (CellRangeAddress merged : cell.getSheet().getMergedRegions()) {
            if (merged.isInRange(cell)) {
                // A merged cell already owns the columns it spans, and the
                // cells it would run into are its own.
                return 0;
            }
        }
        Row row = cell.getRow();
        return CellOverflowCalculator.spillWidth(cell.getColumnIndex(),
                textWidth, rightAligned, spreadsheet.getColW(),
                spreadsheet.getLastFrozenColumn(),
                column -> holdsValue(row, column));
    }

    /**
     * Whether the row shows anything at the given 0-based column.
     * <p>
     * Reads the cell's type rather than formatting it: this runs once per
     * column crossed for every overflowing cell in the viewport, and formatting
     * each neighbour would cost far more than the answer is worth. A blank
     * cell, a missing one, and one holding an empty string all count as empty,
     * so text runs across them.
     */
    private static boolean holdsValue(Row row, int column) {
        if (row == null) {
            return false;
        }
        Cell neighbour = row.getCell(column);
        if (neighbour == null) {
            return false;
        }
        return switch (neighbour.getCellType()) {
        case BLANK, _NONE -> false;
        case STRING -> !neighbour.getStringCellValue().isEmpty();
        default -> true;
        };
    }

    private void handleIsDisplayZeroPreference(Cell cell, CellData cellData) {
        boolean isCellNumeric = cell.getCellType() == CellType.NUMERIC;
        boolean isCellFormula = cell.getCellType() == CellType.FORMULA;
        boolean isApplicableCellType = isCellNumeric || isCellFormula;

        boolean displayZeroAsBlank = !cell.getSheet().isDisplayZeros();
        boolean valueIsZero = ZERO_AS_STRING.equals(cellData.value);

        if (isApplicableCellType && displayZeroAsBlank && valueIsZero) {
            cellData.value = "";
        }
    }
}
