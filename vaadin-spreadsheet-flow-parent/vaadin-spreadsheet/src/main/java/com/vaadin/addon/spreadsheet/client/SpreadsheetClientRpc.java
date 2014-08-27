package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;

import com.vaadin.shared.communication.ClientRpc;

public interface SpreadsheetClientRpc extends ClientRpc {

    void updateBottomRightCellValues(ArrayList<CellData> cellData);

    void updateTopLeftCellValues(ArrayList<CellData> cellData);

    void updateTopRightCellValues(ArrayList<CellData> cellData);

    void updateBottomLeftCellValues(ArrayList<CellData> cellData);

    /**
     * 
     * @param value
     * @param col
     *            1-based
     * @param row
     *            1-based
     * @param formula
     * @param locked
     */
    void showCellValue(String value, int col, int row, boolean formula,
            boolean locked);

    void invalidCellAddress();

    void showSelectedCell(int col, int row, String cellValue, boolean function,
            boolean locked);

    void showSelectedCellRange(int firstColumn, int lastColumn, int firstRow,
            int lastRow, String value, boolean formula, boolean locked);

    /**
     * The String arrays contain the caption and the icon resource key.
     * 
     * @param actionDetails
     */
    void showActions(ArrayList<SpreadsheetActionDetails> actionDetails);

    /**
     * Updates the selected cell and painted range. Displays the selected cell
     * value. Indexes 1-based.
     * 
     * @param col
     *            selected
     * @param row
     *            selected
     * @param c1
     *            painted
     * @param c2
     *            painted
     * @param r1
     *            painted
     * @param r2
     *            painted
     * @param value
     * @param formula
     * @param cellLocked
     */
    void setSelectedCellAndRange(int col, int row, int c1, int c2, int r1,
            int r2, String value, boolean formula, boolean cellLocked);

    void cellsUpdated(ArrayList<CellData> updatedCellData);
}