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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Undo and redo of a value typed into a cell that did not exist yet: undo takes
 * the cell away again, and redo has to make it anew.
 */
class TypedValueUndoTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setLocale(Locale.US);
    }

    /** Types the value into the cell, which the client numbers from 1. */
    private void type(String address, String value) {
        spreadsheet.setSelection(address);
        CellReference cell = new CellReference(address);
        spreadsheet.getCellValueManager().onCellValueChange(cell.getCol() + 1,
                cell.getRow() + 1, value);
    }

    @Test
    void aValueTypedIntoANewCell_goesOnUndo_andComesBackOnRedo() {
        assertNull(spreadsheet.getCell("C5"));
        type("C5", "typed");

        spreadsheet.getSpreadsheetHistoryManager().undo();

        assertNull(spreadsheet.getCell("C5"));

        spreadsheet.getSpreadsheetHistoryManager().redo();

        assertEquals("typed", spreadsheet.getCell("C5").getStringCellValue());
    }

    @Test
    void aNumberTypedIntoANewCell_comesBackOnRedo() {
        type("D2", "42");
        spreadsheet.getSpreadsheetHistoryManager().undo();

        spreadsheet.getSpreadsheetHistoryManager().redo();

        assertEquals(42.0, spreadsheet.getCell("D2").getNumericCellValue());
    }

    @Test
    void aFormulaTypedOver_comesBackOnUndo() {
        spreadsheet.createCell(0, 0, 2.0);
        spreadsheet.createFormulaCell(1, 0, "A1*3");
        type("A2", "typed");

        spreadsheet.getSpreadsheetHistoryManager().undo();

        assertEquals("A1*3", spreadsheet.getCell("A2").getCellFormula());
    }

    @Test
    void aTruthValueTypedOver_comesBackOnUndo() {
        spreadsheet.createCell(0, 0, Boolean.TRUE);
        type("A1", "typed");

        spreadsheet.getSpreadsheetHistoryManager().undo();

        assertTrue(spreadsheet.getCell("A1").getBooleanCellValue());
    }

    @Test
    void anErrorTypedOver_comesBackOnUndo() {
        spreadsheet.createCell(0, 0, "")
                .setCellErrorValue(FormulaError.DIV0.getCode());
        type("A1", "typed");

        spreadsheet.getSpreadsheetHistoryManager().undo();

        assertEquals(FormulaError.DIV0.getCode(),
                spreadsheet.getCell("A1").getErrorCellValue());
    }
}
