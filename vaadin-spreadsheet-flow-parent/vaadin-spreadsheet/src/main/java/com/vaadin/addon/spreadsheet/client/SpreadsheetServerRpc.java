package com.vaadin.addon.spreadsheet.client;

import com.vaadin.shared.communication.ServerRpc;

public interface SpreadsheetServerRpc extends ServerRpc,
        SpreadsheetViewActionHandler {

    /**
     * Context menu should be created for the appropriate selection.
     * <p>
     * Selection can change if the cell at the given indexes isn't included in
     * the previous selection.
     * 
     * @param column
     *            1-based
     * @param row
     *            1-based
     */
    void contextMenuOpenOnSelection(int column, int row);

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
