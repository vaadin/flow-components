/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.ProtectedEditEvent;
import com.vaadin.tests.MockUIExtension;

class LockedCellValueTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setLocale(Locale.US);
    }

    @Test
    void lockSheet_receiveCellValueEditedEvent_preventsEdit() {
        var cell = spreadsheet.createCell(1, 1, "Initial value");
        lockSheet();
        var protectedEditEvent = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(protectedEditEvent::set);
        var cellValueChangeEvent = new AtomicReference<Spreadsheet.CellValueChangeEvent>();
        spreadsheet.addCellValueChangeListener(cellValueChangeEvent::set);
        spreadsheet.setSelection("B2");
        fireCellValueEditedEvent(2, 2, "Updated value");
        Assertions.assertNotNull(protectedEditEvent.get());
        Assertions.assertNull(cellValueChangeEvent.get());
        Assertions.assertEquals("Initial value", cell.getStringCellValue());
    }

    @Test
    void lockSheet_unlockCell_receiveCellValueEditedEvent_allowsEdit() {
        var cell = spreadsheet.createCell(1, 1, "Initial value");
        lockSheet();
        unlockCell("B2");
        var protectedEditEvent = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(protectedEditEvent::set);
        var cellValueChangeEvent = new AtomicReference<Spreadsheet.CellValueChangeEvent>();
        spreadsheet.addCellValueChangeListener(cellValueChangeEvent::set);
        spreadsheet.setSelection("B2");
        fireCellValueEditedEvent(2, 2, "Updated value");
        Assertions.assertNull(protectedEditEvent.get());
        Assertions.assertNotNull(cellValueChangeEvent.get());
        Assertions.assertEquals("Updated value", cell.getStringCellValue());
    }

    private void fireCellValueEditedEvent(int row, int col, String value) {
        TestHelper.fireClientEvent(spreadsheet, "cellValueEdited",
                "[" + row + ", " + col + ", \"" + value + "\"]");
    }

    private void lockSheet() {
        spreadsheet.setSheetProtected(0, "password");
    }

    private void unlockCell(String cellAddress) {
        spreadsheet.createCell(0, 0, "");
        var cellStyle = spreadsheet.getActiveSheet().getWorkbook()
                .createCellStyle();
        cellStyle.setLocked(false);
        spreadsheet.getCell(cellAddress).setCellStyle(cellStyle);
    }
}
