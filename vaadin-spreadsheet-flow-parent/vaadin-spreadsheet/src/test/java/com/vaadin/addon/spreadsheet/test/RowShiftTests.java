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
        sheetController.selectCell("A2");
        sheetController.selectCell("A1");
        sheetController.insertColumn(getFixture(10));

        sheetController.selectCell("A5");
        loadServerFixture("INSERT_ROW");

        assertCellValue("A4", "4");
        assertCellValue("A5", "");
        assertCellValue("A6", "5");

        sheetController.selectCell("A7");
        loadServerFixture("DELETE_ROW");

        assertCellValue("A8", "8");
    }

    @Test
    public void testFormula() {
        sheetController.selectCell("A2");
        sheetController.selectCell("A1");
        sheetController.insertColumn(getFixture(10));

        sheetController.putCellContent("B1", "=$A$6");
        sheetController.putCellContent("C1", "=A6");
        sheetController.putCellContent("B8", "=$A$6");
        sheetController.putCellContent("C8", "=A6");

        sheetController.selectCell("A3");

        loadServerFixture("INSERT_ROW");

        assertCellValue("B1", "6");
        assertCellValue("C1", "6");
        assertCellValue("B9", "6");
        assertCellValue("C9", "6");
    }

    @Test
    public void testDeleteFormulaReference() {

        sheetController.putCellContent("A3", "42");
        sheetController.putCellContent("C1", "=A3");

        sheetController.selectCell("A4");
        loadServerFixture("DELETE_ROW");
        assertCellValue("A3", "42");
        assertCellValue("C1", "42");

        sheetController.selectCell("A2");
        loadServerFixture("DELETE_ROW");
        sheetController.selectCell("A3");
        assertCellValue("A2", "42");
        assertCellValue("C1", "42");

        sheetController.selectCell("A2");
        loadServerFixture("DELETE_ROW");
        assertCellValue("C1", "#REF!");
    }
}
