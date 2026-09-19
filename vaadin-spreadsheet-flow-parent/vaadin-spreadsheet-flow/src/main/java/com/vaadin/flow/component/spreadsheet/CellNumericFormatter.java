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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

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
    private DateFormat originalValueDateFormat = shortDateTimeFormat(
            Locale.getDefault(Locale.Category.FORMAT));

    CellNumericFormatter() {
    }

    void updateLocale(Locale locale) {
        formatter = new CustomDataFormatter(locale);
        localeDecimalSymbols = DecimalFormatSymbols.getInstance(locale);
        originalValueDecimalFormat = new DecimalFormat(
                EXCEL_FORMULA_BAR_DECIMAL_FORMAT, localeDecimalSymbols);
        cellValueFormatter.setLocaleDecimalSymbols(localeDecimalSymbols);
        originalValueDateFormat = shortDateTimeFormat(locale);
    }

    /**
     * The short date + time form the formula bar shows for a date cell - the
     * same shape a no-argument {@link java.text.SimpleDateFormat} produces, but
     * for the spreadsheet's locale instead of the JVM's.
     */
    private static DateFormat shortDateTimeFormat(Locale locale) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, locale);
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
        return switch (cell.getCellType()) {
        case FORMULA -> cell.getCellFormula();
        case NUMERIC -> formatNumericCell(cell);
        case STRING -> formatStringCell(cell);
        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
        case BLANK -> "";
        case ERROR -> String.valueOf(cell.getErrorCellValue());
        default -> "";
        };
    }

    private String formatNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            Date dateCellValue = cell.getDateCellValue();
            if (dateCellValue == null) {
                return "";
            }
            // getDateCellValue read the cell's serial number as a wall clock
            // in POI's time zone. Formatting in any other zone would move the
            // date the formula bar shows off the one stored in the workbook.
            originalValueDateFormat.setTimeZone(LocaleUtil.getUserTimeZone());
            return originalValueDateFormat.format(dateCellValue);
        }
        return originalValueDecimalFormat.format(cell.getNumericCellValue());
    }

    private String formatStringCell(Cell cell) {
        String stringCellValue = cell.getStringCellValue();
        return SpreadsheetUtil.needsLeadingQuote(cell) ? "'" + stringCellValue
                : stringCellValue;
    }

    /**
     * Cached evaluation result of {@code formulaCell}, formatted to its display
     * string per the cell's style. Used to detect formula re-evaluation drift
     * between syncs.
     */
    String getCachedFormulaCellValue(Cell formulaCell) {
        return switch (formulaCell.getCachedFormulaResultType()) {
        case BLANK, FORMULA, _NONE, STRING -> formulaCell.getStringCellValue();
        case BOOLEAN -> String.valueOf(formulaCell.getBooleanCellValue());
        case ERROR -> ErrorEval.getText(formulaCell.getErrorCellValue());
        case NUMERIC -> {
            CellStyle style = formulaCell.getCellStyle();
            yield formatter.formatRawCellContents(
                    formulaCell.getNumericCellValue(), style.getDataFormat(),
                    style.getDataFormatString());
        }
        // Preserve the pre-modernization behavior: pre-switch-expression
        // code initialized `result = null` and returned null when no case
        // matched. Without `default -> null` the new form would throw
        // MatchException if POI ever adds a new CellType.
        default -> null;
        };
    }

    /**
     * Whether a formatted value is a plain number, and so a candidate for being
     * shortened into scientific notation when it does not fit.
     * <p>
     * The minus sign is the locale's own: Finnish and many other locales write
     * a negative number with U+2212, not the hyphen, and a pattern that knows
     * only the hyphen leaves every negative number in those locales
     * unshortened. The hyphen is still accepted, since a value can reach here
     * formatted either way.
     * <p>
     * Both separators are quoted. An unquoted decimal separator is a regular
     * expression of its own - a dot matches any character at all.
     */
    boolean valueContainsOnlyNumbers(String value) {
        String minus = Pattern
                .quote(String.valueOf(localeDecimalSymbols.getMinusSign()));
        String separator = Pattern.quote(
                String.valueOf(localeDecimalSymbols.getDecimalSeparator()));
        return value
                .matches("^(" + minus + "|-)?\\d+(" + separator + "\\d+)?$");
    }

    boolean isGeneralCell(Cell cell) {
        return cell.getCellType() == CellType.NUMERIC && cell.getCellStyle()
                .getDataFormatString().contains("General");
    }
}
