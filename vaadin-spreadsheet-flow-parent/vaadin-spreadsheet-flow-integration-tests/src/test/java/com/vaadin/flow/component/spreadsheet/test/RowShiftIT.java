package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-spreadsheet")
public class RowShiftIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    private void createRow(int n, int column) {
        for (int i = 0; i < n; i++) {
            getSpreadsheet().getCellAt(i + 1, column)
                    .setValue(Integer.toString(i + 1));
        }
    }

    @Test
    public void testBasic() {
        createRow(10, 1);
        clickCell("A5");

        loadTestFixture(TestFixtures.InsertRow);
        Assert.assertEquals("4", getSpreadsheet().getCellAt("A4").getValue());
        Assert.assertEquals("", getSpreadsheet().getCellAt("A5").getValue());
        Assert.assertEquals("5", getSpreadsheet().getCellAt("A6").getValue());
        clickCell("A7");
        loadTestFixture(TestFixtures.DeleteRow);
        Assert.assertEquals("8", getSpreadsheet().getCellAt("A8").getValue());
    }

    @Test
    public void testFormula() {
        createRow(10, 1);
        getSpreadsheet().getCellAt("B1").setValue("=$A$6");
        getSpreadsheet().getCellAt("C1").setValue("=A6");
        getSpreadsheet().getCellAt("B8").setValue("=$A$6");
        getSpreadsheet().getCellAt("C8").setValue("=A6");

        clickCell("A3");
        loadTestFixture(TestFixtures.InsertRow);

        Assert.assertEquals("6", getSpreadsheet().getCellAt("B1").getValue());
        Assert.assertEquals("6", getSpreadsheet().getCellAt("C1").getValue());
        Assert.assertEquals("6", getSpreadsheet().getCellAt("B9").getValue());
        Assert.assertEquals("6", getSpreadsheet().getCellAt("C9").getValue());
    }

    @Test
    public void testDeleteFormulaReference() {
        getSpreadsheet().getCellAt("A3").setValue("42");
        getSpreadsheet().getCellAt("C1").setValue("=A3");
        clickCell("A4");
        loadTestFixture(TestFixtures.DeleteRow);

        Assert.assertEquals("42", getSpreadsheet().getCellAt("A3").getValue());
        Assert.assertEquals("42", getSpreadsheet().getCellAt("C1").getValue());

        clickCell("A2");
        loadTestFixture(TestFixtures.DeleteRow);
        clickCell("A3");
        Assert.assertEquals("42", getSpreadsheet().getCellAt("A2").getValue());
        Assert.assertEquals("42", getSpreadsheet().getCellAt("C1").getValue());

        clickCell("A2");
        loadTestFixture(TestFixtures.DeleteRow);
        Assert.assertEquals("#REF!",
                getSpreadsheet().getCellAt("C1").getValue());
    }
}
