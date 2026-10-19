/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.ArrayList;

import com.vaadin.flow.component.spreadsheet.ClientChanges.Call;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;

/**
 * What the server sends the client, as {@link SpreadsheetClientRpc} names it:
 * each method only sends. The cells, from any of the five methods that carry
 * them, go to the client's mirror of them, which sends those the client does
 * not have at the end of the round trip; the other calls are queued in
 * {@link ClientChanges}, which sends them after the cells, in a declared order.
 */
final class ClientRpcSender implements SpreadsheetClientRpc {

    private final Spreadsheet spreadsheet;

    ClientRpcSender(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public void updateBottomRightCellValues(ArrayList<CellData> cellData) {
        spreadsheet.clientCells.send(cellData);
    }

    @Override
    public void updateTopLeftCellValues(ArrayList<CellData> cellData) {
        spreadsheet.clientCells.send(cellData);
    }

    @Override
    public void updateTopRightCellValues(ArrayList<CellData> cellData) {
        spreadsheet.clientCells.send(cellData);
    }

    @Override
    public void updateBottomLeftCellValues(ArrayList<CellData> cellData) {
        spreadsheet.clientCells.send(cellData);
    }

    @Override
    public void updateFormulaBar(String possibleName, int col, int row) {
        spreadsheet.clientChanges().call(Call.FORMULA_BAR, "updateFormulaBar",
                possibleName, col, row);
    }

    @Override
    public void invalidCellAddress() {
        spreadsheet.clientChanges().call(Call.SELECTION, "invalidCellAddress");
    }

    /**
     * Also tells the client whether to leave its view where it is, which the
     * selection manager says.
     */
    @Override
    public void showSelectedCell(String name, int col, int row,
            String cellValue, boolean function, boolean locked,
            boolean initialSelection) {
        spreadsheet.clientChanges().call(Call.SELECTION, "showSelectedCell",
                name, col, row, cellValue, function, locked, initialSelection,
                keepingView());
    }

    @Override
    public void showActions(ArrayList<SpreadsheetActionDetails> actionDetails) {
        spreadsheet.clientChanges().call(Call.ACTIONS, "showActions",
                Serializer.toJson(actionDetails));
    }

    @Override
    public void setSelectedCellAndRange(String name, int col, int row, int c1,
            int c2, int r1, int r2, boolean scroll) {
        spreadsheet.clientChanges().call(Call.SELECTION,
                "setSelectedCellAndRange", name, col, row, c1, c2, r1, r2,
                scroll && !keepingView());
    }

    @Override
    public void cellsUpdated(ArrayList<CellData> cellData) {
        spreadsheet.clientCells.send(cellData);
    }

    @Override
    public void refreshCellStyles() {
        spreadsheet.clientCells.refreshStyles();
    }

    @Override
    public void editCellComment(int col, int row) {
        spreadsheet.clientChanges().call(Call.COMMENT, "editCellComment", col,
                row);
    }

    private boolean keepingView() {
        return spreadsheet.getCellSelectionManager().isKeepingView();
    }
}
