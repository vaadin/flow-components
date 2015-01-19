package com.vaadin.addon.spreadsheet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;

/**
 * Utility class for miscellaneous Spreadsheet operations.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
public class SpreadsheetUtil {

    private static final Pattern keyParser = Pattern.compile("-?\\d+");

    private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;

    private static final int UNIT_OFFSET_LENGTH = 7;

    private static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109,
            146, 182, 219 };

    /**
     * Column width measured as the number of characters of the maximum digit
     * width of the numbers 0, 1, 2, ..., 9 as rendered in the normal style's
     * font. There are 4 pixels of margin padding (two on each side), plus 1
     * pixel padding for the grid lines.
     * 
     * This value is the same for default font in Office 2007 (Calibri) and
     * Office 2003 and earlier (Arial)
     */
    private static float DEFAULT_COLUMN_WIDTH = 9.140625f;

    /**
     * The width of 1px in columns with default width in units of 1/256 of a
     * character width.
     */
    private static final float PX_DEFAULT = 32.00f;

    /**
     * The width of 1px in columns with overridden width in units of 1/256 of a
     * character width
     */
    private static final float PX_MODIFIED = 36.56f;

    /**
     * Translates cell coordinates to a cell key used to identify cells in the
     * server<->client communiScation.
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
     * to identify cells in the server<->client communiScation.
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
        return cell.getCellType() == Cell.CELL_TYPE_NUMERIC
                && DateUtil.isCellDateFormatted(cell);
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
     * Gets the width of the column at the given index. Value is returned as
     * pixels.
     * 
     * @param sheet
     *            Target sheet
     * @param columnIndex
     *            Column index, 0-based
     * @return Width of the column in PX
     */
    static float getColumnWidthInPixels(Sheet sheet, int columnIndex) {
        // TODO investigate why the default column width gets a different value
        // using this compared to
        // ExcelToHtmlUtils.getColumnWidthInPx(widthUnits).
        if (sheet instanceof XSSFSheet) {
            CTCol col = ((XSSFSheet) sheet).getColumnHelper().getColumn(
                    columnIndex, false);
            double numChars = col == null || !col.isSetWidth() ? DEFAULT_COLUMN_WIDTH
                    : col.getWidth();
            return (float) numChars * XSSFWorkbook.DEFAULT_CHARACTER_WIDTH;
        } else if (sheet instanceof HSSFSheet) {
            int cw = sheet.getColumnWidth(columnIndex);
            int def = sheet.getDefaultColumnWidth() * 256;
            float px = cw == def ? PX_DEFAULT : PX_MODIFIED;
            return cw / px;
        } else {
            return ExcelToHtmlUtils.getColumnWidthInPx(sheet
                    .getColumnWidth(columnIndex));
        }
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
        short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH));

        widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];

        return widthUnits;
    }
}
