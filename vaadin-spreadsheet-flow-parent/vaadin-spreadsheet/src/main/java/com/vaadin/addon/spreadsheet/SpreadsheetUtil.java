package com.vaadin.addon.spreadsheet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

public class SpreadsheetUtil {

    private static final Pattern keyParser = Pattern.compile("-?\\d+");

    /**
     * 
     * @param col
     *            1 based
     * @param row
     *            1 based
     * @return
     */
    public static final String toKey(int col, int row) {
        return "col" + col + " row" + row;
    }

    public static final String toKey(Cell cell) {
        return toKey(cell.getColumnIndex() + 1, cell.getRowIndex() + 1);
    }

    public static boolean cellContainsDate(Cell cell) {
        return cell.getCellType() == Cell.CELL_TYPE_NUMERIC
                && DateUtil.isCellDateFormatted(cell);
    }

    public static String getColHeader(int col) {
        String h = "";
        while (col > 0) {
            h = (char) ('A' + (col - 1) % 26) + h;
            col = (col - 1) / 26;
        }
        return h;
    }

    public static int getColHeaderIndex(String header) {
        int x = 0;
        for (int i = 0; i < header.length(); i++) {
            char h = header.charAt(i);
            x = (h - 'A' + 1) + (x * 26);
        }
        return x;
    }

    public static boolean isCellInRange(CellReference cr, CellRangeAddress cra) {
        return cra.isInRange(cr.getRow(), cr.getCol());
    }

    /**
     * Returns the POI index of the first visible sheet (not hidden & very
     * hidden). If no sheets are visible, returns 0. This is not be possible at
     * least in Excel, but unfortunately POI allows it.
     * 
     * @param workbook
     * @return 0-based
     */
    public static int getFirstVisibleSheetPOIIndex(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!(workbook.isSheetHidden(i) && workbook.isSheetVeryHidden(i))) {
                return i;
            }
        }
        return 0;
    }

    public static int getNumberOfVisibleSheets(Workbook workbook) {
        int result = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!(workbook.isSheetHidden(i) || workbook.isSheetVeryHidden(i))) {
                result++;
            }
        }
        return result;
    }

    public static int getColFromKey(String key) {
        Matcher matcher = keyParser.matcher(key);
        matcher.find();
        return Integer.valueOf(matcher.group());
    }
}
