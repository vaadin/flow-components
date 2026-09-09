/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.rpc;

import java.util.ArrayList;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;

/**
 * Typed façade for server→client messages — NOT legacy Vaadin 8
 * {@code ClientRpc}. Despite the "Rpc" suffix (kept for historical continuity
 * and to mirror the GWT-side interface name), every method here is implemented
 * in {@code Spreadsheet} as a thin wrapper around Vaadin Flow's modern
 * {@code Element} API:
 *
 * <pre>
 * getElement().callJsFunction("methodName", serializedArgs)
 * </pre>
 *
 * <p>
 * The interface exists so the typed method shape is documented in one place;
 * the runtime mechanism is just DOM-event-style JS function invocation. There
 * is no legacy Vaadin 8 RPC machinery in the runtime path — that migration was
 * completed before the modernization plan started. See
 * {@code MODERNIZATION_PLAN.md} Tier 2 Step 10.
 *
 * @since 23.1
 */
public interface SpreadsheetClientRpc {

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
