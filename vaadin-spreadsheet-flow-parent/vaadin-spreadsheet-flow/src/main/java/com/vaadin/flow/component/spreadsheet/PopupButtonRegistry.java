/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.ClientChanges.Call;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * The pop-up buttons of a spreadsheet by their cells, and which of them are on
 * the client. A button is on the client while its cell is shown on the active
 * sheet: its element is a virtual child of the spreadsheet's, and the client
 * has been sent its state with {@code addPopupButton}.
 * <p>
 * Cells are absolute (sheet-qualified) references, as made by
 * {@code SpreadsheetUtil.relativeToAbsolute}, so they survive sheet-name
 * changes.
 */
final class PopupButtonRegistry implements Serializable {

    private final Spreadsheet spreadsheet;
    private final Map<CellReference, PopupButton> buttonsByCell = new HashMap<>();
    /** The buttons on the client, with the state each was last sent in. */
    private final Map<PopupButton, BaseJsonNode> onTheClient = new HashMap<>();

    PopupButtonRegistry(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    PopupButton get(CellReference cell) {
        return buttonsByCell.get(cell);
    }

    /**
     * Puts the button on the cell in place of the one there, and on the client
     * if the cell is shown. A {@code null} button removes the one there.
     */
    void set(CellReference cell, PopupButton button) {
        PopupButton existing = buttonsByCell.remove(cell);
        if (existing != null) {
            detach(existing);
            spreadsheet.markAsDirty();
        }
        if (button != null) {
            button.setCellReference(cell);
            buttonsByCell.put(cell, button);
            if (isShown(button)) {
                attach(button);
                spreadsheet.markAsDirty();
            }
        }
    }

    /**
     * Puts the buttons of the active sheet whose cells are shown on the client,
     * and takes the others of that sheet off. Called whenever the cells shown
     * may have changed.
     * <p>
     * A button already on the client is sent again only when its state, such as
     * {@link PopupButton#markActive(boolean)}, has changed since.
     */
    void refresh() {
        String sheetName = spreadsheet.getActiveSheet().getSheetName();
        for (PopupButton button : buttonsByCell.values()) {
            if (!sheetName.equals(button.getCellReference().getSheetName())) {
                continue;
            }
            if (isShown(button)) {
                attach(button);
            } else {
                detach(button);
            }
        }
    }

    /**
     * Moves the buttons of the active sheet with their rows after
     * {@code Sheet.shiftRows(startRow, endRow, n)}, and drops those of the rows
     * the shift overwrote. A button that moves or goes is taken off the client
     * first, where it is still known by its old cell; the refresh after the
     * shift puts the moved ones back on. Rows are 0-based.
     */
    void shiftRows(int startRow, int endRow, int n) {
        String sheetName = spreadsheet.getActiveSheet().getSheetName();
        Map<CellReference, PopupButton> updated = new HashMap<>();
        for (PopupButton button : buttonsByCell.values()) {
            CellReference cell = button.getCellReference();
            int newRow = RowShiftOrchestrator.rowAfterShift(cell.getRow(),
                    startRow, endRow, n);
            if (!sheetName.equals(cell.getSheetName())
                    || newRow == cell.getRow()) {
                updated.put(cell, button);
                continue;
            }
            detach(button);
            if (newRow != RowShiftOrchestrator.OVERWRITTEN) {
                CellReference newCell = new CellReference(sheetName, newRow,
                        cell.getCol(), true, true);
                button.setCellReference(newCell);
                updated.put(newCell, button);
            }
        }
        buttonsByCell.clear();
        buttonsByCell.putAll(updated);
    }

    /** Takes every button off the client, as when another sheet is shown. */
    void detachAll() {
        for (PopupButton button : new ArrayList<>(onTheClient.keySet())) {
            detach(button);
        }
    }

    /** The client has started anew, with none of the buttons sent before. */
    void forgetClient() {
        onTheClient.clear();
    }

    /** Forgets every button, as when another workbook is set. */
    void clear() {
        buttonsByCell.clear();
    }

    private boolean isShown(PopupButton button) {
        return spreadsheet.viewport().shows(button.getRow(),
                button.getColumn());
    }

    private void attach(PopupButton button) {
        BaseJsonNode state = Serializer.toJson(button.getState());
        if (state.equals(onTheClient.put(button, state))) {
            // The client has it as it is.
            return;
        }
        spreadsheet.clientChanges().call(Call.POPUP_BUTTONS, "addPopupButton",
                state);
        if (!spreadsheet.getElement().equals(button.getElement().getParent())) {
            spreadsheet.getElement().appendVirtualChild(button.getElement());
        }
    }

    private void detach(PopupButton button) {
        if (onTheClient.remove(button) == null) {
            // Not on the client, so there is nothing to take off.
            return;
        }
        spreadsheet.clientChanges().call(Call.POPUP_BUTTONS,
                "removePopupButton", Serializer.toJson(button.getState()));
        if (spreadsheet.getElement().equals(button.getElement().getParent())) {
            spreadsheet.getElement().removeVirtualChild(button.getElement());
        }
    }
}
