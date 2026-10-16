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
    private final ActiveSheetSelector selector;

    SheetLifecycleOperations(Spreadsheet spreadsheet,
            ActiveSheetSelector selector) {
        this.spreadsheet = spreadsheet;
        this.selector = selector;
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
        workbook.removeSheetAt(poiSheetIndex);
        spreadsheet.forgetScrollPositionOfDeletedSheet(poiSheetIndex);
        // POI keeps the sheet shown active, or in place of a deleted one
        // makes the next one active, or the one before when it was the last.
        // That one can be hidden. The sheet shown is loaded again either way,
        // as the deleted sheet may have been in its formulas.
        selector.setActiveSheetWithPOIIndex(
                nearestShown(workbook, workbook.getActiveSheetIndex()));
    }

    /**
     * The sheet at the index if it is shown, or else the nearest one shown
     * after it, or else before it.
     */
    private static int nearestShown(Workbook workbook, int index) {
        for (int i = index; i < workbook.getNumberOfSheets(); i++) {
            if (isShown(workbook, i)) {
                return i;
            }
        }
        int i = index;
        while (!isShown(workbook, i)) {
            i--;
        }
        return i;
    }

    private static boolean isShown(Workbook workbook, int index) {
        return !workbook.isSheetHidden(index)
                && !workbook.isSheetVeryHidden(index);
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
