package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

@TestPath("vaadin-spreadsheet")
public class StyleIT extends AbstractSpreadsheetIT {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    @Ignore("Fails in all browsers, Are POI CellStyles even supported?")
    public void testCssStyleFromFixture() {
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.Styles);
        getCommandExecutor().waitForVaadin();
        assertCorrectCss();
        getCommandExecutor().waitForVaadin();
    }

    @Test
    public void testCssFromUpload() {
        createNewSpreadsheet();
        loadFile("spreadsheet_styles.xlsx");
        assertCorrectCss();
        getCommandExecutor().waitForVaadin();
    }

    private void assertCorrectCss() {
        Assert.assertEquals(getCellStyle("A2", "text-align"), "center");

        Assert.assertEquals(getCellStyle("B2", "text-align"), "right");

        Assert.assertEquals(getCellStyle("A3", "border-bottom-color"),
                "rgba(0, 0, 255, 1)");
        Assert.assertEquals(getCellStyle("A3", "border-bottom-style"), "solid");
        Assert.assertEquals(getCellStyle("A3", "border-bottom-width"), "4px");

        Assert.assertEquals(getCellStyle("B3", "background-color"),
                "rgba(0, 128, 0, 1)");

        Assert.assertEquals(getCellStyle("A4", "color"), "rgba(255, 0, 0, 1)");

        Assert.assertEquals(getCellStyle("C4", "font-style"), "italic");

        Assert.assertEquals(
                (int) Math.ceil(getSize(getCellStyle("A5", "font-size"))), 11);
        Assert.assertEquals(
                (int) Math.ceil(getSize(getCellStyle("B5", "font-size"))), 14);
        Assert.assertEquals(
                (int) Math.ceil(getSize(getCellStyle("C5", "font-size"))), 16);
        Assert.assertEquals(
                (int) Math.ceil(getSize(getCellStyle("D5", "font-size"))), 19);

        Assert.assertEquals(getCellStyle("B4", "font-weight"), "700");
    }
}
