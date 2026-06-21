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
import java.util.Optional;

import com.vaadin.flow.component.UI;

/**
 * Tracks whether a full client-side reload is pending and flushes the marker to
 * the client through a {@code beforeClientResponse} callback.
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
 * flag and registers the {@code beforeClientResponse} callback, and the
 * package-private {@link #flush(Spreadsheet)} method (called from the callback)
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
 * just set to true each time, the callback is registered fresh but is
 * idempotent on a true flag, and the flush still fires at most once per client
 * round.
 */
final class ClientReloadFlag implements Serializable {

    private boolean pending;

    boolean isPending() {
        return pending;
    }

    /**
     * Sets the pending flag to true and registers a
     * {@code beforeClientResponse} callback on the current UI that will call
     * {@link #flush(Spreadsheet)} when the flag is still true at client-flush
     * time. Multiple back-to-back calls coalesce into a single flush.
     */
    void markPending(Spreadsheet spreadsheet) {
        Optional.ofNullable(UI.getCurrent()).ifPresent(ui -> {
            ui.beforeClientResponse(spreadsheet, e -> {
                if (pending) {
                    flush(spreadsheet);
                }
            });
        });
        pending = true;
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
