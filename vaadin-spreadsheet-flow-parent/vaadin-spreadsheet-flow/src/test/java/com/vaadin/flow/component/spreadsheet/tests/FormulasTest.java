package com.vaadin.flow.component.spreadsheet.tests;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.FormulaValueChangeEvent;

public class FormulasTest {

    private Spreadsheet spreadsheet;

    @Before
    public void init() throws Exception {
        spreadsheet = new Spreadsheet();
        var ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void createFormulaCell_createsMissingRowsAndCols() {
        Assert.assertNull(spreadsheet.getActiveSheet().getRow(300));

        spreadsheet.createFormulaCell(300, 100, "1+1");

        var row = spreadsheet.getActiveSheet().getRow(300);
        Assert.assertNotNull(row);
        Assert.assertNotNull(row.getCell(100));
    }

    @Test
    public void createFormulaCell_overWritesExistingCellValue() {
        var cell = spreadsheet.createCell(0, 0, "foo");

        spreadsheet.createFormulaCell(0, 0, "1+1");

        Assert.assertNotEquals("foo", cell.getStringCellValue());
        Assert.assertEquals("1+1", cell.getCellFormula());
    }

    @Test
    public void formulaValueChangeListener_invokedOnFormulaValueChange() {
        // Add a formula value change listener
        var event = new AtomicReference<FormulaValueChangeEvent>();
        spreadsheet.addFormulaValueChangeListener(e -> event.set(e));

        // Create a formula cell and the cell it references
        var A1 = spreadsheet.createFormulaCell(0, 0, "A2+1");
        var A2 = spreadsheet.createCell(1, 0, null);
        spreadsheet.refreshCells(A1, A2);
        event.set(null);

        // Update the referenced cell value
        A2.setCellValue(1);
        spreadsheet.refreshCells(A1, A2);

        // Check that the event was fired with the correct values
        Assert.assertEquals(event.get().getChangedCells().size(), 1);
        Assert.assertEquals(event.get().getChangedCells().iterator().next()
                .formatAsString(), "Sheet1!A1");
        // Sanity check for the forumula cell effective value
        Assert.assertEquals(2.0, A1.getNumericCellValue(), 0.0);
    }

    @Test
    public void setInvalidFormula_invalidFormulaCellsSet() throws Exception {
        // onSheetScroll must be invoked once, otherwise cell comments are not
        // loaded
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll", "[1, 1, 1, 1]");

        // Create a formula cell with an invalid formula
        var A1 = spreadsheet.createFormulaCell(0, 0, "Sheet2!A1");
        spreadsheet.refreshCells(A1);

        Assert.assertEquals("[\"col1 row1\"]",
                spreadsheet.getElement().getProperty("invalidFormulaCells"));
    }

    @Test
    public void setInvalidFormulaErrorMessage_invalidFormulaErrorMessageSet()
            throws Exception {
        spreadsheet.setInvalidFormulaErrorMessage("foo");
        Assert.assertEquals("foo", spreadsheet.getElement()
                .getProperty("invalidFormulaErrorMessage"));
    }

}
