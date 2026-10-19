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
import java.util.EnumMap;
import java.util.List;

import com.vaadin.flow.component.shared.internal.BeforeClientResponseAction;

/**
 * What changed for the client in a round trip, sent once before the response,
 * in this order: the rows and columns added past the last ones, then the
 * borders drawn on other cells, then a sheet shown anew, then the cells, then
 * the calls, by {@link Call kind}. The client takes the properties of the
 * element before any call, whatever order they were set in.
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

    /**
     * The kinds of call the client is sent, in the order the flush sends them,
     * after the cells: the selection shown reads the cells, the formula bar
     * reads the selection and the cells, and the actions, the comment to write
     * and the pop-up buttons go to cells the client has by then. A pop-up
     * button is opened after it is added.
     */
    enum Call {
        SELECTION, FORMULA_BAR, ACTIONS, COMMENT, POPUP_BUTTONS
    }

    /** A call to the client, with its arguments as they were when made. */
    private record PendingCall(String function,
            Serializable[] arguments) implements Serializable {
    }

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

    /** The calls not sent yet, by kind, each kind in the order made. */
    private final EnumMap<Call, List<PendingCall>> calls = new EnumMap<>(
            Call.class);

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

    /**
     * Calls the function on the client in the flush, after the cells and the
     * calls of the kinds before this one.
     */
    void call(Call kind, String function, Serializable... arguments) {
        calls.computeIfAbsent(kind, ignored -> new ArrayList<>())
                .add(new PendingCall(function, arguments));
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
        // By kind, in the order the kinds are declared.
        calls.forEach((kind, pending) -> pending
                .forEach(call -> spreadsheet.getElement()
                        .callJsFunction(call.function(), call.arguments())));
        calls.clear();
    }

    /**
     * Has the client reload the sheet, which forgets its cells and its
     * selection: the sheet is shown with the selection pending, or else the one
     * it was left with.
     */
    private void reload() {
        reload.set(++reloads);
        spreadsheet.forgetClientCells();
        spreadsheet.pendingSelection().sheetShown();
    }
}
