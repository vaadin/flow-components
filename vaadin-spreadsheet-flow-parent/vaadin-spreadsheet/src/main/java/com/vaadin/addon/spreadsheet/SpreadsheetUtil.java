package com.vaadin.addon.spreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

public class SpreadsheetUtil {

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

}
