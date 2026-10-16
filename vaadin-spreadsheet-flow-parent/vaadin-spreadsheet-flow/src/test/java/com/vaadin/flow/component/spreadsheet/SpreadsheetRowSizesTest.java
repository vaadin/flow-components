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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Rows given a height, or none: shown or hidden in the sheet and on the client.
 */
class SpreadsheetRowSizesTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    @Test
    void aHiddenRowGivenAHeight_isShownAtThatHeight() {
        spreadsheet.setRowHidden(2, true);

        spreadsheet.setRowHeight(2, 20f);

        assertFalse(spreadsheet.isRowHidden(2));
        assertFalse(spreadsheet.getHiddenRowIndexes().contains(3));
        assertEquals(20f, spreadsheet.getRowH()[2]);
    }

    @Test
    void aRowGivenAHeight_leavesTheRowBeforeItHidden() {
        spreadsheet.setRowHidden(1, true);

        spreadsheet.setRowHeight(2, 20f);

        assertTrue(spreadsheet.isRowHidden(1));
        assertTrue(spreadsheet.getHiddenRowIndexes().contains(2));
        assertEquals(0f, spreadsheet.getRowH()[1]);
    }

    @Test
    void aRowGivenAHeight_leavesTheRowsAndColumnsAsSet() {
        spreadsheet.setMaxRows(500);
        spreadsheet.setMaxColumns(80);

        spreadsheet.setRowHeight(2, 20f);

        assertEquals(500, spreadsheet.getRows());
        assertEquals(80, spreadsheet.getColumns());
    }

    @Test
    void aRowHidden_leavesTheRowsAndColumnsAsSet() {
        spreadsheet.setMaxRows(500);
        spreadsheet.setMaxColumns(80);

        spreadsheet.setRowHidden(2, true);

        assertEquals(500, spreadsheet.getRows());
        assertEquals(80, spreadsheet.getColumns());
    }

    @Test
    void aRowAddedLater_isGivenItsHeight() {
        spreadsheet.setMaxRows(500);

        spreadsheet.setRowHeight(400, 20f);

        assertEquals(20f, spreadsheet.getRowH()[400]);
        assertEquals(500, spreadsheet.getRows());
    }

    @Test
    void aDefaultHeightSetByTheApplication_isTheSheetsToo() {
        spreadsheet.setDefaultRowHeight(30f);

        assertEquals(30f, spreadsheet.getDefaultRowHeight());
        assertEquals(30f,
                spreadsheet.getActiveSheet().getDefaultRowHeightInPoints());
    }

    @Test
    void rowsWithoutAHeight_areShownAtTheDefaultHeightSet() {
        spreadsheet.setRowHeight(4, 50f);

        spreadsheet.setDefaultRowHeight(30f);

        assertEquals(30f, spreadsheet.getRowH()[3]);
        assertEquals(50f, spreadsheet.getRowH()[4]);
    }

    @Test
    void aRowGivenNoHeight_isHidden() {
        spreadsheet.setRowHeight(4, 0f);

        assertTrue(spreadsheet.isRowHidden(4));
        assertEquals(0f, spreadsheet.getRowH()[4]);
    }
}
