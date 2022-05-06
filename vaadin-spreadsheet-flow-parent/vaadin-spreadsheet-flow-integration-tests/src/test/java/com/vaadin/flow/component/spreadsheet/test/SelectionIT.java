package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SelectionIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        createNewSpreadsheet();
    }

    @Test
    public void testSelectionSingleCell() {
        selectCell("B2");
        selectCell("C3", true, false);
        selectCell("D4", true, false);

        assertCellSelected("B2");
        assertCellSelected("C3");
        assertCellSelected("D4");
    }

    @Test
    public void testMultipleRectSelection() {
        selectCell("B1");
        selectCell("B3", false, true);
        selectCell("D3", true, false);
        selectCell("E3", false, true);

        assertCellSelected("B1");
        assertCellSelected("B2");
        assertCellSelected("B3");
        assertCellSelected("D3");
        assertCellSelected("E3");
    }

    @Test
    public void testComplicatedCellSelection() {
        selectCell("B2");
        selectCell("E2", true, false);
        selectCell("C3", true, false);
        selectCell("D4", false, true);
        selectCell("E5", true, false);
        selectCell("B5", true, false);

        assertCellSelected("B2");
        assertCellSelected("B5");
        assertCellSelected("C3");
        assertCellSelected("C4");
        assertCellSelected("D3");
        assertCellSelected("D4");
        assertCellSelected("E2");
        assertCellSelected("E5");
    }

    @Test
    public void testRowSelection() {
        selectRow(3);
        selectRow(5, true, false);
        selectRow(7, false, true);

        assertRowSelected("3");
        assertRowSelected("5");
        assertRowSelected("6");
        assertRowSelected("7");
    }

    @Test
    public void testColumnSelection() {
        selectColumn("B");
        selectColumn("D", true, false);
        selectColumn("F", false, true);

        assertColumnSelected("B");
        assertColumnSelected("D");
        assertColumnSelected("E");
        assertColumnSelected("F");
    }

    @Test
    public void testRowColumnMixed() {
        selectColumn("C");
        selectColumn("E", true, false);
        selectRow(3, true, false);
        selectRow(5, false, true);
        selectColumn("G", true, false);

        assertColumnSelected("C");
        assertColumnSelected("E");
        assertColumnSelected("G");
        assertRowSelected("3");
        assertRowSelected("4");
        assertRowSelected("5");
    }

    @Test
    @Ignore("Navigating to cell clears the selection in framework Spreadsheet, so this can't work -> Figure out another way to scroll")
    public void testColumnRowWithPagination() {
        selectColumn("C");
        selectColumn("E", true, false);
        selectRow(3, true, false);

        navigateToCell("C195");
        assertCellSelected("C190");
        assertCellSelected("C200");
        assertCellSelected("E190");
        assertCellSelected("E200");

        navigateToCell("AV3");
        assertCellSelected("AS3");
        assertCellSelected("AZ3");
    }

    @Test
    public void testShiftClick() {
        selectCell("B2");
        selectCell("C3", false, true);
        assertCellSelected("B2");
        assertCellSelected("B3");
        assertCellSelected("C2");
        assertCellSelected("C3");

        selectCell("B5");
        selectCell("B7", false, true);
        assertCellSelected("B5");
        assertCellSelected("B6");
        assertCellSelected("B7");

        selectCell("E2");
        selectCell("G2", false, true);
        assertCellSelected("E2");
        assertCellSelected("F2");
        assertCellSelected("G2");
    }

    @Test
    public void testWithMouse() {
        selectRegion("B2", "C3");

        assertCellSelected("B2");
        assertCellSelected("B3");
        assertCellSelected("C2");
        assertCellSelected("C3");

        selectRegion("B5", "B6");

        assertCellSelected("B5");
        assertCellSelected("B6");

        selectRegion("E2", "F2");

        assertCellSelected("E2");
        assertCellSelected("F2");
    }

    @Test
    public void mouseSelection_cellWithStringValue_cellIsSelected() {
        selectCell("B2");
        assertCellSelected("B2");
        getSpreadsheet().getCellAt("B2").setValue("value");

        selectCell("C5");
        assertCellSelected("C5");

        selectCell("B2");
        assertCellSelected("B2");
    }

    private void assertCellSelected(String cell) {
        assertTrue(getSpreadsheet().getCellAt(cell).isCellSelected());
    }

    private void assertRowSelected(String row) {
        for (int i = 0; i < 10; i++) {
            assertCellSelected((char) ('A' + i) + row);
        }
    }

    private void assertColumnSelected(String column) {
        for (int i = 1; i < 10; i++) {
            assertCellSelected(column + i);
        }
    }

}
