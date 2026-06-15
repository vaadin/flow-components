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

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Translates client-side sheet events (tab selected, new-sheet button clicked,
 * sheet renamed) into the right sequence of scroll-position stash +
 * sheet-activation + sheet-creation + listener notification.
 *
 * <p>
 * Owns the orchestration; the actual moves are delegated to
 * {@link SheetScrollMemory} (preserves scroll), {@link ActiveSheetSelector}
 * (activation), and {@link Spreadsheet} (createNewSheet,
 * setSheetNameWithPOIIndex, fireEvent).
 */
@SuppressWarnings("serial")
final class SheetChangeDispatcher implements Serializable {

    private final Spreadsheet spreadsheet;
    private final SheetScrollMemory scrollMemory;

    SheetChangeDispatcher(Spreadsheet spreadsheet,
            SheetScrollMemory scrollMemory) {
        this.spreadsheet = spreadsheet;
        this.scrollMemory = scrollMemory;
    }

    void onSheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
        // The very rare occasion when the sheet has been selected and the
        // selected sheet value is still negative.
        int oldIndex = Math.abs(spreadsheet.getSheetIndex()) - 1;
        scrollMemory.rememberForSheetAtIndex(oldIndex, scrollLeft, scrollTop);
        Sheet oldSheet = spreadsheet.getActiveSheet();
        spreadsheet.setActiveSheetIndex(tabIndex);
        Sheet newSheet = spreadsheet.getActiveSheet();
        spreadsheet.fireSheetChangeEvent(oldSheet, newSheet);
    }

    void onNewSheetCreated(int scrollLeft, int scrollTop) {
        scrollMemory.rememberForSheetAtIndex(spreadsheet.getSheetIndex() - 1,
                scrollLeft, scrollTop);
        spreadsheet.createNewSheet(null, spreadsheet.defaultNewSheetRows,
                spreadsheet.defaultNewSheetColumns);
    }

    void onSheetRename(int sheetIndex, String sheetName) {
        // If Excel doesn't keep these in history, neither will we.
        spreadsheet.setSheetNameWithPOIIndex(
                spreadsheet.getVisibleSheetPOIIndex(sheetIndex), sheetName);
    }
}
