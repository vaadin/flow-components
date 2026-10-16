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
 * Tracks whether a full client-side reload is pending and flushes the marker to
 * the client once before the next response.
 *
 * <p>
 * {@link #isPending()} reads the flag, {@link #markPending(Spreadsheet)} sets
 * it and schedules a {@link BeforeClientResponseAction}, and
 * {@link #flush(Spreadsheet)}, which the action runs, pushes a timestamp to the
 * client {@code reload} element property, clears the flag, and defaults
 * {@code initialSheetSelection} from the active sheet's selected-cells state
 * when not already set. It also decides whether that selection leaves the
 * client's view where it is, as the selection a sheet was left with does.
 *
 * <p>
 * The flush also defaults the initial selection because the two are
 * load-bearing together: when the client reloads the active sheet, it needs a
 * target selection address to render — and the natural source is either the
 * {@code SheetState}-remembered selection (if any) or {@code "A1"}. Doing both
 * atomically in one flush avoids races between "selection seeded" and "reload
 * pushed".
 *
 * <p>
 * Multiple calls to {@link #markPending} between flushes coalesce: the flag is
 * just set to true each time, and the action runs at most once per client
 * round. Scheduled on the component, it also runs when the reload was asked for
 * without a current UI, from a background thread, say.
 */
final class ClientReloadFlag implements Serializable {

    private boolean pending;

    /** Runs {@link #flush(Spreadsheet)}; made on the first mark. */
    private BeforeClientResponseAction flushAction;

    boolean isPending() {
        return pending;
    }

    /**
     * Sets the pending flag to true and schedules {@link #flush(Spreadsheet)}
     * to run once before the next client response. Multiple back-to-back calls
     * coalesce into a single flush.
     */
    void markPending(Spreadsheet spreadsheet) {
        if (flushAction == null) {
            flushAction = new BeforeClientResponseAction(spreadsheet,
                    () -> flush(spreadsheet));
        }
        pending = true;
        flushAction.schedule();
    }

    /**
     * Pushes a fresh timestamp into the client {@code reload} property, clears
     * the pending flag, and seeds {@code spreadsheet.initialSheetSelection}
     * when not already set — either to the active sheet's remembered selection
     * or, failing that, to {@code "A1"}. A seeded selection leaves the view
     * where it is, unless cells were selected after the reload; one that was
     * already set, by a hyperlink, a named range or an undo, is brought into
     * view.
     */
    void flush(Spreadsheet spreadsheet) {
        spreadsheet.getElement().setProperty("reload",
                System.currentTimeMillis());
        pending = false;
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
