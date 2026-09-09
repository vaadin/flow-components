/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.rpc;

import com.vaadin.flow.component.spreadsheet.client.SpreadsheetHandler;

/**
 * Typed façade for client→server messages — NOT legacy Vaadin 8
 * {@code ServerRpc}. Despite the "Rpc" suffix (kept for historical continuity),
 * the runtime mechanism is a single {@code @DomEvent} carrying a {@code type}
 * string plus a {@code JsonNode} payload (see
 * {@code Spreadsheet.SpreadsheetEvent}). Server-side dispatch routes the event
 * to {@code SpreadsheetHandlerImpl} which implements this interface and
 * translates each typed method into its concrete server-side action.
 *
 * <p>
 * The interface extends {@link SpreadsheetHandler} to share the grouping and
 * sheet-scroll methods declared there; the methods declared directly on this
 * interface are the context-menu / connector-init signals that don't have a
 * sibling on the GWT-side grouping handler.
 *
 * <p>
 * Subclass {@code SpreadsheetHandlerImpl} (not this interface directly) to
 * customize client-event handling on a per-instance basis via
 * {@code Spreadsheet.setSpreadsheetHandler(...)}.
 *
 * @since 23.1
 */
public interface SpreadsheetServerRpc extends SpreadsheetHandler {

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
     *
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
     *            the action key
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
     *            the action key
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
     *            the action key
     */
    void actionOnColumnHeader(String actionKey);

    /**
     * Context menu was closed.
     * 
     * @since 24.9.7
     */
    void contextMenuClosed();
}
