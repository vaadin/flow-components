package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

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
            boolean locked, boolean initialSelection);

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
            int r2, String value, boolean formula, boolean cellLocked,
            boolean scroll);

    void cellsUpdated(ArrayList<CellData> updatedCellData);

    void refreshCellStyles();

    void editCellComment(int col, int row);
}