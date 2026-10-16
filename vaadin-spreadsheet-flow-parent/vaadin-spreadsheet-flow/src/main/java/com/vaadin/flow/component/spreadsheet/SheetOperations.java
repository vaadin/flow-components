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
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * The sheets of the workbook: showing one, creating, deleting, renaming, hiding
 * and protecting them, and what the client asks of its tabs.
 * <p>
 * The tabs leave out the hidden and very hidden sheets, so the index of a tab
 * is not the index of its sheet in POI; each method says which it takes. A step
 * the component has a public method for is taken through the component, so an
 * override of it applies.
 */
final class SheetOperations implements Serializable {

    private final Spreadsheet spreadsheet;
    private final SheetScrollMemory scrollMemory;

    SheetOperations(Spreadsheet spreadsheet, SheetScrollMemory scrollMemory) {
        this.spreadsheet = spreadsheet;
        this.scrollMemory = scrollMemory;
    }

    /** Shows the sheet of the tab at the index. */
    void setActiveSheetIndex(int sheetIndex) {
        if (sheetIndex < 0
                || sheetIndex >= spreadsheet.getSheetNames().length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        // Through the component, so an override of it applies.
        spreadsheet.setActiveSheetWithPOIIndex(
                spreadsheet.getVisibleSheetPOIIndex(sheetIndex));
    }

    /** Shows the sheet at the POI index, and loads everything of it. */
    void setActiveSheetWithPOIIndex(int sheetIndex) {
        Workbook workbook = spreadsheet.workbook;
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
        }
        if (!isShown(workbook, sheetIndex)) {
            throw new IllegalArgumentException(
                    "Cannot set a hidden or very hidden sheet as the active sheet. Given index: "
                            + sheetIndex);
        }
        workbook.setActiveSheet(sheetIndex);
        // assume since the UI doesn't allow multiple sheet selections
        // active sheet == selected tab
        workbook.setSelectedTab(sheetIndex);

        // formulas defined relative to the sheet may need recalculation
        spreadsheet.getFormulaEvaluator().clearAllCachedResultValues();
        spreadsheet.getConditionalFormattingEvaluator().clearAllCachedValues();

        spreadsheet.reloadActiveSheetData();
        SpreadsheetFactory.reloadSpreadsheetData(spreadsheet,
                workbook.getSheetAt(sheetIndex));
        spreadsheet.reloadActiveSheetStyles();
        spreadsheet.loadPopupButtons();
    }

    /**
     * The client chose a tab: where the sheet left was scrolled to is kept for
     * when it is shown again.
     */
    void onSheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
        scrollMemory.rememberForSheetAtIndex(spreadsheet.getSheetIndex() - 1,
                scrollLeft, scrollTop);
        Sheet oldSheet = spreadsheet.getActiveSheet();
        spreadsheet.setActiveSheetIndex(tabIndex);
        spreadsheet.fireSheetChangeEvent(oldSheet,
                spreadsheet.getActiveSheet());
    }

    /** The client added a sheet, of the size new sheets have. */
    void onNewSheetCreated(int scrollLeft, int scrollTop) {
        scrollMemory.rememberForSheetAtIndex(spreadsheet.getSheetIndex() - 1,
                scrollLeft, scrollTop);
        spreadsheet.createNewSheet(null, spreadsheet.defaultNewSheetRows,
                spreadsheet.defaultNewSheetColumns);
    }

    /** The client renamed the sheet of the tab at the index. */
    void onSheetRename(int sheetIndex, String sheetName) {
        // If Excel doesn't keep these in history, neither will we.
        spreadsheet.setSheetNameWithPOIIndex(
                spreadsheet.getVisibleSheetPOIIndex(sheetIndex), sheetName);
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

    /** Deletes the sheet of the tab at the index. */
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

    /** Deletes the sheet at the POI index. */
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
        // as the deleted sheet may have been in its formulas. Through the
        // component, so an override of it applies.
        spreadsheet.setActiveSheetWithPOIIndex(
                nearestShown(workbook, workbook.getActiveSheetIndex()));
    }

    /** Renames the sheet of the tab at the index. */
    void setSheetName(int sheetIndex, String sheetName) {
        if (sheetIndex < 0
                || sheetIndex >= spreadsheet.getSheetNames().length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        // Through the component, so an override of it applies.
        spreadsheet.setSheetNameWithPOIIndex(
                spreadsheet.getVisibleSheetPOIIndex(sheetIndex), sheetName);
    }

    /** Renames the sheet at the POI index. */
    void setSheetNameWithPOIIndex(int sheetIndex, String sheetName) {
        Workbook workbook = spreadsheet.workbook;
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
        }
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be null or an empty String, or contain backslash \\.");
        }
        if (spreadsheet.isSheetNameExisting(sheetName)) {
            throw new IllegalArgumentException(
                    "Sheet name must be unique within the workbook.");
        }
        workbook.setSheetName(sheetIndex, sheetName);
        spreadsheet.reloadSheetNames();
        // The formulas that name the sheet now name it anew, and the client
        // shows the formulas of its cells.
        spreadsheet.updateMarkedCells();
    }

    /** Shows, hides or very hides the sheet at the POI index. */
    void setSheetHidden(int sheetPOIIndex, SheetVisibility visibility) {
        Workbook workbook = spreadsheet.workbook;
        boolean wasShown = isShown(workbook, sheetPOIIndex);
        boolean shown = visibility == SheetVisibility.VISIBLE;
        // POI allows the user to hide all sheets; the component does not.
        if (wasShown && !shown
                && SpreadsheetUtil.getNumberOfVisibleSheets(workbook) == 1) {
            throw new IllegalArgumentException(
                    "At least one sheet should be always visible.");
        }
        int activeSheetIndex = workbook.getActiveSheetIndex();

        workbook.setSheetVisibility(sheetPOIIndex, visibility);

        // Nothing to load again when a hidden sheet is made very hidden, or
        // the other way round, or the sheet stays as it was.
        if (shown == wasShown) {
            return;
        }
        if (sheetPOIIndex != activeSheetIndex) {
            spreadsheet.reloadSheetNames();
            spreadsheet.setSheetIndex(
                    spreadsheet.getSpreadsheetSheetIndex(activeSheetIndex) + 1);
        } else {
            // The sheet shown was hidden: the next sheet shown takes its
            // place, or else the one before. Through the component, so an
            // override of it applies.
            spreadsheet.setActiveSheetWithPOIIndex(
                    nearestShown(workbook, activeSheetIndex));
        }
    }

    /**
     * Protects the sheet at the POI index with the password, or takes the
     * protection off with a {@code null} one.
     */
    void setSheetProtected(int sheetPOIIndex, String password) {
        Workbook workbook = spreadsheet.workbook;
        if (sheetPOIIndex < 0
                || sheetPOIIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
        }
        workbook.getSheetAt(sheetPOIIndex).protectSheet(password);
        spreadsheet
                .setSheetProtected(spreadsheet.getActiveSheet().getProtect());
        // What the protection of the sheet shown locks reaches the client
        // with the sheet: resizing, the rows and columns styled as locked,
        // and every cell, the one selected among them.
        if (sheetPOIIndex == workbook.getActiveSheetIndex()) {
            SpreadsheetFactory.loadSheetProtection(spreadsheet);
            spreadsheet.getSpreadsheetStyleFactory()
                    .reloadActiveSheetColumnRowStyles();
            spreadsheet.getCellValueManager().clearCachedContent();
            spreadsheet.reloadVisibleCellContents(false);
            spreadsheet.getCellSelectionManager().reSelectSelectedCell();
        }
    }

    void setActiveSheetProtected(String password) {
        // Through the component, so an override of it applies.
        spreadsheet.setSheetProtected(
                spreadsheet.workbook.getActiveSheetIndex(), password);
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
}
