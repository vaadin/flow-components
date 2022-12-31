package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-spreadsheet")
public class MergeIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void testSelectionBug() {
        final var spreadsheetElement = getSpreadsheet();

        selectRegion("B2", "C3");
        loadTestFixture(TestFixtures.MergeCells);

        // This is needed to workaround some issue that only occurs when running
        // in headless mode. Doesn't affect the actual test.
        selectCell("A1");

        selectRegion("C4", "D3");
        loadTestFixture(TestFixtures.Selection);

        Assert.assertEquals("SELECTED",
                spreadsheetElement.getCellAt("D4").getValue());
        Assert.assertEquals("SELECTED",
                spreadsheetElement.getCellAt("B4").getValue());
        Assert.assertEquals("SELECTED",
                spreadsheetElement.getCellAt("D2").getValue());
    }

    @Test
    public void testBasic() {
        final var spreadsheetElement = getSpreadsheet();
        spreadsheetElement.getCellAt("A1").setValue("1");
        spreadsheetElement.getCellAt("A2").setValue("2");

        spreadsheetElement.getCellAt("B1").setValue("=A1+1");
        spreadsheetElement.getCellAt("B2").setValue("=A2+1");

        selectRegion("A1", "A2");
        loadTestFixture(TestFixtures.MergeCells);
        Assert.assertEquals("2", spreadsheetElement.getCellAt("B1").getValue());
        Assert.assertEquals("3", spreadsheetElement.getCellAt("B2").getValue());

        final var a1 = spreadsheetElement.getCellAt("A1");

        // This is needed to workaround some issue that only occurs when running
        // in headless mode. Doesn't affect the actual test.
        selectCell("A1");

        a1.setValue("10");

        Assert.assertEquals("11",
                spreadsheetElement.getCellAt("B1").getValue());
        Assert.assertEquals("3", spreadsheetElement.getCellAt("B2").getValue());
    }

    @Test
    public void testContents() {
        selectCell("A2");
        setCellValue("A1", "A1 text");
        setCellValue("B1", "B1 text");

        selectRegion("A1", "B1");
        loadTestFixture(TestFixtures.MergeCells);

        Assert.assertEquals("A1 text", getMergedCellContent("A1"));
    }
}
