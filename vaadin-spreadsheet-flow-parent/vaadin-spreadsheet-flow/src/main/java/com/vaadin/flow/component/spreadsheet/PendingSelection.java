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

/**
 * The selection a sheet is shown with, made once the client has the sheet and
 * reports where it is. What leads to cells of another sheet - a hyperlink, a
 * named range, an undo - sets it as the sheet is shown, and the cells are
 * brought into view. Else the sheet is shown with the selection it was left
 * with, or A1, which leaves the view where the client puts the sheet back.
 * <p>
 * The address is kept in the component's protected
 * {@code initialSheetSelection}, as on main, so that an application setting it
 * there is heard.
 */
final class PendingSelection implements Serializable {

    private final Spreadsheet spreadsheet;

    /**
     * Whether the selection, once made, leaves the client's view where it is:
     * the selection the sheet was left with, which need not be in view.
     */
    private boolean keepsView;

    PendingSelection(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Selects the address, of the sheet shown next, once the client has the
     * sheet: brought into view.
     */
    void select(String address) {
        spreadsheet.initialSheetSelection = address;
    }

    /**
     * A sheet is shown anew: the client reloads it, which clears its selection.
     * Unless a selection is set already, the sheet is shown with the one it was
     * left with, or A1, which leaves the view where it is unless cells are
     * selected after the reload.
     */
    void sheetShown() {
        if (spreadsheet.initialSheetSelection == null) {
            String selectedCellsOnSheet = spreadsheet.sheetState()
                    .getSelectedCellsOnSheet(spreadsheet.getActiveSheet());
            spreadsheet.initialSheetSelection = selectedCellsOnSheet == null
                    ? "A1"
                    : selectedCellsOnSheet;
            // The reload cleared the selection. Unless something has selected
            // cells since, this is the selection the sheet was left with, and
            // it leaves the view where the client puts the sheet back.
            keepsView = spreadsheet.getCellSelectionManager()
                    .getSelectedCellReference() == null;
        } else {
            keepsView = false;
        }
    }

    /**
     * The client has the sheet and reports where it is: the selection is made,
     * if one is pending.
     *
     * @return whether one was
     */
    boolean make() {
        String address = spreadsheet.initialSheetSelection;
        if (address == null) {
            return false;
        }
        CellSelectionManager selection = spreadsheet.getCellSelectionManager();
        if (keepsView) {
            selection.selectKeepingView(address);
        } else {
            selection.onSheetAddressChanged(address, true);
        }
        spreadsheet.initialSheetSelection = null;
        keepsView = false;
        return true;
    }
}
