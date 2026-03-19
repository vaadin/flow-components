/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.FormulaValueChangeEvent;
import com.vaadin.tests.MockUIExtension;

class FormulasTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();

        // onSheetScroll must be invoked once, otherwise cell comments are not
        // loaded
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "[1, 1, 1, 1]");
    }

    @Test
    void createFormulaCell_createsMissingRowsAndCols() {
        Assertions.assertNull(spreadsheet.getActiveSheet().getRow(300));

        spreadsheet.createFormulaCell(300, 100, "1+1");

        var row = spreadsheet.getActiveSheet().getRow(300);
        Assertions.assertNotNull(row);
        Assertions.assertNotNull(row.getCell(100));
    }

    @Test
    void createFormulaCell_overWritesExistingCellValue() {
        var cell = spreadsheet.createCell(0, 0, "foo");

        spreadsheet.createFormulaCell(0, 0, "1+1");

        spreadsheet.refreshCells(cell);

        Assertions.assertThrows(IllegalStateException.class,
                cell::getStringCellValue);
        Assertions.assertEquals(2, cell.getNumericCellValue(), 0);
        Assertions.assertEquals("1+1", cell.getCellFormula());
    }

    @Test
    void formulaValueChangeListener_invokedOnFormulaValueChange() {
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
        Assertions.assertEquals(1, event.get().getChangedCells().size());
        Assertions.assertEquals("Sheet1!A1", event.get().getChangedCells()
                .iterator().next().formatAsString());
        // Sanity check for the formula cell effective value
        Assertions.assertEquals(2.0, A1.getNumericCellValue(), 0.0);
    }

    @Disabled("Test ignored since it always passes locally but randomly fails on CI")
    @Test
    void setInvalidFormula_invalidFormulaCellsSet() {
        // Create a formula cell with an invalid formula
        var A1 = spreadsheet.createFormulaCell(0, 0, "Sheet2!A1");
        spreadsheet.refreshCells(A1);

        Assertions.assertEquals("[\"col1 row1\"]",
                spreadsheet.getElement().getProperty("invalidFormulaCells"));
    }

    @Test
    void setInvalidFormula_deleteSelectedCell_invalidFormulaCellsCleared() {
        // Create a formula cell with an invalid formula
        var A1 = spreadsheet.createFormulaCell(0, 0, "Sheet2!A1");
        spreadsheet.refreshCells(A1);

        // Delete the selected cell
        spreadsheet.setSelection("A1");
        spreadsheet.getCellValueManager().onDeleteSelectedCells();

        Assertions.assertEquals("[]",
                spreadsheet.getElement().getProperty("invalidFormulaCells"));
    }

    @Test
    void setInvalidFormula_deleteSelectedCells_invalidFormulaCellsCleared() {
        // Create a formula cell with an invalid formula
        var A1 = spreadsheet.createFormulaCell(0, 0, "Sheet2!A1");
        spreadsheet.refreshCells(A1);

        // Delete the selected cell range
        spreadsheet.setSelection("A1:A2");
        spreadsheet.getCellValueManager().onDeleteSelectedCells();

        Assertions.assertEquals("[]",
                spreadsheet.getElement().getProperty("invalidFormulaCells"));
    }

    @Test
    void setInvalidFormulaErrorMessage_invalidFormulaErrorMessageSet() {
        spreadsheet.setInvalidFormulaErrorMessage("foo");
        Assertions.assertEquals("foo", spreadsheet.getElement()
                .getProperty("invalidFormulaErrorMessage"));
    }

    @Disabled
    @Test
    void createFormulaCell_updateCellValue() {
        spreadsheet.setSelection("A1");
        var A1 = spreadsheet.createFormulaCell(0, 0, "1+1");
        Assertions.assertEquals("2", spreadsheet.getCellValue(A1));

        spreadsheet.getCellValueManager().onCellValueChange(1, 1, "foo");
        Assertions.assertEquals("foo", spreadsheet.getCellValue(A1));
    }

    @Test
    void createFormulaCellWithCircularReference_updateCellValue() {
        spreadsheet.setSelection("A1");
        var A1 = spreadsheet.createFormulaCell(0, 0, "A1");
        Assertions.assertEquals("~CIRCULAR~REF~", spreadsheet.getCellValue(A1));

        spreadsheet.getCellValueManager().onCellValueChange(1, 1, "foo");
        Assertions.assertEquals("foo", spreadsheet.getCellValue(A1));
    }
}
