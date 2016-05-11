package com.vaadin.spreadsheet.charts.interactiontests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.parallel.Browser;

@RunLocally(Browser.FIREFOX)
public class InteractionTestBenchTests extends TestBenchTestCase {

    @Before
    public void setUp() throws Exception {
        // openTestURL();
        setDriver(new FirefoxDriver());
        getDriver()
                .get("http://localhost:8080/SpreadsheetChartsDemoUI#file/InteractionSample.xlsx");
    }

    @Test
    public void dummy() throws Exception {
        SpreadsheetPage spreadsheetPage = new SpreadsheetPage(getDriver());
        setValueForCell(spreadsheetPage, "A14", "10");
        
        Assert.assertTrue(false);
    }

    private void setValueForCell(SpreadsheetPage spreadsheetPage, String cell,
            String value) {
        spreadsheetPage.clickOnCell(cell);
        spreadsheetPage.setFormulaFieldValue(value);
        new Actions(driver).sendKeys(Keys.RETURN).build().perform();
    }
}
