package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

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

    @Ignore("Investigate why the action to clear the input is not working")
    @Test
    public void testRenameSheet() {

        Actions actions = new Actions(getDriver());
        actions.doubleClick(findElement(By.xpath(
                "//div[@class='sheet-tabsheet-container']//div[text()='Sheet1']")));
        actions.perform();

        findElement(By.cssSelector(".sheet-tabsheet-tab.selected-tab input"))
                .clear();
        findElement(By.cssSelector(".sheet-tabsheet-tab.selected-tab input"))
                .click();
        // driver.findElement(By.xpath("//*[@class='sheet-tabsheet-container']//input")).click();
        // driver.findElement(
        // By.xpath("//*[@class='sheet-tabsheet-container']//input"))
        findElement(By.cssSelector(".sheet-tabsheet-tab.selected-tab input"))
                .sendKeys(Keys.chord(Keys.CONTROL, Keys.DELETE)
                        + "new sheet name" + Keys.RETURN);

        findElement(By.xpath(
                "//*[@class='sheet-tabsheet-container']//*[text()='new sheet name']"));
        loadTestFixture(TestFixtures.Rename);
        Assert.assertNotNull(driver.findElement(By.xpath(
                "//*[@class='sheet-tabsheet-container']//*[text()='new_sheet_REnamed']")));
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
