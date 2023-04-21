package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet/preserve-on-refresh")
public class PreserveOnRefreshIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void refresh_chartVisible() {
        $("vaadin-button").id("with-charts").click();

        String chartSelector = "vaadin-chart";
        waitUntilComponentVisible(getSpreadsheetElement(), chartSelector);

        getDriver().navigate().refresh();

        waitUntilComponentVisible(getSpreadsheetElement(), chartSelector);
    }

    @Test
    public void refresh_buttonVisible() {
        $("vaadin-button").id("with-button").click();

        SpreadsheetElement spreadsheetElement = getSpreadsheetElement();
        clickCell(spreadsheetElement, "B2");
        waitUntilComponentVisible(spreadsheetElement, "vaadin-button");

        getDriver().navigate().refresh();

        waitUntilComponentVisible(getSpreadsheetElement(), "vaadin-button");
    }

    @Test
    public void setValueToTextField_refresh_textFieldValueRetained() {
        $("vaadin-button").id("with-text-field").click();

        String sampleText = "sample_text";

        SpreadsheetElement spreadsheetElement = getSpreadsheetElement();
        clickCell(spreadsheetElement, "B2");
        TestBenchElement input = spreadsheetElement
                .findElement(By.cssSelector("input"));
        input.click();
        input.sendKeys(sampleText, Keys.ENTER);

        getDriver().navigate().refresh();

        spreadsheetElement = getSpreadsheetElement();
        input = spreadsheetElement.findElement(By.cssSelector("input"));
        waitUntilComponentVisible(spreadsheetElement, "vaadin-text-field");
        Assert.assertEquals(sampleText, input.getAttribute("value"));
    }

    @Test
    public void setSpreadsheetWithButton_setSpreadsheetWithTextField_clickEditor_refresh_noErrors() {
        $("vaadin-button").id("with-button").click();
        $("vaadin-button").id("with-text-field").click();

        clickCell(getSpreadsheetElement(), "B2");

        getDriver().navigate().refresh();

        checkLogsForErrors();
    }

    @Test
    public void refresh_noErrors() {
        getDriver().navigate().refresh();

        checkLogsForErrors();
    }

    private void waitUntilComponentVisible(SpreadsheetElement spreadsheetElement,
            String cssSelector) {
        waitUntil(driver -> spreadsheetElement
                .findElement(By.cssSelector(cssSelector)).isDisplayed());
    }

    private void clickCell(SpreadsheetElement spreadsheetElement,
            String address) {
        SheetCellElement cellElement = spreadsheetElement.getCellAt(address);
        new Actions(getDriver()).moveToElement(cellElement).click().build()
                .perform();
    }

    private SpreadsheetElement getSpreadsheetElement() {
        return $(SpreadsheetElement.class).first();
    }
}
