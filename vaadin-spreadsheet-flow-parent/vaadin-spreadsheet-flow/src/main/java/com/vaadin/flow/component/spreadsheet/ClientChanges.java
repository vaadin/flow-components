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

import com.vaadin.flow.component.shared.internal.BeforeClientResponseAction;

/**
 * What changed for the client in a round trip, sent once before the response,
 * in this order: the rows and columns added past the last ones, then the
 * borders drawn on other cells, then a sheet shown anew, then the cells.
 * <p>
 * The rows and columns added go first, as what follows from them restyles
 * cells, and so changes borders and cells: the order saves the flush a second
 * pass. A sheet shown anew has the client reload it, and the client would drop
 * the cells it is sent with the reload: the reload forgets them, and the client
 * is given the cells it shows once it reports where it is. Each change is sent
 * once, its flag cleared as it is sent; sent again, it would change nothing on
 * the client, only take the time. Scheduled on the component, the flush also
 * runs when changes come without a current UI, from a background thread, say.
 */
final class ClientChanges implements Serializable {

    private final Spreadsheet spreadsheet;

    private final BeforeClientResponseAction flush;

    private boolean sizes;

    private boolean borders;

    private boolean sheetShown;

    /**
     * The reloads asked of the client so far, which its {@code reload} property
     * takes: a new value each time, as a timestamp was not when two reloads
     * came in the same millisecond, and the second never reached the client.
     */
    private int reloads;

    /** The reloads asked of the client, as it has them. */
    private final ClientProperty<Integer> reload;

    ClientChanges(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        flush = new BeforeClientResponseAction(spreadsheet, this::flush);
        reload = ClientProperty.integer(spreadsheet.getElement(), "reload", 0);
    }

    /** Rows or columns were added past the last ones of the sheet. */
    void sizes() {
        sizes = true;
        flush.schedule();
    }

    /**
     * What follows from the rows and columns added was done at once: doing it
     * again before the response would change nothing, only take the time.
     */
    void sizesDone() {
        sizes = false;
    }

    /** The borders drawn on other cells changed. */
    void borders() {
        borders = true;
        flush.schedule();
    }

    /** A sheet is shown anew: the client reloads it. */
    void sheetShown() {
        sheetShown = true;
        flush.schedule();
    }

    /** Cells are to be sent. */
    void cells() {
        flush.schedule();
    }

    /** Whether the client reloads the sheet at the end of this round trip. */
    boolean isSheetShown() {
        return sheetShown;
    }

    private void flush() {
        if (sizes) {
            sizes = false;
            spreadsheet.sizeChanged();
        }
        if (borders) {
            borders = false;
            spreadsheet.getSpreadsheetStyleFactory().sendBorders();
        }
        if (sheetShown) {
            sheetShown = false;
            reload();
        }
        spreadsheet.clientCells.flush();
    }

    /**
     * Has the client reload the sheet, which forgets its cells, and seeds the
     * selection it shows when not already set: the active sheet's remembered
     * selection, or A1. A seeded selection leaves the view where it is, unless
     * cells were selected after the reload; one that was already set, by a
     * hyperlink, a named range or an undo, is brought into view.
     */
    private void reload() {
        reload.set(++reloads);
        spreadsheet.forgetClientCells();
        if (spreadsheet.initialSheetSelection == null) {
            String selectedCellsOnSheet = spreadsheet.sheetState()
                    .getSelectedCellsOnSheet(spreadsheet.getActiveSheet());
            spreadsheet.initialSheetSelection = selectedCellsOnSheet == null
                    ? "A1"
                    : selectedCellsOnSheet;
            // The reload cleared the selection. Unless something has selected
            // cells since, this is the selection the sheet was left with, and
            // it leaves the view where the client puts the sheet back.
            spreadsheet.initialSheetSelectionKeepsView = spreadsheet
                    .getCellSelectionManager()
                    .getSelectedCellReference() == null;
        } else {
            spreadsheet.initialSheetSelectionKeepsView = false;
        }
    }
}
