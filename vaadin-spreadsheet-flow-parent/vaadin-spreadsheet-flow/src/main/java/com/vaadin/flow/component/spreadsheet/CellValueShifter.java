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
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.LoggerFactory;

/**
 * Writes a value into a destination cell as the type-correct shift of a source
 * cell, optionally applying an arithmetic increment for Excel autofill.
 *
 * <p>
 * Type dispatch:
 *
 * <ul>
 * <li>{@code FORMULA} — rewrite cell references along the shift axis (row or
 * column delta). Absolute references (prefixed with {@code $}) are
 * preserved.</li>
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

    static final Pattern ROW_SHIFT_PATTERN = Pattern
            .compile("[$]?[a-zA-Z]+[$]?\\d+");

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

    private void shiftFormula(Cell shiftedCell, Cell newCell) {
        try {
            if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                // shift column indexes
                int collDiff = newCell.getColumnIndex()
                        - shiftedCell.getColumnIndex();
                Matcher matcher = ROW_SHIFT_PATTERN
                        .matcher(shiftedCell.getCellFormula());
                String originalFormula = shiftedCell.getCellFormula();
                StringBuilder newFormula = new StringBuilder();
                int lastEnd = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    String replacement;
                    if (!s.startsWith("$")) {
                        String oldIndexString = s.replaceAll("[$]{0,1}\\d+",
                                "");

                        int columnIndex = SpreadsheetUtil
                                .getColHeaderIndex(oldIndexString);
                        columnIndex += collDiff;
                        replacement = s.replace(oldIndexString,
                                SpreadsheetUtil.getColHeader(columnIndex));
                    } else {
                        // if column has a '$' reference shouldn't change
                        replacement = s;
                    }
                    newFormula.append(originalFormula.substring(lastEnd,
                            matcher.start()));
                    newFormula.append(replacement);
                    lastEnd = matcher.end();
                }
                newFormula.append(originalFormula.substring(lastEnd));
                newCell.setCellFormula(newFormula.toString());
            } else { // shift row indexes
                int rowDiff = newCell.getRowIndex() - shiftedCell.getRowIndex();
                Matcher matcher = ROW_SHIFT_PATTERN
                        .matcher(shiftedCell.getCellFormula());
                String originalFormula = shiftedCell.getCellFormula();
                StringBuilder newFormula = new StringBuilder();
                int lastEnd = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    String rowString = s.replaceAll("[$]{0,1}[a-zA-Z]+", "");
                    String replacement;
                    if (!rowString.startsWith("$")) {
                        int row = Integer.parseInt(rowString);
                        row += rowDiff;
                        replacement = s.replace(rowString,
                                Integer.toString(row));
                    } else {
                        // if row has a '$' reference shouldn't change
                        replacement = s;
                    }
                    newFormula.append(originalFormula.substring(lastEnd,
                            matcher.start()));
                    newFormula.append(replacement);
                    lastEnd = matcher.end();
                }
                newFormula.append(originalFormula.substring(lastEnd));
                newCell.setCellFormula(newFormula.toString());
            }
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            // TODO visualize shifting error
            newCell.setCellFormula(shiftedCell.getCellFormula());
        }
        spreadsheet.getCellValueManager().getFormulaEvaluator()
                .notifySetFormula(newCell);
    }
}
