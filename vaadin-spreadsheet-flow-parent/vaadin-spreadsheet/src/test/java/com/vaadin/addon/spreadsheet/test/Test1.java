package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.addon.spreadsheet.test.testutil.ModifierController;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.ComboBoxElement;

public abstract class Test1 extends UITest {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    protected SheetController sheetController;
    protected SheetController ctrl;
    protected ModifierController shift;

    @Before
    public void setUpController() {
        sheetController = keyboardSetup();
        ctrl = new ModifierController(driver, Keys.CONTROL,
                testBench(getDriver()), getDesiredCapabilities());
        shift = new ModifierController(driver, Keys.SHIFT,
                testBench(getDriver()), getDesiredCapabilities());
    }

    protected SheetController keyboardSetup() {
        SheetController c = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
        createNewSheet();
        testBench(driver).waitForVaadin();
        return c;
    }

    protected void createNewSheet() {
        org.openqa.selenium.By newSpreadsheetButton = By
                .id("newSpreadsheetButton");
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions
                .presenceOfElementLocated(newSpreadsheetButton));
        driver.findElement(newSpreadsheetButton).click();
        testBench().waitForVaadin();
    }

    protected void newSheetAndLoadServerFixture(String fixtureName) {
        driver.findElement(By.id("newSpreadsheetButton")).click();
        loadServerFixture(fixtureName);
    }

    protected void loadServerFixture(String fixtureName) {
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions
                .presenceOfElementLocated(By.id("fixtureNameCmb")));
        $(ComboBoxElement.class).id("fixtureNameCmb").selectByText(fixtureName);
        $(ButtonElement.class).id("loadFixtureBtn").click();
    }

    protected void loadSheetFile(String filename) {
        testBenchElement(
                driver.findElement(By.xpath("//*[@id='testSheetSelect']/input")))
                .click(10, 10);
        testBench(driver).waitForVaadin();
        driver.findElement(By.xpath("//*[@id='testSheetSelect']/input"))
                .clear();
        driver.findElement(By.xpath("//*[@id='testSheetSelect']/input"))
                .sendKeys(filename);
        testBench(driver).waitForVaadin();

        driver.findElement(By.xpath("//*[@id='testSheetSelect']/input"))
                .sendKeys(Keys.RETURN);
        testBench(driver).waitForVaadin();

        driver.findElement(By.xpath("//*[@id='update']")).click();
        // driver.findElement(
        // By.xpath(String
        // .format("//span[@class='v-button-caption' and contains(text(), '%s')]",
        // "Update"))).click();
    }

    protected void assertInRange(double from, double value, double to) {
        Assert.assertTrue("Value [" + value + "] is not in range: [" + from
                + " - " + to + "]", value >= from && value <= to);
    }

    protected void assertCellValue(String cell, String text) {
        String actual = sheetController.getCellContent(cell);
        Assert.assertTrue("Failed asserting that cell [" + cell
                + "] contains [" + text + "]. Actual value [" + actual + "]",
                actual.equals(text));
    }

    protected double getSize(String size) {
        return Double.parseDouble(size.replaceAll("[^.0-9]", ""));
    }
}
