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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.tests.MockUIExtension;

/**
 * The selection each sheet was left with is remembered for as long as its
 * workbook is shown, and only that long: a workbook shown anew, or the same one
 * loaded again, leaves no listener of the last one behind.
 */
class SelectionMemoryLifecycleTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    private int selectionListeners() {
        return ComponentUtil.getListeners(spreadsheet,
                Spreadsheet.SelectionChangeEvent.class).size();
    }

    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");
        return workbook;
    }

    @Test
    void workbooksShownAndReloaded_leaveOneSelectionListener() {
        int listeners = selectionListeners();

        for (int i = 0; i < 3; i++) {
            spreadsheet.setWorkbook(workbook());
            ui.fakeClientCommunication();
        }
        spreadsheet.reload();
        spreadsheet.reload();
        ui.fakeClientCommunication();

        assertEquals(listeners, selectionListeners());
    }

    @Test
    void theWorkbookShown_remembersItsSelection_andTheLastOneNoLonger() {
        SheetState lastWorkbooks = spreadsheet.sheetState();

        spreadsheet.setWorkbook(workbook());
        ui.fakeClientCommunication();
        spreadsheet.setSelection("C4");

        assertEquals("C4", spreadsheet.sheetState()
                .getSelectedCellsOnSheet(spreadsheet.getActiveSheet()));
        assertNull(lastWorkbooks
                .getSelectedCellsOnSheet(spreadsheet.getActiveSheet()));
    }
}
