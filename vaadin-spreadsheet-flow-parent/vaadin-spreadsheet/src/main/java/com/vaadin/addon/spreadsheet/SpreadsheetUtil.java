package com.vaadin.addon.spreadsheet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Utility class for miscellaneous Spreadsheet operations.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
public class SpreadsheetUtil {

    private static final Pattern keyParser = Pattern.compile("-?\\d+");

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
     * @return
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

    public static int getRowFromKey(String key) {
        Matcher matcher = keyParser.matcher(key);
        matcher.find(); // find first digit (col)
        matcher.find(); // find second digit (row)
        return Integer.valueOf(matcher.group());
    }
}
