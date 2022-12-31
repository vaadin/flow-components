package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import org.junit.Assert;

@TestPath("vaadin-spreadsheet")
public class FormulaIT extends AbstractSpreadsheetIT {

    private final String[] integerColumn = { "1", "2", "3", "4" };
    private final String[] mixedColumn = { "1", "3.1415", "example",
            "12-Feb-2007", "", "12/2/2007" };
    private final String[] floatColumn = { "1.11", "2.22", "3.33", "4.44",
            "5.55", "", "", "", "6.66", "7.77" };

    @Before
    public void init() {
        open();
    }

    @Test
    public void testSimpleFormulaSheet() throws Exception {
        loadFile("formulasheet.xlsx");
        selectCell("A2");
        Assert.assertEquals("1", getSpreadsheet().getCellAt("A1").getValue());
        selectCell("A1");
        Assert.assertEquals("1", getFormulaFieldValue());

        Assert.assertEquals("2", getSpreadsheet().getCellAt("B1").getValue());
        selectCell("B1");
        Assert.assertEquals("=A1+1", getFormulaFieldValue());

        Assert.assertEquals("10", getSpreadsheet().getCellAt("C8").getValue());
        selectCell("C8");
        Assert.assertEquals("=C7+1", getFormulaFieldValue());
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
        Assert.assertEquals("=A+2", cellB1.getValue());
    }

    @Test
    public void testGenericFormula() {
        createNewSpreadsheet();
        selectCell("A1");
        insertColumn(integerColumn);

        selectCell("B1");
        setCellValue("B1", "=SUM(A1:A4)");
        setCellValue("B2", "=A1+A2+A3+A4");
        setCellValue("B3", "=PRODUCT(A1:A4)");
        setCellValue("B4", "=A1*A2*A3*A4");

        Assert.assertEquals("10", getCellContent("B1"));
        Assert.assertEquals("10", getCellContent("B2"));
        Assert.assertEquals("24", getCellContent("B3"));
        Assert.assertEquals("24", getCellContent("B4"));
    }

    @Test
    public void testCount() {
        createNewSpreadsheet();
        selectCell("A1");
        insertColumn(mixedColumn);
        selectCell("B1");
        setCellValue("B1", "=COUNT(A1:A4)");
        setCellValue("B2", "=COUNTA(A1:A6)");

        // Date strings must be interpreted as numeric
        Assert.assertEquals("2", getCellValue("B1"));
        Assert.assertEquals("5", getCellValue("B2"));
    }

    @Test
    public void testSubTotals() {
        createNewSpreadsheet();
        setCellValue("A1", "10");
        setCellValue("A2", "20");
        setCellValue("A3", "=SUBTOTAL(9,A1:A2)");
        setCellValue("A4", "30");
        setCellValue("A5", "40");
        setCellValue("A6", "=SUBTOTAL(9,A1:A5)");
        setCellValue("A7", "50");

        setCellValue("B1", "=SUM(A1:A7)");
        setCellValue("B2", "=SUM(A1:A3)");
        setCellValue("B3", "=SUM(A1:A5)");
        setCellValue("B4", "=SUM(A1:A6)");

        setCellValue("C1", "=SUBTOTAL(9,A1:A7)");
        setCellValue("C2", "=SUBTOTAL(9,A1:A3)");
        setCellValue("C3", "=SUBTOTAL(9,A1:A5)");
        setCellValue("C4", "=SUBTOTAL(9,A1:A6)");

        Assert.assertEquals("280", getCellContent("B1"));
        Assert.assertEquals("60", getCellContent("B2"));
        Assert.assertEquals("130", getCellContent("B3"));
        Assert.assertEquals("230", getCellContent("B4"));

        Assert.assertEquals("150", getCellContent("C1"));
        Assert.assertEquals("30", getCellContent("C2"));
        Assert.assertEquals("100", getCellContent("C3"));
        Assert.assertEquals("100", getCellContent("C4"));
    }

    @Test
    public void testRecursiveFormulas() {
        createNewSpreadsheet();
        setCellValue("A1", "10");
        setCellValue("A2", "20");
        setCellValue("A3", "30");

        setCellValue("B1", "=A1+A2");
        setCellValue("B2", "=B1+A3");
        setCellValue("A3", "30");
        Assert.assertEquals("60", getCellValue("B2"));

        // Change a basic cell value and check if other cells are updated.
        setCellValue("A1", "40");
        Assert.assertEquals("90", getCellValue("B2"));
    }

    @Test
    public void testFloatOperations() {
        createNewSpreadsheet();
        for (int i = 0; i < floatColumn.length; i++) {
            getCellAt(1, i + 1).setValue(floatColumn[i]);
        }
        setCellValue("B1", "=SUM(A1:A10)");
        setCellValue("B2", "=COUNT(A1:A10)");
        setCellValue("B3", "=COUNTIF(A1:A10,\">5\")");
        setCellValue("B4", "=SUMIF(A1:A10,\">5\")");
        setCellValue("B5", "=AVERAGE(A1:A10)");
        setCellValue("B6", "=AVERAGE(A1:A2, A4)");

        Assert.assertEquals("31.08", getCellContent("B1"));
        Assert.assertEquals("7", getCellContent("B2"));
        Assert.assertEquals("3", getCellContent("B3"));
        Assert.assertEquals("19.98", getCellContent("B4"));
        Assert.assertEquals("4.44", getCellContent("B5"));
        Assert.assertEquals("2.59", getCellContent("B6"));
    }

    @Test
    public void invalidFormulaIndicatorMouseDown_cellSelected() {
        createNewSpreadsheet();

        setCellValue("A1", "=a");
        selectCell("B1");

        var invalidFormulaIndicator = getCellElement("A1")
                .findElement(By.className("cell-invalidformula-triangle"));
        executeScript(
                "arguments[0].dispatchEvent(new Event('mousedown', {bubbles:true}))",
                invalidFormulaIndicator);

        Assert.assertTrue(isCellSelected("A1"));
    }
}
