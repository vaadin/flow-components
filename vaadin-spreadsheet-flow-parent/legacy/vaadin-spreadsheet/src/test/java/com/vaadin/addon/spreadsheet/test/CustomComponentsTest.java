package com.vaadin.addon.spreadsheet.test;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Duration;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Sleeper;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;

public class CustomComponentsTest extends AbstractSpreadsheetTestCase {

    final static String TEXT_PROXY = "text";
    final static Integer NUM_PROXY = 42;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        headerPage.createNewSpreadsheet();
    }
    @Test
    public void testTextField() {
        headerPage.loadTestFixture(TestFixtures.CustomComponent);
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        SheetCellElement b2 = spreadsheet.getCellAt("B2");
        typeInTextFieldEditor(b2, TEXT_PROXY);

        sheetController.putCellContent("B3", "=B2");

        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B2"));
        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B3"));

        testBench(driver).waitForVaadin();
        typeInTextFieldEditor(b2, NUM_PROXY.toString());
        spreadsheet.getCellAt("B3").setValue("=B2*2");

        Assert.assertEquals(NUM_PROXY.toString(), spreadsheet.
                getCellAt("B2").getValue());
        Assert.assertEquals((NUM_PROXY * 2) + "", spreadsheet
                .getCellAt("B3").getValue());
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
                .click().build().perform();
    }

    @Test
    public void testCheckBox() throws InterruptedException {
        headerPage.loadTestFixture(TestFixtures.CustomComponent);

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
        headerPage.loadTestFixture(TestFixtures.CustomComponent);

        sheetController.putCellContent("I3", "=I2*3");

        sheetController.selectCell("I2");
        Select select = new Select(driver.findElement(By.xpath(sheetController
                .cellToXPath("I2") + "//select")));
        select.getOptions().get(3).click();
        testBench(driver).waitForVaadin();

        sheetController.selectCell("G1");

        Assert.assertEquals("120", sheetController.getCellContent("I3"));
    }

    @Test
    public void testScrollingBug() throws InterruptedException {
        headerPage.loadTestFixture(TestFixtures.CustomComponent);

        SheetCellElement b2 = $(SpreadsheetElement.class).first().getCellAt(
                "B2");
        typeInTextFieldEditor(b2, TEXT_PROXY);
        sheetController.selectCell("B5");

        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B2"));
        sheetController.selectCell("B5");
        sheetController.navigateToCell("B100");
        Sleeper.SYSTEM_SLEEPER.sleep(new Duration(1, TimeUnit.SECONDS));
        sheetController.navigateToCell("B1");
        Sleeper.SYSTEM_SLEEPER.sleep(new Duration(3, TimeUnit.SECONDS));

        Assert.assertEquals(TEXT_PROXY, sheetController.getCellContent("B2"));
    }

}
