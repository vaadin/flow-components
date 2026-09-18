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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class MultipleSheetIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void testMultipleSheet() {
        var spreadsheet = getSpreadsheet();

        findElementInShadowRoot(By.cssSelector("div.col4.row4")).click();

        var cell = (TestBenchElement) findElementInShadowRoot(
                By.cssSelector("div.col6.row6"));
        cell.click(8, 8, Keys.CONTROL);

        spreadsheet.getCellAt("B2").setValue("sheet0 value");
        Assert.assertEquals("sheet0 value", getCellContent("B2"));

        spreadsheet.addSheet();
        spreadsheet.selectSheet("new sheet");

        Assert.assertEquals("", spreadsheet.getCellAt("B2").getValue());
        spreadsheet.getCellAt("B2").setValue("sheet1 value");
        spreadsheet.selectSheet("Sheet1");
        Assert.assertEquals("", spreadsheet.getCellAt("C3").getValue());
        Assert.assertEquals("sheet0 value",
                spreadsheet.getCellAt("B2").getValue());
    }

    @Test
    public void testRenameSheet() {
        // Double-clicking the active tab turns its name into an input.
        new Actions(getDriver()).doubleClick(
                findElementInShadowRoot(By.cssSelector(".sheet-tab.active")))
                .perform();

        // The input opens focused with the old name selected, so typing
        // replaces it. Typed through the keyboard rather than at the
        // element: sending keys to an element focuses it first, and
        // focusing it through the shadow boundary takes the focus off
        // the input, which ends the rename before a single character
        // has arrived.
        Assert.assertNotNull("No rename input on the active tab",
                findElementInShadowRoot(By.cssSelector(".sheet-tab-rename")));
        new Actions(getDriver()).sendKeys("new sheet name" + Keys.RETURN)
                .perform();

        waitUntil(driver -> "new sheet name".equals(getSelectedSheetName()));

        // The server renames sheets too, and the tab follows.
        loadTestFixture(TestFixtures.Rename);
        waitUntil(driver -> !findElementsInShadowRoot(
                By.cssSelector("[title='new_sheet_REnamed']")).isEmpty());
    }

    @Test
    public void testMultipleSheetByAPI() {
        loadTestFixture(TestFixtures.CreateSheet);

        findElementInShadowRoot(By.cssSelector("[title='newSheet1']"));
        findElementInShadowRoot(By.cssSelector("[title='newSheet2']"));
    }

    @Test(expected = NoSuchElementException.class)
    public void multiplySheets_removeSheetBySpreadsheetAPI_sheetIsRemoved() {
        loadTestFixture(TestFixtures.CreateSheet);
        findElementInShadowRoot(By.cssSelector("[title='dontSee']"));
    }

    @Test(expected = NoSuchElementException.class)
    public void multiplySheets_removeSheetByPOI_sheetIsRemoved() {
        loadTestFixture(TestFixtures.CreateSheet);
        findElementInShadowRoot(By.cssSelector("[title='dontSee2']"));
    }

}
