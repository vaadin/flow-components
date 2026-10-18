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
import java.util.regex.Matcher;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;

/**
 * Writes a value into a destination cell as the type-correct shift of a source
 * cell, optionally applying an arithmetic increment for Excel autofill.
 *
 * <p>
 * Type dispatch:
 *
 * <ul>
 * <li>{@code FORMULA} — copy the formula with its relative references moved
 * along the shift axis, as POI moves those of a copied row or column. Absolute
 * references (prefixed with {@code $}) are preserved, and a reference moved off
 * the sheet becomes {@code #REF!}.</li>
 * <li>{@code BOOLEAN}, {@code ERROR} — copy the value unchanged.</li>
 * <li>{@code NUMERIC} — copy, plus {@code sequenceIncrement * distance} when
 * the increment is non-null.</li>
 * <li>{@code STRING} — copy, plus increment the trailing-digit suffix when
 * present (uses {@link SequenceIncrement#TRAILING_DIGITS_PATTERN} so detection
 * and application cannot drift).</li>
 * <li>{@code BLANK} — write blank.</li>
 * </ul>
 *
 * <p>
 * Holds a {@link Spreadsheet} reference for two side effects: notifying the
 * style factory of the new cell ({@code cellStyleUpdated}) and notifying the
 * cell-value manager of the cell write ({@code cellUpdated} /
 * {@code cellDeleted}), plus calling {@code notifySetFormula} on the formula
 * evaluator after a formula rewrite. Used by {@link CellSelectionShifter}
 * during fill / drag / shift operations.
 */
final class CellValueShifter implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(CellValueShifter.class);

    private final Spreadsheet spreadsheet;

    CellValueShifter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Writes {@code shiftedCell}'s value into {@code newCell}, applying the
     * type-specific shift logic and optional arithmetic increment.
     *
     * @param shiftedCell
     *            Source cell
     * @param newCell
     *            Resulting new cell
     * @param removeShifted
     *            true to remove the source cell at the end
     * @param sequenceIncrement
     *            increment added to shifted cell value (or null)
     */
    void shift(Cell shiftedCell, Cell newCell, boolean removeShifted,
            Double sequenceIncrement) {
        // clear the new cell first because it might have errors which prevent
        // it from being set to a new type
        if (newCell.getCellType() != CellType.BLANK
                || shiftedCell.getCellType() == CellType.BLANK) {
            newCell.setBlank();
        }
        spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(newCell,
                true);
        switch (shiftedCell.getCellType()) {
        case FORMULA -> shiftFormula(shiftedCell, newCell);
        case BOOLEAN -> newCell.setCellValue(shiftedCell.getBooleanCellValue());
        case ERROR ->
            newCell.setCellErrorValue(shiftedCell.getErrorCellValue());
        case NUMERIC -> shiftNumeric(shiftedCell, newCell, sequenceIncrement);
        case STRING -> shiftString(shiftedCell, newCell, sequenceIncrement);
        case BLANK -> newCell.setBlank();
        // _NONE / future CellType values: no-op.
        default -> {
            // intentional no-op
        }
        }
        spreadsheet.getCellValueManager().cellUpdated(newCell);
        if (removeShifted) {
            shiftedCell.setCellValue((String) null);
            spreadsheet.getCellValueManager().cellDeleted(shiftedCell);
        }
    }

    private void shiftString(Cell shiftedCell, Cell newCell,
            Double sequenceIncrement) {
        if (sequenceIncrement != null) {
            int dif;
            if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                dif = newCell.getColumnIndex() - shiftedCell.getColumnIndex();
            } else {
                dif = newCell.getRowIndex() - shiftedCell.getRowIndex();
            }

            Matcher matcher = SequenceIncrement.TRAILING_DIGITS_PATTERN
                    .matcher(shiftedCell.getStringCellValue());
            if (matcher.find()) {
                String base = shiftedCell.getStringCellValue().substring(0,
                        matcher.start());
                String currentValue = matcher.group();
                Double currVal = Double.parseDouble(currentValue);
                newCell.setCellValue(base
                        + (int) Math.abs(currVal + sequenceIncrement * dif));
            } else {
                newCell.setCellValue(shiftedCell.getStringCellValue());
            }
        } else {
            newCell.setCellValue(shiftedCell.getStringCellValue());
        }
    }

    private void shiftNumeric(Cell shiftedCell, Cell newCell,
            Double sequenceIncrement) {
        if (sequenceIncrement != null) {
            int dif;
            if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                dif = newCell.getColumnIndex() - shiftedCell.getColumnIndex();
            } else {
                dif = newCell.getRowIndex() - shiftedCell.getRowIndex();
            }
            newCell.setCellValue(shiftedCell.getNumericCellValue()
                    + sequenceIncrement * dif);
        } else {
            newCell.setCellValue(shiftedCell.getNumericCellValue());
        }
    }

    /**
     * Copies the formula to the new cell, its relative references moved as far
     * as the cell moved, as Excel's fill handle moves them: the formula is
     * parsed and its references shifted by POI, so sheet names, function names
     * and text stay as they are, and a reference moved off the sheet becomes
     * {@code #REF!}.
     */
    private void shiftFormula(Cell shiftedCell, Cell newCell) {
        try {
            newCell.setCellFormula(copiedFormula(shiftedCell, newCell));
        } catch (RuntimeException e) {
            LOGGER.debug(e.getMessage(), e);
            // TODO visualize shifting error
            newCell.setCellFormula(shiftedCell.getCellFormula());
        }
        spreadsheet.getCellValueManager().getFormulaEvaluator()
                .notifySetFormula(newCell);
    }

    private static String copiedFormula(Cell from, Cell to) {
        Workbook workbook = from.getSheet().getWorkbook();
        if (workbook instanceof HSSFWorkbook hssf) {
            HSSFEvaluationWorkbook book = HSSFEvaluationWorkbook.create(hssf);
            return copiedFormula(from, to, book, book);
        }
        XSSFEvaluationWorkbook book = XSSFEvaluationWorkbook
                .create((XSSFWorkbook) workbook);
        return copiedFormula(from, to, book, book);
    }

    private static String copiedFormula(Cell from, Cell to,
            FormulaParsingWorkbook parsing,
            FormulaRenderingWorkbook rendering) {
        Sheet sheet = from.getSheet();
        Workbook workbook = sheet.getWorkbook();
        int sheetIndex = workbook.getSheetIndex(sheet);
        String sheetName = sheet.getSheetName();
        SpreadsheetVersion version = workbook.getSpreadsheetVersion();
        Ptg[] ptgs = FormulaParser.parse(from.getCellFormula(), parsing,
                FormulaType.CELL, sheetIndex);
        int rows = to.getRowIndex() - from.getRowIndex();
        if (rows != 0) {
            FormulaShifter
                    .createForRowCopy(sheetIndex, sheetName, from.getRowIndex(),
                            from.getRowIndex(), rows, version)
                    .adjustFormula(ptgs, sheetIndex);
        }
        int columns = to.getColumnIndex() - from.getColumnIndex();
        if (columns != 0) {
            FormulaShifter.createForColumnCopy(sheetIndex, sheetName,
                    from.getColumnIndex(), from.getColumnIndex(), columns,
                    version).adjustFormula(ptgs, sheetIndex);
        }
        return FormulaRenderer.toFormulaString(rendering, ptgs);
    }
}
