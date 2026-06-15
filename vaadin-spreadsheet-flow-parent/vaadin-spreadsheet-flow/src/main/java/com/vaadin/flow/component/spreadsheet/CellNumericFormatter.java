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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Locale-aware numeric / formula-bar formatting for cell values.
 *
 * <p>
 * Owns the four formatting fields previously held by {@link CellValueManager}:
 * the POI {@link DataFormatter} (locale-aware {@link CustomDataFormatter}), the
 * formula-bar high-precision decimal format, the locale decimal symbols, and
 * the scientific-notation {@link CellValueFormatter}. All four reset together
 * on a locale change, so they form one concern.
 *
 * <p>
 * Methods cover the three formatting axes:
 *
 * <ul>
 * <li>Locale rebuild — {@link #updateLocale(Locale)}.</li>
 * <li>POI {@link DataFormatter} access — {@link #getDataFormatter()},
 * {@link #setDataFormatter(DataFormatter)}.</li>
 * <li>Cell value extraction — {@link #getOriginalCellValue(Cell)} (formula-bar
 * raw value), {@link #getCachedFormulaCellValue(Cell)} (formula's last
 * evaluated value), and the numeric predicates
 * {@link #valueContainsOnlyNumbers(String)} /
 * {@link #isGeneralCell(Cell)}.</li>
 * </ul>
 *
 * <p>
 * Used by {@link CellValueManager} (preserves its public API as thin
 * delegators) and by {@link CellFitCalculator} (which reads the embedded
 * {@link CellValueFormatter} for scientific-notation output).
 */
final class CellNumericFormatter implements Serializable {

    private static final String EXCEL_FORMULA_BAR_DECIMAL_FORMAT = "###.################";

    private DataFormatter formatter = new CustomDataFormatter();
    private DecimalFormat originalValueDecimalFormat = new DecimalFormat(
            EXCEL_FORMULA_BAR_DECIMAL_FORMAT);
    private DecimalFormatSymbols localeDecimalSymbols = DecimalFormatSymbols
            .getInstance();
    private CellValueFormatter cellValueFormatter = new CellValueFormatter();

    CellNumericFormatter() {
    }

    void updateLocale(Locale locale) {
        formatter = new CustomDataFormatter(locale);
        localeDecimalSymbols = DecimalFormatSymbols.getInstance(locale);
        originalValueDecimalFormat = new DecimalFormat(
                EXCEL_FORMULA_BAR_DECIMAL_FORMAT, localeDecimalSymbols);
        cellValueFormatter.setLocaleDecimalSymbols(localeDecimalSymbols);
    }

    DataFormatter getDataFormatter() {
        return formatter;
    }

    void setDataFormatter(DataFormatter dataFormatter) {
        formatter = dataFormatter;
    }

    DecimalFormat getOriginalValueDecimalFormat() {
        return originalValueDecimalFormat;
    }

    CellValueFormatter getCellValueFormatter() {
        return cellValueFormatter;
    }

    /**
     * Formula-bar raw value for {@code cell}. Returns the formula source for
     * {@code FORMULA} cells, an ISO-style date string for date-formatted
     * numeric cells, the leading-quote-preserved string for {@code STRING}
     * cells, the boolean's string form for {@code BOOLEAN}, the error text for
     * {@code ERROR}, and {@code ""} for {@code BLANK} / unknown.
     */
    String getOriginalCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType cellType = cell.getCellType();
        switch (cellType) {
        case FORMULA:
            return cell.getCellFormula();
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                Date dateCellValue = cell.getDateCellValue();
                if (dateCellValue != null) {
                    return new SimpleDateFormat().format(dateCellValue);
                }
                return "";
            }
            return originalValueDecimalFormat
                    .format(cell.getNumericCellValue());
        case STRING:
            String stringCellValue = cell.getStringCellValue();
            if (SpreadsheetUtil.needsLeadingQuote(cell)) {
                return "'" + stringCellValue;
            }
            return stringCellValue;
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case BLANK:
            return "";
        case ERROR:
            return String.valueOf(cell.getErrorCellValue());
        default:
            return "";
        }
    }

    /**
     * Cached evaluation result of {@code formulaCell}, formatted to its display
     * string per the cell's style. Used to detect formula re-evaluation drift
     * between syncs.
     */
    String getCachedFormulaCellValue(Cell formulaCell) {
        String result = null;
        switch (formulaCell.getCachedFormulaResultType()) {
        case BLANK:
        case FORMULA:
        case _NONE:
        case STRING:
            result = formulaCell.getStringCellValue();
            break;
        case BOOLEAN:
            result = String.valueOf(formulaCell.getBooleanCellValue());
            break;
        case ERROR:
            result = ErrorEval.getText(formulaCell.getErrorCellValue());
            break;
        case NUMERIC:
            CellStyle style = formulaCell.getCellStyle();
            result = formatter.formatRawCellContents(
                    formulaCell.getNumericCellValue(), style.getDataFormat(),
                    style.getDataFormatString());
            break;
        }
        return result;
    }

    boolean valueContainsOnlyNumbers(String value) {
        return value.matches("^-?\\d+("
                + localeDecimalSymbols.getDecimalSeparator() + "\\d+)?$");
    }

    boolean isGeneralCell(Cell cell) {
        return cell.getCellType() == CellType.NUMERIC && cell.getCellStyle()
                .getDataFormatString().contains("General");
    }
}
