/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;

import com.vaadin.shared.communication.ClientRpc;

public interface SpreadsheetClientRpc extends ClientRpc {

    void updateBottomRightCellValues(ArrayList<CellData> cellData);

    void updateTopLeftCellValues(ArrayList<CellData> cellData);

    void updateTopRightCellValues(ArrayList<CellData> cellData);

    void updateBottomLeftCellValues(ArrayList<CellData> cellData);

    /**
     * @param col
     *            Selected cell's column. 1-based
     * @param row
     *            Selected cell's row. 1-based
     */
    void updateFormulaBar(String possibleName, int col, int row);

    void invalidCellAddress();

    void showSelectedCell(String name, int col, int row, String cellValue,
            boolean formula, boolean locked, boolean initialSelection);

    /**
     * The String arrays contain the caption and the icon resource key.
     *
     * @param actionDetails
     */
    void showActions(ArrayList<SpreadsheetActionDetails> actionDetails);

    /**
     * Updates the selected cell and painted range. Displays the selected cell
     * value. Indexes 1-based.
     */
    void setSelectedCellAndRange(String name, int col, int row, int c1, int c2,
            int r1, int r2, boolean scroll);

    void cellsUpdated(ArrayList<CellData> updatedCellData);

    void refreshCellStyles();

    void editCellComment(int col, int row);
}
