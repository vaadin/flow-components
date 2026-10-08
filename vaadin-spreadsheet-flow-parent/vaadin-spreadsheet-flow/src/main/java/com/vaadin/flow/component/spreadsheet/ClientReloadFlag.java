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
 * Names the architectural pattern previously distributed across five fragments
 * on {@code Spreadsheet}:
 *
 * <ol>
 * <li>a {@code boolean reload} field — the pending-flag state</li>
 * <li>a private {@code setReload(boolean)} setter that mirrors a timestamp to
 * the client {@code reload} element property</li>
 * <li>the public {@code isRerenderPending()} read</li>
 * <li>an inline {@code beforeClientResponse} callback registration in
 * {@code reloadActiveSheetData}</li>
 * <li>a private {@code updateReloadState()} method that wires the callback's
 * flush logic, including a default for {@code initialSheetSelection}</li>
 * </ol>
 *
 * <p>
 * Now: {@link #isPending()} reads, {@link #markPending(Spreadsheet)} sets the
 * flag and schedules a {@link BeforeClientResponseAction}, and the
 * package-private {@link #flush(Spreadsheet)} method (which the action runs)
 * pushes the timestamp, clears the flag, and defaults
 * {@code initialSheetSelection} from the active sheet's selected-cells state
 * when not already set.
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
 * without a current UI - from a background thread, say - which the
 * {@code UI.beforeClientResponse} callback it replaces never did.
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
     * or, failing that, to {@code "A1"}.
     */
    void flush(Spreadsheet spreadsheet) {
        spreadsheet.getElement().setProperty("reload",
                System.currentTimeMillis());
        pending = false;
        if (spreadsheet.initialSheetSelection == null) {
            String selectedCellsOnSheet = spreadsheet.sheetState
                    .getSelectedCellsOnSheet(spreadsheet.getActiveSheet());
            spreadsheet.initialSheetSelection = selectedCellsOnSheet == null
                    ? "A1"
                    : selectedCellsOnSheet;
        }
    }
}
