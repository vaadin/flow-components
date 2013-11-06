package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;

public class RowShiftTests extends Test1 {

    private String[] getFixture(int n) {
        String[] ret = new String[n];
        for (int i = 0; i < n; i++) {
            ret[i] = "" + (i + 1);
        }
        return ret;
    }

    @Test
    public void testBasic() {
        c.selectCell("A1");
        c.insertColumn(getFixture(10));

        c.selectCell("A5");
        loadServerFixture("INSERT_ROW");

        assertCellValue("A4", "4");
        assertCellValue("A5", "");
        assertCellValue("A6", "5");

        c.selectCell("A7");
        loadServerFixture("DELETE_ROW");

        assertCellValue("A8", "8");
    }

    @Test
    public void testFormula() {
        c.selectCell("A1");
        c.insertColumn(getFixture(10));

        c.putCellContent("B1", "=$A$6");
        c.putCellContent("C1", "=A6");
        c.putCellContent("B8", "=$A$6");
        c.putCellContent("C8", "=A6");

        c.selectCell("A3");

        loadServerFixture("INSERT_ROW");

        assertCellValue("B1", "6");
        assertCellValue("C1", "6");
        assertCellValue("B9", "6");
        assertCellValue("C9", "6");
    }

    @Test
    public void testDeleteFormulaReference() {

        c.putCellContent("A3", "42");
        c.putCellContent("C1", "=A3");

        c.selectCell("A4");
        loadServerFixture("DELETE_ROW");
        assertCellValue("A3", "42");
        assertCellValue("C1", "42");

        c.selectCell("A2");
        loadServerFixture("DELETE_ROW");
        assertCellValue("A2", "42");
        assertCellValue("C1", "42");

        c.selectCell("A2");
        loadServerFixture("DELETE_ROW");
        assertCellValue("C1", "ERROR:Unexpected celltype (5)");

    }
}
