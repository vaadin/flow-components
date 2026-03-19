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
import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.ProtectedEditEvent;
import com.vaadin.tests.MockUIExtension;

class ClipboardTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setLocale(Locale.US);
    }

    @Test
    void paste_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\"]");
        Assertions.assertEquals("A1", getCellValue("A1"));
    }

    @Test
    void paste_numeric_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"1\"]");
        Assertions.assertEquals(1,
                spreadsheet.getCell("A1").getNumericCellValue(), 0.0);
    }

    @Test
    void paste_multiRow_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\\nA2\"]");
        Assertions.assertEquals("A1", getCellValue("A1"));
        Assertions.assertEquals("A2", getCellValue("A2"));
    }

    @Test
    void paste_multiRowR_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\\rA2\"]");
        Assertions.assertEquals("A1", getCellValue("A1"));
        Assertions.assertEquals("A2", getCellValue("A2"));
    }

    @Test
    void paste_multiRowRN_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\\r\\nA2\"]");
        Assertions.assertEquals("A1", getCellValue("A1"));
        Assertions.assertEquals("A2", getCellValue("A2"));
    }

    @Test
    void paste_multiRow_multiColumn_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\\tB1\\nA2\\tB2\"]");
        Assertions.assertEquals("A1", getCellValue("A1"));
        Assertions.assertEquals("A2", getCellValue("A2"));
        Assertions.assertEquals("B1", getCellValue("B1"));
        Assertions.assertEquals("B2", getCellValue("B2"));
    }

    @Test
    void lockedCell_paste_cellHasOriginalValue() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");
        // Try pasting the value to a locked cell
        paste("[\"A1\"]");
        Assertions.assertEquals("locked", getCellValue("A1"));
    }

    @Test
    void lockedCell_paste_firesProtectedEditEvent() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(e -> event.set(e));

        paste("[\"A1\"]");

        Assertions.assertNotNull(event.get());
    }

    @Test
    void lockSheet_pasteIntoNonExistingCell_cellNotCreated() {
        lockSheet();
        spreadsheet.setSelection("A1");
        // Try pasting the value to a non-existing cell in a locked sheet
        paste("[\"A1\"]");
        Assertions.assertNull(spreadsheet.getCell("A1"));
    }

    @Test
    void createCell_lockSheet_pasteIntoExistingCell_cellValueNotUpdated() {
        spreadsheet.createCell(9, 1, "initial");

        lockSheet();

        // Try pasting after locking
        spreadsheet.setSelection("B10");
        paste("[\"new data\"]");
        Assertions.assertEquals("initial", getCellValue("B10"));
    }

    @Test
    void lockSheet_unlockRow_shouldAllowPaste() {
        lockSheet();

        // Unlock a row
        var workbook = spreadsheet.getActiveSheet().getWorkbook();
        var unlockedRowStyle = workbook.createCellStyle();
        unlockedRowStyle.setLocked(false);

        var row = spreadsheet.getActiveSheet().createRow(5);
        row.setRowStyle(unlockedRowStyle);

        // Try pasting to a cell in the unlocked row
        spreadsheet.setSelection("A6");
        paste("[\"test data\"]");
        Assertions.assertEquals("test data", getCellValue("A6"));

        // Try pasting again after cell has been created
        paste("[\"new data\"]");
        Assertions.assertEquals("new data", getCellValue("A6"));
    }

    @Test
    void lockSheet_unlockColumn_shouldAllowPaste() {
        lockSheet();

        // Unlock a column
        var workbook = spreadsheet.getActiveSheet().getWorkbook();
        var unlockedColumnStyle = workbook.createCellStyle();
        unlockedColumnStyle.setLocked(false);

        spreadsheet.getActiveSheet().setDefaultColumnStyle(1,
                unlockedColumnStyle);

        // Try pasting to a cell in the unlocked column
        spreadsheet.setSelection("B10");
        paste("[\"test data\"]");
        Assertions.assertEquals("test data", getCellValue("B10"));

        // Try pasting again after cell has been created
        paste("[\"new data\"]");
        Assertions.assertEquals("new data", getCellValue("B10"));
    }

    @Test
    void paste_selectionUpdated() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\\tB1\"]");
        Assertions.assertEquals("A1:B1", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
    }

    @Test
    void paste_firesCellValueChangeEvent() {
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<CellValueChangeEvent>();
        spreadsheet.addCellValueChangeListener(e -> event.set(e));
        paste("[\"A1\"]");

        Assertions.assertNotNull(event.get());
    }

    @Test
    void paste_emptyCell() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\\tB1\\tC1\"]");
        paste("[\"A1\\t\\tC1\"]");

        Assertions.assertEquals("A1", getCellValue("A1"));
        Assertions.assertEquals("", getCellValue("B1"));
        Assertions.assertEquals("C1", getCellValue("C1"));
    }

    @Test
    void paste_undo_cellHasOriginalValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\"]");
        paste("[\"A1_updated\"]");
        undo();

        Assertions.assertEquals("A1", getCellValue("A1"));
    }

    @Test
    void paste_multiRow_multiColumn_undefinedCell() {
        spreadsheet.setSelection("A1:A2");
        // Paste a 2x2 matrix
        paste("[\"A1\\tB1\\nA2\\tB2\"]");
        // Paste the same matrix but with one cell undefined
        paste("[\"A1\\tB1\\nA2\"]");
        Assertions.assertEquals("A1", getCellValue("A1"));
        Assertions.assertEquals("A2", getCellValue("A2"));
        Assertions.assertEquals("B1", getCellValue("B1"));
        Assertions.assertEquals("", getCellValue("B2"));
    }

    @Test
    void cut_clearsSelectedCells() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\"]");
        cut();
        Assertions.assertEquals("", getCellValue("A1"));
    }

    @Test
    void lockedCell_cut_cellHasOriginalValue() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");
        cut();
        Assertions.assertEquals("locked", getCellValue("A1"));
    }

    @Test
    void lockedCell_singleSelection_cut_cellHasOriginalValue() {
        lockCell("A1");
        spreadsheet.setSelection("A1");
        cut();
        Assertions.assertEquals("locked", getCellValue("A1"));
    }

    @Test
    void lockedCell_cut_firesProtectedEditEvent() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(e -> event.set(e));

        cut();

        Assertions.assertNotNull(event.get());
    }

    @Test
    void lockedCell_singleSelection_cut_firesProtectedEditEvent() {
        lockCell("A1");
        spreadsheet.setSelection("A1");

        var event = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(e -> event.set(e));

        cut();

        Assertions.assertNotNull(event.get());
    }

    @Test
    void cut_firesCellValueChangeEvent() {
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<CellValueChangeEvent>();
        spreadsheet.addCellValueChangeListener(e -> event.set(e));
        cut();

        Assertions.assertNotNull(event.get());
    }

    @Test
    void cut_undo_cellHasOriginalValue() {
        spreadsheet.setSelection("A1:A2");
        paste("[\"A1\"]");
        cut();
        undo();

        Assertions.assertEquals("A1", getCellValue("A1"));
    }

    private String getCellValue(String cellAddress) {
        return spreadsheet.getCell(cellAddress).getStringCellValue();
    }

    private void paste(String clipboardContent) {
        TestHelper.fireClientEvent(spreadsheet, "onPaste", clipboardContent);
    }

    private void undo() {
        TestHelper.fireClientEvent(spreadsheet, "onUndo", "[]");
    }

    private void cut() {
        TestHelper.fireClientEvent(spreadsheet, "clearSelectedCellsOnCut",
                "[]");
    }

    private void lockSheet() {
        spreadsheet.setSheetProtected(0, "password");
    }

    private void lockCell(String cellAddress) {
        lockSheet();
        spreadsheet.createCell(0, 0, "locked");
        var lockedCellStyle = spreadsheet.getActiveSheet().getWorkbook()
                .createCellStyle();
        lockedCellStyle.setLocked(true);
        spreadsheet.getCell(cellAddress).setCellStyle(lockedCellStyle);
    }

}
