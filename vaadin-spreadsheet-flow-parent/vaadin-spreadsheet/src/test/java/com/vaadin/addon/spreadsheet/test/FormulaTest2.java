package com.vaadin.addon.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.parallel.Browser;

public class FormulaTest2 extends Test1 {

    private final String[] integerColumn = { "1", "2", "3", "4" };
    private final String[] mixedColumn = { "1", "3.1415", "example",
            "12-Feb-2007", "", "12/2/2007" };
    private final String[] floatColumn = { "1.11", "2.22", "3.33", "4.44",
            "5.55", "", "", "", "6.66", "7.77" };

    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowsersToTest() {
        // PhantomJS doesn't support right-click
        return getBrowserCapabilities(Browser.FIREFOX, Browser.CHROME);
    }

    @Test
    public void testGenericFormula() {
        sheetController.selectCell("A1");
        sheetController.insertColumn(integerColumn);
        sheetController.selectCell("B1");

        sheetController.insertAndRet("=SUM(A1:A4)")
                .insertAndRet("=A1+A2+A3+A4").insertAndRet("=PRODUCT(A1:A4)")
                .insertAndRet("=A1*A2*A3*A4");

        Assert.assertEquals("10", sheetController.getCellContent("B1"));
        Assert.assertEquals("10", sheetController.getCellContent("B2"));
        Assert.assertEquals("24", sheetController.getCellContent("B3"));
        Assert.assertEquals("24", sheetController.getCellContent("B4"));
    }

    @Test
    public void testCount() {
        sheetController.selectCell("A1");
        sheetController.insertColumn(mixedColumn);
        sheetController.selectCell("B1");
        sheetController.insertAndRet("=COUNT(A1:A4)").insertAndRet(
                "=COUNTA(A1:A6)");

        testBench(driver).waitForVaadin();

        // Date strings must be interpreted as numeric
        Assert.assertEquals("2", sheetController.getCellContent("B1")); // Rev
                                                                        // 16:
                                                                        // actual
        // value:2
        Assert.assertEquals("5", sheetController.getCellContent("B2"));
    }

    @Test
    public void testSubTotals() {
        sheetController.selectCell("A1");
        sheetController.insertColumn(new String[] { "10", "20",
                "=SUBTOTAL(9,A1:A2)", "30", "40", "=SUBTOTAL(9,A1:A5)", "50" });
        sheetController.selectCell("B1");
        sheetController.insertColumn(new String[] { "=SUM(A1:A7)",
                "=SUM(A1:A3)", "=SUM(A1:A5)", "=SUM(A1:A6)" });
        sheetController.selectCell("C1");
        sheetController.insertColumn(new String[] { "=SUBTOTAL(9,A1:A7)",
                "=SUBTOTAL(9,A1:A3)", "=SUBTOTAL(9,A1:A5)",
                "=SUBTOTAL(9,A1:A6)" });

        Assert.assertEquals("280", sheetController.getCellContent("B1"));
        Assert.assertEquals("60", sheetController.getCellContent("B2"));
        Assert.assertEquals("130", sheetController.getCellContent("B3"));
        Assert.assertEquals("230", sheetController.getCellContent("B4"));

        Assert.assertEquals("150", sheetController.getCellContent("C1"));
        Assert.assertEquals("30", sheetController.getCellContent("C2"));
        Assert.assertEquals("100", sheetController.getCellContent("C3"));
        Assert.assertEquals("100", sheetController.getCellContent("C4"));
    }

    @Test
    public void testRecursiveFormulas() {
        sheetController.selectCell("A1");
        sheetController.insertColumn(new String[] { "10", "20", "30" });
        sheetController.selectCell("B1");
        sheetController.insertColumn(new String[] { "=A1+A2", "=B1+A3" });

        Assert.assertEquals("60", sheetController.getCellContent("B2"));

        // Change a basic cell value and check if other cells are updated.
        sheetController.selectCell("A1");
        sheetController.insertAndRet("40");
        Assert.assertEquals("90", sheetController.getCellContent("B2"));
    }

    @Test
    public void testFloatOperations() {
        sheetController.selectCell("A1");
        sheetController.insertColumn(floatColumn);
        sheetController.selectCell("B1");
        sheetController.insertAndRet("=SUM(A1:A10)")
                .insertAndRet("=COUNT(A1:A10)")
                .insertAndRet("=COUNTIF(A1:A10,\">5\")")
                .insertAndRet("=SUMIF(A1:A10,\">5\")")
                .insertAndRet("=AVERAGE(A1:A10)")
                .insertAndRet("=AVERAGE(A1:A2, A4)");

        testBench(driver).waitForVaadin();

        Assert.assertEquals("31.08", sheetController.getCellContent("B1"));
        Assert.assertEquals("7", sheetController.getCellContent("B2"));
        Assert.assertEquals("3", sheetController.getCellContent("B3"));
        Assert.assertEquals("19.98", sheetController.getCellContent("B4"));
        Assert.assertEquals("4.44", sheetController.getCellContent("B5"));
        Assert.assertEquals("2.59", sheetController.getCellContent("B6"));
    }

}
