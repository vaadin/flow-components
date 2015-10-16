package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.server.browserlaunchers.Sleeper;
import org.openqa.selenium.support.ui.Select;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class CustomComponentsTest extends Test1 {

    final static String TEXT_PROXY = "text";
    final static Integer NUM_PROXY = 42;

    @Test
    public void testTextField() {
        loadServerFixture("CUSTOM_COMPONENTS");

        SheetCellElement b2 = $(SpreadsheetElement.class).first().getCellAt(
                "B2");
        typeInTextFieldEditor(b2, TEXT_PROXY);

        sheetController.putCellContent("B3", "=B2");

        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B2"));
        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B3"));

        typeInTextFieldEditor(b2, NUM_PROXY.toString());

        sheetController.putCellContent("B3", "=B2*2");

        Assert.assertEquals(NUM_PROXY.toString(),
                sheetController.getCellContent("B2"));
        Assert.assertEquals((NUM_PROXY * 2) + "",
                sheetController.getCellContent("B3"));
    }

    private void typeInTextFieldEditor(SheetCellElement cell, String text) {
        activateEditorInCell(cell);
        cell.findElement(By.xpath("./input")).clear();
        activateEditorInCell(cell);
        cell.findElement(By.xpath("./input")).sendKeys(text, Keys.RETURN);
    }

    private void activateEditorInCell(SheetCellElement cell) {
        cell.click();
        new Actions(getDriver()).moveToElement(cell).moveByOffset(7, 7)
                .doubleClick().build().perform();
    }

    @Test
    public void testCheckBox() throws InterruptedException {
        loadServerFixture("CUSTOM_COMPONENTS");

        sheetController.putCellContent("C3", "=C2*2");
        sheetController.putCellContent("C4", "=IF(C2,1,0)");

        sheetController.selectCell("A1");

        Assert.assertEquals("0", sheetController.getCellContent("C3"));
        Assert.assertEquals("0", sheetController.getCellContent("C4"));

        SheetCellElement c2 = $(SpreadsheetElement.class).first().getCellAt(
                "C2");
        c2.click();
        new Actions(getDriver())
                .moveToElement(c2.findElement(By.xpath(".//input"))).click()
                .build().perform();

        sheetController.selectCell("A1");

        Assert.assertEquals("2", sheetController.getCellContent("C3"));
        Assert.assertEquals("1", sheetController.getCellContent("C4"));
    }

    @Test
    public void testNativeSelect() {
        loadServerFixture("CUSTOM_COMPONENTS");

        sheetController.putCellContent("I3", "=I2*3");

        sheetController.selectCell("I2");
        Select select = new Select(driver.findElement(By.xpath(sheetController
                .cellToXPath("I2") + "//select")));
        select.getOptions().get(3).click();
        testBench(driver).waitForVaadin();

        sheetController.selectCell("G1");

        Assert.assertEquals("90", sheetController.getCellContent("I3"));
    }

    @Test
    public void testScrollingBug() throws InterruptedException {
        loadServerFixture("CUSTOM_COMPONENTS");

        SheetCellElement b2 = $(SpreadsheetElement.class).first().getCellAt(
                "B2");
        typeInTextFieldEditor(b2, TEXT_PROXY);
        sheetController.selectCell("B5");

        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B2"));
        sheetController.selectCell("B5");
        sheetController.navigateToCell("B100");

        Sleeper.sleepTightInSeconds(1);
        sheetController.navigateToCell("B1");
        Sleeper.sleepTightInSeconds(3);

        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B2"));
    }

    @Test
    public void testButtonHandling() {
        loadServerFixture("CUSTOM_COMPONENTS");

        driver.findElement(By.id("b10-btn")).click();
        testBench(driver).waitForVaadin();
        Assert.assertEquals("42", sheetController.getCellContent("B11"));
        Assert.assertEquals("b12", driver.findElement(By.id("b12-label"))
                .getText());
    }

}
