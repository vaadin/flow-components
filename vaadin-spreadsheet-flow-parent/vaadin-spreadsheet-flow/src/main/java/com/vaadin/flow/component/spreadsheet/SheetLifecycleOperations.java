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
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Creates and deletes sheets on the underlying {@link Workbook}, with the
 * cascade (sheet-name reservation check, active-sheet shift after delete, data
 * reload, sheet-change event firing) that has to follow.
 */
final class SheetLifecycleOperations implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetLifecycleOperations(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void createNewSheet(String sheetName, int rows, int columns) {
        if (sheetName != null && sheetName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be an empty String.");
        }
        if (sheetName != null && sheetName.length() > 31) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be longer than 31 characters");
        }
        if (sheetName != null && spreadsheet.isSheetNameExisting(sheetName)) {
            throw new IllegalArgumentException(
                    "Sheet name must be unique within the workbook.");
        }
        Sheet previousSheet = spreadsheet.getActiveSheet();
        SpreadsheetFactory.addNewSheet(spreadsheet, spreadsheet.workbook,
                sheetName, rows, columns);
        spreadsheet.fireSheetChangeEvent(previousSheet,
                spreadsheet.getActiveSheet());
    }

    void deleteSheetWithPOIIndex(int poiSheetIndex) {
        if (spreadsheet.getNumberOfVisibleSheets() < 2) {
            throw new IllegalArgumentException(
                    "A workbook must contain at least one visible worksheet");
        }
        Workbook workbook = spreadsheet.workbook;
        int removedVisibleIndex = spreadsheet
                .getSpreadsheetSheetIndex(poiSheetIndex);
        workbook.removeSheetAt(poiSheetIndex);
        spreadsheet.forgetScrollPositionOfDeletedSheet(poiSheetIndex);

        // POI doesn't seem to shift the active sheet index ...
        // TODO: This no longer seems to be the case. Remove the following
        // logic?
        int oldIndex = spreadsheet.getSheetIndex() - 1;
        if (removedVisibleIndex <= oldIndex) { // removed before current
            if (oldIndex == spreadsheet.getNumberOfVisibleSheets()) {
                // need to shift index backwards if the current sheet is last
                workbook.setActiveSheet(
                        spreadsheet.getVisibleSheetPOIIndex(oldIndex - 1));
            } else {
                workbook.setActiveSheet(
                        spreadsheet.getVisibleSheetPOIIndex(oldIndex));
            }
        }
        // reload everything because there is ALWAYS a chance that the
        // removed sheet affects the currently visible sheet (formulas etc.)
        spreadsheet.reloadActiveSheetData();
    }

    void deleteSheet(int sheetIndex) {
        if (sheetIndex < 0
                || sheetIndex >= spreadsheet.getNumberOfVisibleSheets()) {
            throw new IllegalArgumentException(
                    "Invalid index for visible sheet given.");
        }
        // Through the component, so an override of it applies.
        spreadsheet.deleteSheetWithPOIIndex(
                spreadsheet.getVisibleSheetPOIIndex(sheetIndex));
    }
}
