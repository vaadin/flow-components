package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.parallel.Browser;

public class RowShiftTests extends AbstractSpreadsheetTestCase {


    private void createRow(int n, int column) {
        for (int i = 0; i < n; i++) {
            spreadsheet.getCellAt(i + 1, column).setValue(Integer.toString(i + 1));
        }
    }
    SpreadsheetElement spreadsheet;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        headerPage.createNewSpreadsheet();
        spreadsheet = $(SpreadsheetElement.class).first();
    }
    @Test
    public void testBasic() {
        createRow(10,1);
        spreadsheet.getCellAt("A5").click();

        headerPage.loadTestFixture(TestFixtures.InsertRow);
        Assert.assertEquals("4",spreadsheet.getCellAt("A4").getValue());
        Assert.assertEquals("",spreadsheet.getCellAt("A5").getValue());
        Assert.assertEquals("5",spreadsheet.getCellAt("A6").getValue());
        spreadsheet.getCellAt("A7").click();
        headerPage.loadTestFixture(TestFixtures.DeleteRow);
        Assert.assertEquals("8",spreadsheet.getCellAt("A8").getValue());
    }

    @Test
    public void testFormula() {
        skipBrowser("Sending multiple keys fails with IE", Browser.IE9, Browser.IE10, Browser.IE11);
        createRow(10,1);
        spreadsheet.getCellAt("B1").setValue("=$A$6");
        spreadsheet.getCellAt("C1").setValue("=A6");
        spreadsheet.getCellAt("B8").setValue("=$A$6");
        spreadsheet.getCellAt("C8").setValue("=A6");

        spreadsheet.getCellAt("A3").click();
        headerPage.loadTestFixture(TestFixtures.InsertRow);

        Assert.assertEquals("6",spreadsheet.getCellAt("B1").getValue());
        Assert.assertEquals("6",spreadsheet.getCellAt("C1").getValue());
        Assert.assertEquals("6",spreadsheet.getCellAt("B9").getValue());
        Assert.assertEquals("6",spreadsheet.getCellAt("C9").getValue());
    }

    @Test
    public void testDeleteFormulaReference() {
        skipBrowser("sheetController.putCellContent() fails with PhantomJS", Browser.PHANTOMJS);
        spreadsheet.getCellAt("A3").setValue("42");
        spreadsheet.getCellAt("C1").setValue("=A3");
        spreadsheet.getCellAt("A4").click();
        headerPage.loadTestFixture(TestFixtures.DeleteRow);

        Assert.assertEquals("42",spreadsheet.getCellAt("A3").getValue());
        Assert.assertEquals("42",spreadsheet.getCellAt("C1").getValue());

        spreadsheet.getCellAt("A2").click();
        headerPage.loadTestFixture(TestFixtures.DeleteRow);
        spreadsheet.getCellAt("A3").click();
        Assert.assertEquals("42",spreadsheet.getCellAt("A2").getValue());
        Assert.assertEquals("42",spreadsheet.getCellAt("C1").getValue());

        spreadsheet.getCellAt("A2").click();
        headerPage.loadTestFixture(TestFixtures.DeleteRow);
        Assert.assertEquals("#REF!",spreadsheet.getCellAt("C1").getValue());
    }
}
