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
            if (df instanceof CustomDataFormatter) {
                String color = ((CustomDataFormatter) df)
                        .getCellTextColor(cell);
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
                        }
                    }
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
