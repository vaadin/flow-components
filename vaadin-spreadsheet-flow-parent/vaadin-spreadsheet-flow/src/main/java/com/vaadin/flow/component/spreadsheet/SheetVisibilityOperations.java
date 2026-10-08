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

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Toggles sheet visibility (visible / hidden / very-hidden) and sheet-level
 * protection on the underlying {@link Workbook}. Owns the active-sheet shift
 * that has to happen when the currently active sheet is hidden, and the data +
 * style reload cascade.
 */
final class SheetVisibilityOperations implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetVisibilityOperations(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void setSheetHidden(int sheetPOIIndex, SheetVisibility visibility) {
        Workbook workbook = spreadsheet.workbook;
        // POI allows the user to hide all sheets; the component does not.
        if (visibility != SheetVisibility.VISIBLE
                && SpreadsheetUtil.getNumberOfVisibleSheets(workbook) == 1
                && !(workbook.isSheetHidden(sheetPOIIndex)
                        || workbook.isSheetVeryHidden(sheetPOIIndex))) {
            throw new IllegalArgumentException(
                    "At least one sheet should be always visible.");
        }
        boolean isHidden = workbook.isSheetHidden(sheetPOIIndex);
        boolean isVeryHidden = workbook.isSheetVeryHidden(sheetPOIIndex);
        int activeSheetIndex = workbook.getActiveSheetIndex();

        workbook.setSheetVisibility(sheetPOIIndex, visibility);

        // skip component reload if "nothing changed"
        boolean changed = (visibility == SheetVisibility.VISIBLE
                && (isHidden || isVeryHidden))
                || (visibility != SheetVisibility.VISIBLE
                        && !(isHidden || isVeryHidden));
        if (!changed) {
            return;
        }
        if (sheetPOIIndex != activeSheetIndex) {
            spreadsheet.reloadSheetNames();
            spreadsheet.setSheetIndex(
                    spreadsheet.getSpreadsheetSheetIndex(activeSheetIndex) + 1);
        } else {
            // the active sheet can only be hidden (not "made visible to
            // become active") through this branch
            int oldVisibleSheetIndex = spreadsheet.getSheetIndex() - 1;
            if (visibility != SheetVisibility.VISIBLE
                    && activeSheetIndex == (workbook.getNumberOfSheets() - 1)) {
                // hiding the active sheet, and it was the last sheet
                oldVisibleSheetIndex--;
            }
            int newActiveSheetIndex = spreadsheet
                    .getVisibleSheetPOIIndex(oldVisibleSheetIndex);
            workbook.setActiveSheet(newActiveSheetIndex);
            spreadsheet.reloadActiveSheetData();
            SpreadsheetFactory.reloadSpreadsheetData(spreadsheet,
                    spreadsheet.getActiveSheet());
            spreadsheet.reloadSheetStyles(false, false);
        }
    }

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
        // If the currently active sheet was protected, the protection for
        // the currently selected cell might have changed.
        if (sheetPOIIndex == workbook.getActiveSheetIndex()) {
            spreadsheet.loadCustomComponents();
            spreadsheet.getCellSelectionManager().reSelectSelectedCell();
        }
    }

    void setActiveSheetProtected(String password) {
        // Through the component, so an override of it applies.
        spreadsheet.setSheetProtected(
                spreadsheet.workbook.getActiveSheetIndex(), password);
    }
}
