package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormulaIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void testSimpleFormulaSheet() throws Exception {
        loadFile("formulasheet.xlsx");
        selectCell("A2");
        assertEquals("1", getSpreadsheet().getCellAt("A1").getValue());
        selectCell("A1");
        assertEquals("1", getFormulaFieldValue());

        assertEquals("2", getSpreadsheet().getCellAt("B1").getValue());
        selectCell("B1");
        assertEquals("=A1+1", getFormulaFieldValue());

        assertEquals("10", getSpreadsheet().getCellAt("C8").getValue());
        selectCell("C8");
        assertEquals("=C7+1", getFormulaFieldValue());
    }

    @Test
    public void validValueReference_invalidIsSet_formulaIsUpdated()
            throws Exception {
        createNewSpreadsheet();
        SheetCellElement cellA1 = getSpreadsheet().getCellAt(1, 1);
        SheetCellElement cellB1 = getSpreadsheet().getCellAt(2, 1);

        // Initial setup: A1=3, A2=A1
        cellA1.setValue("3");
        cellB1.setValue("=A1");

        // Change A1 to an invalid formula
        cellA1.setValue("=A+2");
        // Check reference to A1 was updated
        assertEquals("=A+2", cellB1.getValue());
    }
}
