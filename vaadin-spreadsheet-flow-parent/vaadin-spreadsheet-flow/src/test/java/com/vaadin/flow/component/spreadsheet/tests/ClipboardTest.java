package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.ProtectedEditEvent;

public class ClipboardTest {

    private Spreadsheet spreadsheet;

    @Before
    public void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setLocale(Locale.US);
        var ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void paste_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1']");
        Assert.assertEquals("A1", getCellValue("A1"));
    }

    @Test
    public void paste_numeric_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['1']");
        Assert.assertEquals(1, spreadsheet.getCell("A1").getNumericCellValue(),
                0.0);
    }

    @Test
    public void paste_multiRow_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1\\nA2']");
        Assert.assertEquals("A1", getCellValue("A1"));
        Assert.assertEquals("A2", getCellValue("A2"));
    }

    @Test
    public void paste_multiRowR_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1\\rA2']");
        Assert.assertEquals("A1", getCellValue("A1"));
        Assert.assertEquals("A2", getCellValue("A2"));
    }

    @Test
    public void paste_multiRowRN_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1\\r\\nA2']");
        Assert.assertEquals("A1", getCellValue("A1"));
        Assert.assertEquals("A2", getCellValue("A2"));
    }

    @Test
    public void paste_multiRow_multiColumn_cellHasValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1\\tB1\\nA2\\tB2']");
        Assert.assertEquals("A1", getCellValue("A1"));
        Assert.assertEquals("A2", getCellValue("A2"));
        Assert.assertEquals("B1", getCellValue("B1"));
        Assert.assertEquals("B2", getCellValue("B2"));
    }

    @Test
    public void lockedCell_paste_cellHasOriginalValue() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");
        // Try pasting the value to a locked cell
        paste("['A1']");
        Assert.assertEquals("locked", getCellValue("A1"));
    }

    @Test
    public void lockedCell_paste_firesProtectedEditEvent() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(e -> event.set(e));

        paste("['A1']");

        Assert.assertNotNull(event.get());
    }

    @Test
    public void paste_selectionUpdated() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1\tB1']");
        Assert.assertEquals("A1:B1", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
    }

    @Test
    public void paste_firesCellValueChangeEvent() {
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<CellValueChangeEvent>();
        spreadsheet.addCellValueChangeListener(e -> event.set(e));
        paste("['A1']");

        Assert.assertNotNull(event.get());
    }

    @Test
    public void paste_emptyCell() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1\tB1\tC1']");
        paste("['A1\t\tC1']");

        Assert.assertEquals("A1", getCellValue("A1"));
        Assert.assertEquals("", getCellValue("B1"));
        Assert.assertEquals("C1", getCellValue("C1"));
    }

    @Test
    public void paste_undo_cellHasOriginalValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1']");
        paste("['A1_updated']");
        undo();

        Assert.assertEquals("A1", getCellValue("A1"));
    }

    @Test
    public void paste_multiRow_multiColumn_undefinedCell() {
        spreadsheet.setSelection("A1:A2");
        // Paste a 2x2 matrix
        paste("['A1\\tB1\\nA2\\tB2']");
        // Paste the same matrix but with one cell undefined
        paste("['A1\\tB1\\nA2']");
        Assert.assertEquals("A1", getCellValue("A1"));
        Assert.assertEquals("A2", getCellValue("A2"));
        Assert.assertEquals("B1", getCellValue("B1"));
        Assert.assertEquals("", getCellValue("B2"));
    }

    @Test
    public void cut_clearsSelectedCells() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1']");
        cut();
        Assert.assertEquals("", getCellValue("A1"));
    }

    @Test
    public void lockedCell_cut_cellHasOriginalValue() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");
        cut();
        Assert.assertEquals("locked", getCellValue("A1"));
    }

    @Test
    public void lockedCell_singleSelection_cut_cellHasOriginalValue() {
        lockCell("A1");
        spreadsheet.setSelection("A1");
        cut();
        Assert.assertEquals("locked", getCellValue("A1"));
    }

    @Test
    public void lockedCell_cut_firesProtectedEditEvent() {
        lockCell("A1");
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(e -> event.set(e));

        cut();

        Assert.assertNotNull(event.get());
    }

    @Test
    public void lockedCell_singleSelection_cut_firesProtectedEditEvent() {
        lockCell("A1");
        spreadsheet.setSelection("A1");

        var event = new AtomicReference<ProtectedEditEvent>();
        spreadsheet.addProtectedEditListener(e -> event.set(e));

        cut();

        Assert.assertNotNull(event.get());
    }

    @Test
    public void cut_firesCellValueChangeEvent() {
        spreadsheet.setSelection("A1:A2");

        var event = new AtomicReference<CellValueChangeEvent>();
        spreadsheet.addCellValueChangeListener(e -> event.set(e));
        cut();

        Assert.assertNotNull(event.get());
    }

    @Test
    public void cut_undo_cellHasOriginalValue() {
        spreadsheet.setSelection("A1:A2");
        paste("['A1']");
        cut();
        undo();

        Assert.assertEquals("A1", getCellValue("A1"));
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

    private void lockCell(String cellAddress) {
        spreadsheet.setSheetProtected(0, "password");
        spreadsheet.createCell(0, 0, "locked");
        var lockedCellStyle = spreadsheet.getActiveSheet().getWorkbook()
                .createCellStyle();
        lockedCellStyle.setLocked(true);
        spreadsheet.getCell(cellAddress).setCellStyle(lockedCellStyle);
    }

}
