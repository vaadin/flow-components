package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.RefListEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

/**
 * Utility class for miscellaneous Spreadsheet operations.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class SpreadsheetUtil implements Serializable {

    private static final Pattern keyParser = Pattern.compile("-?\\d+");

    private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;

    private static final int UNIT_OFFSET_LENGTH = 7;

    private static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109,
            146, 182, 219 };

    /**
     * Translates cell coordinates to a cell key used to identify cells in the
     * server-client communication.
     *
     * @param col
     *            Column index, 1-based
     * @param row
     *            Row index 1-based
     * @return Cell key
     */
    public static final String toKey(int col, int row) {
        return "col" + col + " row" + row;
    }

    /**
     * Translates cell coordinates from the given Cell object to a cell key used
     * to identify cells in the server-client communication.
     *
     * @param cell
     *            Cell to fetch the coordinates from
     * @return Cell key
     */
    public static final String toKey(Cell cell) {
        return toKey(cell.getColumnIndex() + 1, cell.getRowIndex() + 1);
    }

    /**
     * Determines whether the given cell contains a date or not.
     *
     * @param cell
     *            Cell to examine
     * @return true if the cell contains a date
     */
    public static boolean cellContainsDate(Cell cell) {
        return cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell);
    }

    public static CellReference relativeToAbsolute(Spreadsheet sheet,
            CellReference cell) {
        String sheetName = sheet.getActiveSheet().getSheetName();
        return new CellReference(sheetName, cell.getRow(), cell.getCol(), true,
                true);
    }

    /**
     * Generates the column header for column with the given index
     *
     * @param columnIndex
     *            Index of column, 1-based
     * @return Generated column header
     */
    public static String getColHeader(int columnIndex) {
        String h = "";
        while (columnIndex > 0) {
            h = (char) ('A' + (columnIndex - 1) % 26) + h;
            columnIndex = (columnIndex - 1) / 26;
        }
        return h;
    }

    /**
     * Returns the column index for the column with the given header.
     *
     * @param header
     *            Column header
     * @return Index of column, 1-based
     */
    public static int getColHeaderIndex(String header) {
        header = header.toUpperCase();
        int x = 0;
        for (int i = 0; i < header.length(); i++) {
            char h = header.charAt(i);
            x = (h - 'A' + 1) + (x * 26);
        }
        return x;
    }

    /**
     * Determines whether the given cell is within the given range.
     *
     * @param cellReference
     *            Target cell reference
     * @param cellRange
     *            Cell range to check
     * @return true if the cell is in the range
     */
    public static boolean isCellInRange(CellReference cellReference,
            CellRangeAddress cellRange) {
        return cellRange.isInRange(cellReference.getRow(),
                cellReference.getCol());
    }

    /**
     * Returns the POI index of the first visible sheet (not hidden or very
     * hidden). If no sheets are visible, returns 0. This is not be possible at
     * least in Excel, but unfortunately POI allows it.
     *
     * @param workbook
     *            Workbook to get the sheets from
     * @return Index of the first visible sheet, 0-based
     */
    public static int getFirstVisibleSheetPOIIndex(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!(workbook.isSheetHidden(i) && workbook.isSheetVeryHidden(i))) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Returns the number of visible sheets (not hidden or very hidden) in the
     * given Workbook.
     *
     * @param workbook
     *            Workbook to get the sheets from
     * @return Number of visible sheets
     */
    public static int getNumberOfVisibleSheets(Workbook workbook) {
        int result = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!(workbook.isSheetHidden(i) || workbook.isSheetVeryHidden(i))) {
                result++;
            }
        }
        return result;
    }

    /**
     * Returns the column index for the given Cell key.
     *
     * @param key
     *            Cell key
     * @return Column index of cell, 1-based
     */
    public static int getColumnIndexFromKey(String key) {
        Matcher matcher = keyParser.matcher(key);
        matcher.find(); // find first digit (col)
        return Integer.valueOf(matcher.group());
    }

    /**
     * Returns the row index for the given Cell key.
     *
     * @param key
     *            Cell key
     * @return Row index of cell, 1-based
     */
    public static int getRowFromKey(String key) {
        Matcher matcher = keyParser.matcher(key);
        matcher.find(); // find first digit (col)
        matcher.find(); // find second digit (row)
        return Integer.valueOf(matcher.group());
    }

    /**
     * Converts pixel units to Excel width units (one Excel width unit is
     * 1/256th of a character width)
     *
     * @param pxs
     *            Pixel value to convert
     * @return Value in Excel width units
     */
    static short pixel2WidthUnits(int pxs) {
        short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR
                * (pxs / UNIT_OFFSET_LENGTH));

        widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];

        return widthUnits;
    }

    /**
     * Gets the default column width for new sheets in pixels. The calculation
     * is done using POI.
     *
     * @return Default column width in PX
     */
    static int getDefaultColumnWidthInPx() {
        // Formula taken from XSSFSheet.getColumnWidthInPixels
        return (int) (SpreadsheetFactory.DEFAULT_COL_WIDTH_UNITS
                * EXCEL_COLUMN_WIDTH_FACTOR / 256.0
                * Units.DEFAULT_CHARACTER_WIDTH);
    }

    /**
     * Tries to parse the given String to a percentage. Specifically, checks if
     * the String ends with the '%' character, and the rest can be parsed to a
     * number.
     * <p>
     *
     * @param cellContent
     *            The string to be parsed
     * @param locale
     *            The current locale, used for number parsing.
     * @return the number as a decimal if it can be parsed; e.g. 42% returns
     *         0.42 and 0.42% returns 0.0042. Returns <code>null</code> if the
     *         number can't be parsed as a decimal.
     */
    public static Double parsePercentage(String cellContent, Locale locale) {

        if (cellContent == null || cellContent.length() < 2) {
            return null;
        }

        char last = cellContent.charAt(cellContent.length() - 1);
        if (last == '%') {

            String sub = cellContent.substring(0, cellContent.length() - 1);

            Double num = parseNumber(sub, locale);
            if (num != null) {
                return num / 100;
            }
        }

        return null;
    }

    public static Double parseNumber(Cell cell, String value, Locale locale) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (cell.getCellStyle().getDataFormatString() != null) {
            DataFormatter df = new CustomDataFormatter(locale);
            try {
                Method formatter = df.getClass().getDeclaredMethod("getFormat",
                        Cell.class);
                formatter.setAccessible(true);
                Format format = (Format) formatter.invoke(df, cell);
                if (format != null) {
                    ParsePosition parsePosition = new ParsePosition(0);
                    Object parsed = format.parseObject(value, parsePosition);
                    if (parsePosition.getIndex() == value.length()) {
                        if (parsed instanceof Double) {
                            return (Double) parsed;
                        } else if (parsed instanceof Number) {
                            return ((Number) parsed).doubleValue();
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            } catch (UnsupportedOperationException e) {
            }

        }
        return parseNumber(value, locale);
    }

    public static Double parseNumber(String cellContent, Locale locale) {

        if (cellContent == null) {
            return null;
        }

        try {

            String trimmedContent = cellContent.trim();

            if (locale != null) {
                DecimalFormat format = (DecimalFormat) DecimalFormat
                        .getInstance(locale);
                DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

                // using format.parse() won't work, as it doesn't handle
                // grouping separators correctly. If we have an Italian locale
                // (1.234,00) and try to parse 1.1, the result is 11. So, we
                // need to do some checking that the grouping separators are in
                // places we would expect.

                char groupSep = symbols.getGroupingSeparator();
                char decSep = symbols.getDecimalSeparator();

                int groupingIndex = trimmedContent.lastIndexOf(groupSep);
                int decIndex = trimmedContent.indexOf(decSep);

                // special case; non-breaking space
                if (groupSep == 160 && groupingIndex == -1) {
                    // try normal space
                    groupSep = ' ';
                    groupingIndex = trimmedContent.lastIndexOf(groupSep);

                    if (groupingIndex != -1) {
                        // replace normal spaces with non-breaking, so that
                        // parsing goes correctly
                        trimmedContent = trimmedContent.replaceAll(" ", " ");
                    }
                }

                // no decimal, grouping needs to be 3 characters from end
                boolean noDecButCorrect = decIndex == -1
                        && groupingIndex == trimmedContent.length() - 4;
                // but, if we have scientific notation, the above might not work
                // (e.g. 4.2e2 has 3 digits, but is invalid).
                if (groupingIndex != -1) {
                    try {
                        String sub = trimmedContent
                                .substring(trimmedContent.length() - 4);
                        if (sub.toLowerCase().contains("e")) {
                            noDecButCorrect = false;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // too short for correct use of grouping separator
                        noDecButCorrect = false;
                    }
                }

                // decimal point; grouping needs to be 3 chars in front of
                // decimal point
                boolean decAndGrouping = groupingIndex + 4 == decIndex;

                // again, check for scientific notation. If present, E needs to
                // be after decimal point and not the last char.
                if (decAndGrouping) {
                    int indexOfE = trimmedContent.toLowerCase().indexOf('e');
                    if (indexOfE != -1) {
                        if (indexOfE < decIndex
                                || indexOfE == trimmedContent.length() - 1) {
                            decAndGrouping = false;
                        }
                    }

                }

                if (groupingIndex == -1 || noDecButCorrect || decAndGrouping) {
                    ParsePosition pos = new ParsePosition(0);
                    Number parse = format.parse(trimmedContent, pos);
                    if (parse != null
                            && pos.getIndex() == trimmedContent.length()) {
                        return parse.doubleValue();
                    }
                }

            } else {
                // simple check
                trimmedContent = trimmedContent.replace(",", ".");
                Double d = Double.parseDouble(trimmedContent);
                return d;
            }

        } catch (NumberFormatException e) {
            // is OK
        }
        return null;
    }

    /**
     * Determine if the given cell content should be displayed with a leading
     * quote in both cell editor and formula bar
     * <p>
     *
     * @param cell
     *            The cell to be checked
     * @return true if the cell contains a string with the "quotePrefix" style
     *         set. Note that for Excel 97 file format, returns true for every
     *         string.
     */
    public static boolean needsLeadingQuote(Cell cell) {
        if (cell.getCellType() != CellType.STRING) {
            return false;
        }

        if (cell.getStringCellValue() == null) {
            return false;
        }

        return styleHasQuotePrefix(cell);
    }

    private static boolean styleHasQuotePrefix(Cell cell) {
        if (!(cell instanceof XSSFCell)) {
            // in 97 format all strings are prefixed
            return true;
        }

        XSSFCellStyle cellStyle = (XSSFCellStyle) cell.getCellStyle();

        if (cellStyle == null || cellStyle.getCoreXf() == null) {
            return false;
        }

        return cellStyle.getCoreXf().getQuotePrefix();
    }

    /**
     * evaluate the formula (which may just be a single cell or range string)
     * and find the bounding rectangle for the referenced cells.
     *
     * @param formula
     * @param spreadsheet
     * @param includeHiddenCells
     * @return CellRangeAddress bounding the evaluated result
     */
    public static CellRangeAddress getRangeForReference(String formula,
            Spreadsheet spreadsheet, boolean includeHiddenCells) {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxRow = 0;
        int maxCol = 0;
        for (CellReference ref : getAllReferencedCells(formula, spreadsheet,
                includeHiddenCells)) {
            minRow = Math.min(minRow, ref.getRow());
            maxRow = Math.max(maxRow, ref.getRow());
            minCol = Math.min(minCol, ref.getCol());
            maxCol = Math.max(maxCol, ref.getCol());
        }
        return new CellRangeAddress(minRow, maxRow, minCol, maxCol);
    }

    /**
     * This function returns all the cells that the given formula references.
     * You can optionally filter out all the hidden rows from the list honoring
     * filtering of charts based on SpreadsheetTable filter settings.
     *
     * @param formula
     *            The formula to find referenced cells for
     * @param spreadsheet
     *            Spreadsheet to operate on
     * @param includeHiddenCells
     *            <code>true</code> to include cells residing in hidden rows or
     *            columns, <code>false</code> to omit them
     *
     */
    public static List<CellReference> getAllReferencedCells(String formula,
            Spreadsheet spreadsheet, boolean includeHiddenCells) {
        final List<CellReference> cellRefs = new ArrayList<>();
        getAllReferencedCells(
                ((WorkbookEvaluatorProvider) spreadsheet.getFormulaEvaluator())
                        ._getWorkbookEvaluator().evaluate(formula,
                                new CellReference(
                                        spreadsheet.getActiveSheet()
                                                .getSheetName(),
                                        0, 0, false, false)),
                spreadsheet, cellRefs);

        if (includeHiddenCells) {
            return cellRefs;
        } else {
            // Filter out hidden cells of rows that are hidden (Excel spec)
            ArrayList<CellReference> visibleCells = new ArrayList<CellReference>();
            for (CellReference cr : cellRefs) {
                if (!spreadsheet.isRowHidden(cr.getRow())
                        && !spreadsheet.isColumnHidden(cr.getCol())) {
                    visibleCells.add(cr);
                }
            }
            return visibleCells;
        }
    }

    private static void getAllReferencedCells(ValueEval rawEval,
            Spreadsheet spreadsheet, List<CellReference> cells) {
        if (rawEval instanceof AreaEval) {
            // includes 2D and 3D contiguous ranges (1+ sheets, start/end
            // row/column)
            final AreaEval areaEval = (AreaEval) rawEval;
            for (int s = areaEval.getFirstSheetIndex(); s <= areaEval
                    .getLastSheetIndex(); s++) {
                for (int r = areaEval.getFirstRow(); r <= areaEval
                        .getLastRow(); r++) {
                    for (int c = areaEval.getFirstColumn(); c <= areaEval
                            .getLastColumn(); c++) {
                        cells.add(new CellReference(
                                spreadsheet.getWorkbook().getSheetName(s), r, c,
                                false, false));
                    }
                }
            }
        } else if (rawEval instanceof RefEval) {
            // same cell on 1+ sheets, by row/column index
            final RefEval refEval = (RefEval) rawEval;
            for (int s = refEval.getFirstSheetIndex(); s <= refEval
                    .getLastSheetIndex(); s++) {
                cells.add(new CellReference(
                        spreadsheet.getWorkbook().getSheetName(s),
                        refEval.getRow(), refEval.getColumn(), false, false));
            }
        } else if (rawEval instanceof RefListEval) {
            // list of evals, call this with each one
            final RefListEval list = (RefListEval) rawEval;
            for (ValueEval eval : list.getList())
                getAllReferencedCells(eval, spreadsheet, cells);
        } // ignore others, static values, not cell references
    }
}
