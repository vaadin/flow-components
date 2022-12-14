package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-spreadsheet/preserve-on-refresh")
public class PreserveOnRefreshIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void refresh_chartVisible() {
        assertChartVisible();

        getDriver().navigate().refresh();

        assertChartVisible();
    }

    @Test
    public void refresh_noErrors() {
        getDriver().navigate().refresh();

        checkLogsForErrors();
    }

    private void assertChartVisible() {
        Assert.assertTrue($(SpreadsheetElement.class).first()
                .findElement(By.cssSelector("vaadin-chart")).isDisplayed());
    }
}
