package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

public class SelectionTest extends Test1 {

    @Test
    public void testSelectionSingleCell() {
        c.selectCell("B2");
        ctrl.selectCell("C3");
        ctrl.selectCell("D4");

        markSelectedCells();

        assertCellSelected("B2");
        assertCellSelected("C3");
        assertCellSelected("D4");
    }

    @Test
    public void testMultipleRectSelection() {
        c.selectCell("B1");
        shift.selectCell("B3");
        ctrl.selectCell("D3");
        shift.selectCell("E3");

        markSelectedCells();

        assertCellSelected("B1");
        assertCellSelected("B2");
        assertCellSelected("B3");
        assertCellSelected("D3");
        assertCellSelected("E3");
    }

    @Test
    public void testComplicatedCellSelection() {
        c.selectCell("B2");
        ctrl.clickCell("E2");
        ctrl.clickCell("C3");
        shift.clickCell("D4");
        ctrl.clickCell("E5");
        ctrl.clickCell("B5");

        markSelectedCells();

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
        c.clickRow("3");
        ctrl.clickRow("5");
        shift.clickRow("7");

        markSelectedCells();

        assertRowSelected("3");
        assertRowSelected("5");
        assertRowSelected("6");
        assertRowSelected("7");
    }

    @Test
    public void testColumnSelection() {
        c.clickColumn("B");
        ctrl.clickColumn("D");
        shift.clickColumn("F");

        markSelectedCells();

        assertColumnSelected("B");
        assertColumnSelected("D");
        assertColumnSelected("E");
        assertColumnSelected("F");
    }

    @Test
    public void testRowColumnMixed() {
        c.clickColumn("C");
        ctrl.clickColumn("E");
        ctrl.clickRow("3");
        shift.clickRow("5");
        ctrl.clickColumn("G");

        markSelectedCells();

        assertColumnSelected("C");
        assertColumnSelected("E");
        assertColumnSelected("G");
        assertRowSelected("3");
        assertRowSelected("4");
        assertRowSelected("5");
    }

    @Test
    public void testColumnRowWithPagination() {
        c.clickColumn("C");
        ctrl.clickColumn("E");
        ctrl.clickRow("3");

        markSelectedCells();

        c.navigateToCell("C195");
        assertCellSelected("C190");
        assertCellSelected("C200");
        assertCellSelected("E190");
        assertCellSelected("E200");

        c.navigateToCell("AV3");
        assertCellSelected("AS3");
        assertCellSelected("AZ3");
    }

    @Test
    public void testShiftClick() {
        c.clickCell("B2");
        shift.clickCell("C3");
        markSelectedCells();

        c.clickCell("B5");
        shift.clickCell("B7");
        markSelectedCells();

        c.clickCell("E2");
        shift.clickCell("G2");
        markSelectedCells();

        assertCellSelected("B2");
        assertCellSelected("B3");
        assertCellSelected("C2");
        assertCellSelected("C3");

        assertCellSelected("B5");
        assertCellSelected("B6");
        assertCellSelected("B7");

        assertCellSelected("E2");
        assertCellSelected("F2");
        assertCellSelected("G2");

    }

    @Test
    public void testWithMouse() {

        c.selectRegion("B2", "C3");
        markSelectedCells();

        assertCellSelected("B2");
        assertCellSelected("B3");
        assertCellSelected("C2");
        assertCellSelected("C3");

        c.selectRegion("B5", "B6");
        markSelectedCells();

        assertCellSelected("B5");
        assertCellSelected("B6");

        c.selectRegion("E2", "F2");
        markSelectedCells();

        assertCellSelected("E2");
        assertCellSelected("F2");
    }

    protected void markSelectedCells() {
        loadServerFixture("SELECTION");
    }

    private void assertCellSelected(String cell) {
        Assert.assertEquals("SELECTED", c.getCellContent(cell));
    }

    private void assertRowSelected(String row) {
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("SELECTED",
                    c.getCellContent((char) ('A' + i) + row));
        }
    }

    private void assertColumnSelected(String column) {
        for (int i = 1; i < 10; i++) {
            Assert.assertEquals("SELECTED", c.getCellContent(column + i));
        }
    }

}
