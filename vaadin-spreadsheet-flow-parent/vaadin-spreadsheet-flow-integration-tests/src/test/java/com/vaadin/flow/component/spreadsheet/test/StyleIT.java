/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

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
        Assert.assertEquals("center", getCellStyle("A2", "text-align"));

        Assert.assertEquals("right", getCellStyle("B2", "text-align"));

        Assert.assertEquals("rgba(0, 0, 255, 1)",
                getCellStyle("A3", "border-bottom-color"));
        Assert.assertEquals("solid", getCellStyle("A3", "border-bottom-style"));
        Assert.assertEquals("4px", getCellStyle("A3", "border-bottom-width"));

        Assert.assertEquals("rgba(0, 128, 0, 1)",
                getCellStyle("B3", "background-color"));

        Assert.assertEquals("rgba(255, 0, 0, 1)", getCellStyle("A4", "color"));

        Assert.assertEquals("italic", getCellStyle("C4", "font-style"));

        Assert.assertEquals(11,
                (int) Math.ceil(getSize(getCellStyle("A5", "font-size"))));
        Assert.assertEquals(14,
                (int) Math.ceil(getSize(getCellStyle("B5", "font-size"))));
        Assert.assertEquals(16,
                (int) Math.ceil(getSize(getCellStyle("C5", "font-size"))));
        Assert.assertEquals(19,
                (int) Math.ceil(getSize(getCellStyle("D5", "font-size"))));

        Assert.assertEquals("700", getCellStyle("B4", "font-weight"));
    }
}
