package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.By;
import com.vaadin.testbench.parallel.Browser;

public class MultipleSheetTest extends AbstractSpreadsheetTestCase {

    public void setUp() throws Exception {
        super.setUp();
        headerPage.createNewSpreadsheet();
    }

    @Test
    public void testMultipleSheet() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        driver.findElement(By.xpath("//div[contains(@class,'col4 row4')]"))
                .click();
        testBenchElement(
                driver.findElement(By
                        .xpath("//div[contains(@class, 'col6 row6')]"))).click(
                8, 8, Keys.CONTROL);
        testBench(driver).waitForVaadin();

        spreadsheet.getCellAt("B2").setValue("sheet0 value");
        Assert.assertEquals("sheet0 value",
                sheetController.getCellContent("B2"));

        spreadsheet.addSheet("new sheet");
        spreadsheet.selectSheet("new sheet");

        Assert.assertEquals("", spreadsheet.getCellAt("B2").getValue());
        spreadsheet.getCellAt("B2").setValue("sheet1 value");
        spreadsheet.selectSheet("Sheet1");
        testBench(driver).waitForVaadin();
        Assert.assertEquals("", spreadsheet.getCellAt("C3").getValue());
        Assert.assertEquals("sheet0 value", spreadsheet.getCellAt("B2")
                .getValue());
    }

    @Test
    public void testRenameSheet() {
        skipBrowser("Cannot find the 'new sheet name' element on PhantomJS", Browser.PHANTOMJS);

        Actions actions = new Actions(driver);
        actions.doubleClick(driver.findElement(By
                .xpath("//div[@class='sheet-tabsheet-container']//div[text()='Sheet1']")));
        actions.perform();
        testBench(driver).waitForVaadin();
        driver.findElement(
                By.cssSelector(".sheet-tabsheet-tab.selected-tab input"))
                .clear();
        driver.findElement(
                By.cssSelector(".sheet-tabsheet-tab.selected-tab input"))
                .click();
        // driver.findElement(By.xpath("//*[@class='sheet-tabsheet-container']//input")).click();
        // driver.findElement(
        // By.xpath("//*[@class='sheet-tabsheet-container']//input"))
        driver.findElement(
                By.cssSelector(".sheet-tabsheet-tab.selected-tab input"))
                .sendKeys(
                        Keys.chord(Keys.CONTROL, Keys.DELETE)
                                + "new sheet name" + Keys.RETURN);
        testBench(driver).waitForVaadin();
        driver.findElement(By
                .xpath("//*[@class='sheet-tabsheet-container']//*[text()='new sheet name']"));
        headerPage.loadTestFixture(TestFixtures.Rename);
        Assert.assertNotNull(driver.findElement(By
                .xpath("//*[@class='sheet-tabsheet-container']//*[text()='new_sheet_REnamed']")));
    }

    @Test
    public void testMultipleSheetByAPI() {
        headerPage.loadTestFixture(TestFixtures.CreateSheet);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.findElement(By.xpath("//*[text()='newSheet1']"));
        spreadsheet.findElement(By.xpath("//*[text()='newSheet2']"));
    }

    @Test(expected=NoSuchElementException.class)
    public void multiplySheets_removeSheetBySpreadsheetAPI_sheetIsRemoved() {
        headerPage.loadTestFixture(TestFixtures.CreateSheet);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.findElement(By.xpath("//*[text()='dontSee']"));
    }
    @Test(expected=NoSuchElementException.class)
    public void multiplySheets_removeSheetByPOI_sheetIsRemoved() {
        headerPage.loadTestFixture(TestFixtures.CreateSheet);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.findElement(By.xpath("//*[text()='dontSee2']"));
    }


}
