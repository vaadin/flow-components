package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

public class FormulaTest2 extends Test1 {

    private final String[] integerColumn = { "1", "2", "3", "4" };
    private final String[] mixedColumn = { "1", "3.1415", "example",
            "12-Feb-2007", "", "12/2/2007" };
    private final String[] floatColumn = { "1.11", "2.22", "3.33", "4.44",
            "5.55", "", "", "", "6.66", "7.77" };

    @Test
    public void testGenericFormula() {

        c.insertColumn(integerColumn);
        c.selectCell("B1");

        c.insertAndRet("=SUM(A1:A4)").insertAndRet("=A1+A2+A3+A4")
                .insertAndRet("=PRODUCT(A1:A4)").insertAndRet("=A1*A2*A3*A4");

        testBench(driver).waitForVaadin();

        Assert.assertEquals("10", c.getCellContent("B1"));
        Assert.assertEquals("10", c.getCellContent("B2"));
        Assert.assertEquals("24", c.getCellContent("B3"));
        Assert.assertEquals("24", c.getCellContent("B4"));
    }

    @Test
    public void testCount() {

        c.insertColumn(mixedColumn);
        c.selectCell("B1");
        c.insertAndRet("=COUNT(A1:A4)").insertAndRet("=COUNTA(A1:A6)");

        testBench(driver).waitForVaadin();

        // Date strings must be interpreted as numeric
        Assert.assertEquals("2", c.getCellContent("B1")); // Rev 16: actual
                                                          // value:2
        Assert.assertEquals("5", c.getCellContent("B2"));
    }

    @Test
    public void testSubTotals() {

        c.insertColumn(new String[] { "10", "20", "=SUBTOTAL(9,A1:A2)", "30",
                "40", "=SUBTOTAL(9,A1:A5)", "50" });
        c.selectCell("B1");
        c.insertColumn(new String[] { "=SUM(A1:A7)", "=SUM(A1:A3)",
                "=SUM(A1:A5)", "=SUM(A1:A6)" });
        c.selectCell("C1");
        c.insertColumn(new String[] { "=SUBTOTAL(9,A1:A7)",
                "=SUBTOTAL(9,A1:A3)", "=SUBTOTAL(9,A1:A5)",
                "=SUBTOTAL(9,A1:A6)" });

        Assert.assertEquals("280", c.getCellContent("B1"));
        Assert.assertEquals("60", c.getCellContent("B2"));
        Assert.assertEquals("130", c.getCellContent("B3"));
        Assert.assertEquals("230", c.getCellContent("B4"));

        Assert.assertEquals("150", c.getCellContent("C1"));
        Assert.assertEquals("30", c.getCellContent("C2"));
        Assert.assertEquals("100", c.getCellContent("C3"));
        Assert.assertEquals("100", c.getCellContent("C4"));
    }

    @Test
    public void testRecursiveFormulas() {
        c.insertColumn(new String[] { "10", "20", "30" });
        c.selectCell("B1");
        c.insertColumn(new String[] { "=A1+A2", "=B1+A3" });

        Assert.assertEquals("60", c.getCellContent("B2"));

        // Change a basic cell value and check if other cells are updated.
        c.selectCell("A1");
        c.insertAndRet("40");
        Assert.assertEquals("90", c.getCellContent("B2"));
    }

    @Test
    public void testFloatOperations() {
        c.insertColumn(floatColumn);
        c.selectCell("B1");
        c.insertAndRet("=SUM(A1:A10)").insertAndRet("=COUNT(A1:A10)")
                .insertAndRet("=COUNTIF(A1:A10,\">5\")")
                .insertAndRet("=SUMIF(A1:A10,\">5\")")
                .insertAndRet("=AVERAGE(A1:A10)")
                .insertAndRet("=AVERAGE(A1:A2, A4)");

        testBench(driver).waitForVaadin();

        Assert.assertEquals("31.08", c.getCellContent("B1"));
        Assert.assertEquals("7", c.getCellContent("B2"));
        Assert.assertEquals("3", c.getCellContent("B3"));
        Assert.assertEquals("19.98", c.getCellContent("B4"));
        Assert.assertEquals("4.44", c.getCellContent("B5"));
        Assert.assertEquals("2.59", c.getCellContent("B6"));
    }

}
