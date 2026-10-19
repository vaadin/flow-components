/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.rpc;

import java.io.Serializable;
import java.util.ArrayList;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;

/**
 * The messages the server sends to the client. Despite the "Rpc" suffix, kept
 * for compatibility, each is a call to the web component, sent at the end of
 * the round trip in a fixed order: the cells, then the selection, the formula
 * bar, the actions and the comment to write. The cells of the four pane methods
 * and of {@link #cellsUpdated(ArrayList)} all go the same way, and only those
 * the client does not have are sent.
 * <p>
 * Sending is all an implementation does: the selection has changed, and its
 * listeners have heard of it, before {@link #showSelectedCell} is called.
 *
 * @since 23.1
 */
public interface SpreadsheetClientRpc extends Serializable {

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
            boolean function, boolean locked, boolean initialSelection);

    /**
     * The String arrays contain the caption and the icon resource key.
     *
     * @param actionDetails
     *            the action details
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
