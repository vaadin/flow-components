package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FormulaColumnsIT extends AbstractSpreadsheetIT {

    private final String[] integerColumn = { "1", "2", "3", "4" };
    private final String[] mixedColumn = { "1", "3.1415", "example",
            "12-Feb-2007", "", "12/2/2007" };
    private final String[] floatColumn = { "1.11", "2.22", "3.33", "4.44",
            "5.55", "", "", "", "6.66", "7.77" };

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        createNewSpreadsheet();
    }

    @Test
    public void testGenericFormula() {
        selectCell("A1");
        insertColumn(integerColumn);

        selectCell("B1");
        getSpreadsheet().getCellAt("B1").setValue("=SUM(A1:A4)");
        getSpreadsheet().getCellAt("B2").setValue("=A1+A2+A3+A4");
        getSpreadsheet().getCellAt("B3").setValue("=PRODUCT(A1:A4)");
        getSpreadsheet().getCellAt("B4").setValue("=A1*A2*A3*A4");

        Assert.assertEquals("10", getSpreadsheet().getCellAt("B1").getValue());
        Assert.assertEquals("10", getSpreadsheet().getCellAt("B2").getValue());
        Assert.assertEquals("24", getSpreadsheet().getCellAt("B3").getValue());
        Assert.assertEquals("24", getSpreadsheet().getCellAt("B4").getValue());
    }

    @Test
    public void testCount() {
        selectCell("A1");
        insertColumn(mixedColumn);
        selectCell("B1");
        getSpreadsheet().getCellAt("B1").setValue("=COUNT(A1:A4)");
        getSpreadsheet().getCellAt("B2").setValue("=COUNTA(A1:A6)");

        // Date strings must be interpreted as numeric
        Assert.assertEquals("2", getSpreadsheet().getCellAt("B1").getValue());
        Assert.assertEquals("5", getSpreadsheet().getCellAt("B2").getValue());
    }

    @Test
    public void testSubTotals() {
        getSpreadsheet().getCellAt("A1").setValue("10");
        getSpreadsheet().getCellAt("A2").setValue("20");
        getSpreadsheet().getCellAt("A3").setValue("=SUBTOTAL(9,A1:A2)");
        getSpreadsheet().getCellAt("A4").setValue("30");
        getSpreadsheet().getCellAt("A5").setValue("40");
        getSpreadsheet().getCellAt("A6").setValue("=SUBTOTAL(9,A1:A5)");
        getSpreadsheet().getCellAt("A7").setValue("50");

        getSpreadsheet().getCellAt("B1").setValue("=SUM(A1:A7)");
        getSpreadsheet().getCellAt("B2").setValue("=SUM(A1:A3)");
        getSpreadsheet().getCellAt("B3").setValue("=SUM(A1:A5)");
        getSpreadsheet().getCellAt("B4").setValue("=SUM(A1:A6)");

        getSpreadsheet().getCellAt("C1").setValue("=SUBTOTAL(9,A1:A7)");
        getSpreadsheet().getCellAt("C2").setValue("=SUBTOTAL(9,A1:A3)");
        getSpreadsheet().getCellAt("C3").setValue("=SUBTOTAL(9,A1:A5)");
        getSpreadsheet().getCellAt("C4").setValue("=SUBTOTAL(9,A1:A6)");

        Assert.assertEquals("280", getSpreadsheet().getCellAt("B1").getValue());
        Assert.assertEquals("60", getSpreadsheet().getCellAt("B2").getValue());
        Assert.assertEquals("130", getSpreadsheet().getCellAt("B3").getValue());
        Assert.assertEquals("230", getSpreadsheet().getCellAt("B4").getValue());

        Assert.assertEquals("150", getSpreadsheet().getCellAt("C1").getValue());
        Assert.assertEquals("30", getSpreadsheet().getCellAt("C2").getValue());
        Assert.assertEquals("100", getSpreadsheet().getCellAt("C3").getValue());
        Assert.assertEquals("100", getSpreadsheet().getCellAt("C4").getValue());
    }

    @Test
    public void testRecursiveFormulas() {
        getSpreadsheet().getCellAt("A1").setValue("10");
        getSpreadsheet().getCellAt("A2").setValue("20");
        getSpreadsheet().getCellAt("A3").setValue("30");

        getSpreadsheet().getCellAt("B1").setValue("=A1+A2");
        getSpreadsheet().getCellAt("B2").setValue("=B1+A3");
        getSpreadsheet().getCellAt("A3").setValue("30");
        Assert.assertEquals("60", getSpreadsheet().getCellAt("B2").getValue());

        // Change a basic cell value and check if other cells are updated.
        getSpreadsheet().getCellAt("A1").setValue("40");
        Assert.assertEquals("90", getSpreadsheet().getCellAt("B2").getValue());
    }

    @Test
    @Ignore("Formulas with quotes are not working")
    public void testFloatOperations() {
        for (int i = 0; i < floatColumn.length; i++) {
            getSpreadsheet().getCellAt(i + 1, 1).setValue(floatColumn[i]);
        }
        getSpreadsheet().getCellAt("B1").setValue("=SUM(A1:A10)");
        getSpreadsheet().getCellAt("B2").setValue("=COUNT(A1:A10)");
        // TODO: investigate and fix COUNTIF
        getSpreadsheet().getCellAt("B3").setValue("=COUNTIF(A1:A10,\">5\")");
        // TODO: investigate and fix SUMIF
        getSpreadsheet().getCellAt("B4").setValue("=SUMIF(A1:A10,\">5\")");
        getSpreadsheet().getCellAt("B5").setValue("=AVERAGE(A1:A10)");
        getSpreadsheet().getCellAt("B6").setValue("=AVERAGE(A1:A2, A4)");

        Assert.assertEquals("31.08",
                getSpreadsheet().getCellAt("B1").getValue());
        Assert.assertEquals("7", getSpreadsheet().getCellAt("B2").getValue());
        Assert.assertEquals("3", getSpreadsheet().getCellAt("B3").getValue());
        Assert.assertEquals("19.98",
                getSpreadsheet().getCellAt("B4").getValue());
        Assert.assertEquals("4.44",
                getSpreadsheet().getCellAt("B5").getValue());
        Assert.assertEquals("2.59",
                getSpreadsheet().getCellAt("B6").getValue());
    }

}
