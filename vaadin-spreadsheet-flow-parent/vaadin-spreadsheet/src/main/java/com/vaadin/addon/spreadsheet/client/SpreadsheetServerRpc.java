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

import com.vaadin.shared.communication.ServerRpc;

public interface SpreadsheetServerRpc extends ServerRpc, SpreadsheetHandler {

    /**
     * Called when the client side connector has been initialized.
     * 
     * This is for making sure that the non-state related stuff is cleared from
     * server side when needed, because non state stuff is not resent to client
     * when the component is attached again. Thus this marks that cached should
     * be cleared etc.
     */
    void onConnectorInit();

    /**
     * Context menu should be created for the appropriate selection.
     * <p>
     * Selection can change if the cell at the given indexes isn't included in
     * the previous selection.
     * @param row
     *            1-based
     * @param column
     *            1-based
     */
    void contextMenuOpenOnSelection(int row, int column);

    /**
     * The action was selected from context menu for the current selection.
     * 
     * @param actionKey
     */
    void actionOnCurrentSelection(String actionKey);

    /**
     * Context menu should be created for the row.
     * 
     * @param rowIndex
     *            1-based
     */
    void rowHeaderContextMenuOpen(int rowIndex);

    /**
     * The action was selected from context menu for the row header.
     * 
     * @param actionKey
     */
    void actionOnRowHeader(String actionKey);

    /**
     * Context menu should be created for the column.
     * 
     * @param columnIndex
     *            1-based
     */
    void columnHeaderContextMenuOpen(int columnIndex);

    /**
     * The action was selected from context menu for the column header.
     * 
     * @param actionKey
     */
    void actionOnColumnHeader(String actionKey);

}
