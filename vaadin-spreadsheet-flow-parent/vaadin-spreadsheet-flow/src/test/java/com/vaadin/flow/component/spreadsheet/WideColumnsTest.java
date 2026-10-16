/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;

/**
 * A wide column keeps its width in the workbook. The width was converted to
 * POI's units in a short, so every width from 896 pixels on overflowed and the
 * workbook got a negative or far too small one, while the sheet showed the
 * width that was asked for.
 */
class WideColumnsTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet(20, 5);
    }

    @Test
    void columnWiderThan896Pixels_keepsItsWidthInTheWorkbook() {
        spreadsheet.setColumnWidth(1, 1000);

        assertEquals(1000, spreadsheet.getColW()[1]);
        assertEquals(1000,
                spreadsheet.getActiveSheet().getColumnWidthInPixels(1), 1);
    }

    @Test
    void columnWiderThanTheWorkbookHolds_isKeptAtTheWidestItHolds() {
        spreadsheet.setColumnWidth(1, 5000);

        // 255 characters, the most POI holds, which it reads as 1785 pixels.
        assertEquals(1785,
                (int) spreadsheet.getActiveSheet().getColumnWidthInPixels(1));
        assertEquals(1785, spreadsheet.getColW()[1]);
    }

    @Test
    void columnDraggedVeryWide_isKeptAtTheWidestTheWorkbookHolds() {
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [2], \"newWidths\": [3000]}");

        assertEquals(1785, spreadsheet.getColW()[1]);
        assertEquals(1785,
                (int) spreadsheet.getActiveSheet().getColumnWidthInPixels(1));
    }
}
