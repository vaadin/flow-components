package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.By;

public class MultipleSheetTest extends Test1 {

    @Test
    public void testMultipleSheet() {

        driver.findElement(By.xpath("//div[contains(@class,'col4 row4')]"))
                .click();
        testBenchElement(
                driver.findElement(By
                        .xpath("//div[contains(@class, 'col6 row6')]"))).click(
                8, 8, Keys.CONTROL);
        testBench(driver).waitForVaadin();
        sheetController.putCellContent("B2", "sheet0 value");
        Assert.assertEquals("sheet0 value",
                sheetController.getCellContent("B2"));

        driver.findElement(
                By.xpath("//*[@id='spreadsheetId']//*[@class='add-new-tab']"))
                .click();
        testBench(driver).waitForVaadin();
        driver.findElement(By
                .xpath("//*[@id='spreadsheetId']//*[text()='Sheet1']"));
        testBench(driver).waitForVaadin();
        Assert.assertEquals("", sheetController.getCellContent("B2"));

        sheetController.putCellContent("C3", "sheet1 value");
        Assert.assertEquals("sheet1 value",
                sheetController.getCellContent("C3"));

        driver.findElement(
                By.xpath("//*[@id='spreadsheetId']//*[text()='Sheet1']"))
                .click();
        testBench(driver).waitForVaadin();
        Assert.assertEquals("", sheetController.getCellContent("C3"));
        Assert.assertEquals("sheet0 value",
                sheetController.getCellContent("B2"));
    }

    @Test
    public void testRenameSheet() {
        createNewSheet();
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
        loadServerFixture("SHEET_RENAME_1");
        Assert.assertNotNull(driver.findElement(By
                .xpath("//*[@class='sheet-tabsheet-container']//*[text()='new_sheet_REnamed']")));
    }

    @Test
    public void testMultipleSheetByAPI() {
        newSheetAndLoadServerFixture("SHEETS");

        driver.findElement(By
                .xpath("//*[@id='spreadsheetId']//*[text()='newSheet1']"));
        driver.findElement(By
                .xpath("//*[@id='spreadsheetId']//*[text()='newSheet2']"));
        try {
            driver.findElement(By
                    .xpath("//*[@id='spreadsheetId']//*[text()='dontSee']"));
            fail();
        } catch (NoSuchElementException exception) {
        }

        try {
            driver.findElement(By
                    .xpath("//*[@id='spreadsheetId']//*[text()='dontSee2']"));
            fail();
        } catch (NoSuchElementException exception) {
        }
    }

}
