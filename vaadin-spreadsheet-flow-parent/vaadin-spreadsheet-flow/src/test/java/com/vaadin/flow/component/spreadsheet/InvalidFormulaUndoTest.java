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

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.command.RowInsertOrDeleteCommand;

/**
 * Undo puts the invalid-formula flag back with the cell it belongs to.
 *
 * <p>
 * A formula the server could not parse is kept as text and remembered in a side
 * map, which is what draws the red corner. Restoring only the text leaves the
 * cell looking fine while its content is still broken.
 */
class InvalidFormulaUndoTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        spreadsheet = new Spreadsheet();
    }

    @Test
    void invalidFormulaEntered_isFlagged() {
        enterInvalidFormula();

        assertTrue(spreadsheet.isMarkedAsInvalidFormula(1, 2));
        assertEquals("=a",
                spreadsheet.getActiveSheet().getRow(1).getCell(0)
                        .getStringCellValue(),
                "An unparseable formula is kept as text");
    }

    @Test
    void rowWithInvalidFormulaDeleted_flagIsDropped() {
        enterInvalidFormula();

        deleteSecondRow();

        assertFalse(spreadsheet.isMarkedAsInvalidFormula(1, 2));
    }

    @Test
    void rowWithInvalidFormulaDeletedAndUndone_flagIsBack() {
        enterInvalidFormula();
        RowInsertOrDeleteCommand command = deleteSecondRow();

        command.execute();

        assertEquals("=a", spreadsheet.getActiveSheet().getRow(1).getCell(0)
                .getStringCellValue(), "Undo restores the text");
        assertTrue(spreadsheet.isMarkedAsInvalidFormula(1, 2),
                "Undo has to restore the flag with the text, or the cell"
                        + " looks valid while holding a broken formula");
    }

    @Test
    void rowWithInvalidFormulaDeletedUndoneAndRedone_flagIsDroppedAgain() {
        enterInvalidFormula();
        RowInsertOrDeleteCommand command = deleteSecondRow();
        command.execute();

        command.execute();

        assertFalse(spreadsheet.isMarkedAsInvalidFormula(1, 2),
                "Redoing the delete takes the flag away again");
    }

    @Test
    void validCellInDeletedRowUndone_isNotFlagged() {
        spreadsheet.setSelection("A2");
        spreadsheet.getCellValueManager().onCellValueChange(1, 2, "plain");
        RowInsertOrDeleteCommand command = deleteSecondRow();

        command.execute();

        assertFalse(spreadsheet.isMarkedAsInvalidFormula(1, 2),
                "Undo must not invent a flag for a cell that never had one");
    }

    private void enterInvalidFormula() {
        spreadsheet.setSelection("A2");
        spreadsheet.getCellValueManager().onCellValueChange(1, 2, "=a");
    }

    private RowInsertOrDeleteCommand deleteSecondRow() {
        RowInsertOrDeleteCommand command = new RowInsertOrDeleteCommand(
                spreadsheet, new CellRangeAddress(1, 1, 0, 0));
        command.execute();
        return command;
    }
}
