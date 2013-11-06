package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vaadin.shared.communication.ClientRpc;

public interface SpreadsheetClientRpc extends ClientRpc {

    void addCells(HashMap<String, String> cellData,
            HashMap<Integer, String> selectorsToStyleMap);

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

    void addUpdatedCells(HashMap<String, String> updatedCellData,
            ArrayList<String> removedCells, HashMap<Integer, String> hashMap);

    /**
     * The String arrays contain the caption and the icon resource key.
     * 
     * @param actionDetails
     */
    void showActions(List<SpreadsheetActionDetails> actionDetails);
}
