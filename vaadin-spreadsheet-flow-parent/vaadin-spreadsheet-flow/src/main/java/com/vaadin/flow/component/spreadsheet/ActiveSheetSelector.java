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

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Translates between visible-sheet indexes (what the UI exposes) and POI
 * indexes (which include hidden / very hidden sheets), and activates a sheet on
 * the underlying {@link Workbook} with the cache invalidation and reload
 * cascade that has to follow.
 *
 * <p>
 * Throws {@link IllegalArgumentException} when an invalid index is given or
 * when an attempt is made to activate a hidden/very-hidden sheet.
 */
final class ActiveSheetSelector implements Serializable {

    private final Spreadsheet spreadsheet;

    ActiveSheetSelector(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    int getActiveSheetIndex() {
        return spreadsheet.getSheetIndex() - 1;
    }

    int getActiveSheetPOIIndex() {
        return spreadsheet.getVisibleSheetPOIIndex(getActiveSheetIndex());
    }

    void setActiveSheetIndex(int sheetIndex) {
        if (sheetIndex < 0
                || sheetIndex >= spreadsheet.getSheetNames().length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int poiSheetIndex = spreadsheet.getVisibleSheetPOIIndex(sheetIndex);
        // Through the component, so an override of it applies.
        spreadsheet.setActiveSheetWithPOIIndex(poiSheetIndex);
    }

    void setActiveSheetWithPOIIndex(int sheetIndex) {
        Workbook workbook = spreadsheet.workbook;
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
        }
        if (workbook.isSheetHidden(sheetIndex)
                || workbook.isSheetVeryHidden(sheetIndex)) {
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
}
