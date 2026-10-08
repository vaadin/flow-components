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
import java.util.Arrays;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Renames sheets on the underlying {@link Workbook} with uniqueness + non-empty
 * validation, and keeps the visible-sheet-names mirror used by the client in
 * sync.
 */
final class SheetMetadataOperations implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetMetadataOperations(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void setSheetName(int sheetIndex, String sheetName) {
        if (sheetIndex < 0
                || sheetIndex >= spreadsheet.getSheetNames().length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int poiSheetIndex = spreadsheet.getVisibleSheetPOIIndex(sheetIndex);
        // Through the component, so an override of it applies.
        spreadsheet.setSheetNameWithPOIIndex(poiSheetIndex, sheetName);
    }

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
        if (!workbook.isSheetVeryHidden(sheetIndex)
                && !workbook.isSheetHidden(sheetIndex)) {
            int ourIndex = spreadsheet.getSpreadsheetSheetIndex(sheetIndex);
            String[] names = Arrays.copyOf(spreadsheet.getSheetNames(),
                    spreadsheet.getSheetNames().length);
            names[ourIndex] = sheetName;
            spreadsheet.setSheetNames(names);
        }
    }
}
