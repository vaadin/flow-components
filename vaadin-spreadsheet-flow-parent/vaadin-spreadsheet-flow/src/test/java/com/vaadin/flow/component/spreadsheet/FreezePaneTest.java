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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Freezing rows and columns from the server, and taking the freeze off again:
 * in the workbook, and on the client, which draws the frozen panes.
 */
class FreezePaneTest {

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

    /** The frozen rows, as the client is told. */
    private int frozenRows() {
        return spreadsheet.getElement().getProperty("verticalSplitPosition",
                -1);
    }

    /** The frozen columns, as the client is told. */
    private int frozenColumns() {
        return spreadsheet.getElement().getProperty("horizontalSplitPosition",
                -1);
    }

    @Test
    void createFreezePane_freezesTheRowsAndColumnsOnTheClient() {
        spreadsheet.createFreezePane(2, 3);

        assertEquals(2, frozenRows());
        assertEquals(3, frozenColumns());
    }

    @Test
    void removeFreezePane_takesTheFreezeOff_inTheWorkbookAndOnTheClient() {
        spreadsheet.createFreezePane(2, 3);

        spreadsheet.removeFreezePane();

        assertNull(spreadsheet.getActiveSheet().getPaneInformation());
        assertEquals(0, frozenRows());
        assertEquals(0, frozenColumns());
    }

    @Test
    void removeFreezePane_onASheetWithoutOne_leavesItUnfrozen() {
        spreadsheet.removeFreezePane();

        assertNull(spreadsheet.getActiveSheet().getPaneInformation());
        assertEquals(0, frozenRows());
        assertEquals(0, frozenColumns());
    }
}
