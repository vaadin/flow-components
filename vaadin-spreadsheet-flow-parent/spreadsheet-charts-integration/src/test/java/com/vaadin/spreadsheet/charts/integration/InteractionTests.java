package com.vaadin.spreadsheet.charts.integration;

import com.vaadin.testbench.TestBenchTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

public class InteractionTests extends TestBenchTestCase {

    @Before
    public void setUp() throws Exception {
        // openTestURL();
        setDriver(new ChromeDriver());
        getDriver()
                .get("http://localhost:9998/SpreadsheetChartsDemoUI#file/TypeSample%20-%20Scatter.xlsx");
    }

    @Test
    public void dummy() throws Exception {
        SpreadsheetPage spreadsheetPage = new SpreadsheetPage(getDriver());
        setValueForCell(spreadsheetPage, "C1", "258");
        Assert.assertTrue(true);
    }

    private void setValueForCell(SpreadsheetPage spreadsheetPage, String cell,
            String value) {
        spreadsheetPage.clickOnCell(cell);
        spreadsheetPage.setFormulaFieldValue(value);
        new Actions(driver).sendKeys(Keys.RETURN).build().perform();
    }
}
