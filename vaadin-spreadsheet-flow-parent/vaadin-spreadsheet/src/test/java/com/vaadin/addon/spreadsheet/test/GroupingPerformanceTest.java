package com.vaadin.addon.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.base.Predicate;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.By;
import com.vaadin.testbench.parallel.BrowserUtil;

public class GroupingPerformanceTest extends AbstractSpreadsheetTestCase {

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private int startMillis = 0;
    private int endMillis = 0;

    @Override
    public void setUp() throws Exception {
        setDebug(true);
        super.setUp();
    }

    @Test
    public void spreadsheetWithGroupings_expandGroup_doesNotReloadEverything()
            throws Exception {
        headerPage.loadFile("grouping_performance.xlsx", this);
        waitForElementPresent(By.className("row-group-pane"));

        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        WebElement rowGrouping = spreadsheetElement.findElement(By
                .cssSelector(".row-group-pane .grouping.plus"));

        clearLog();
        rowGrouping.click();

        waitForElementPresent(By.cssSelector(".row-group-pane .grouping.minus"));

        waitUntil(new Predicate<WebDriver>() {
            private int processingCount;

            @Override
            public boolean apply(WebDriver webDriver) {
                processingCount = 0;
                processingCount += checkDebugLog();
                return processingCount >= 2;
            }

            @Override
            public String toString() {
                return "processing count to reach 2 (was: " + processingCount
                        + ")";
            }
        });
        DesiredCapabilities desiredCapabilities = getDesiredCapabilities();
        Integer expected = 2000;
        if (BrowserUtil.isIE(desiredCapabilities, 11)
                || BrowserUtil.isIE(desiredCapabilities, 10)) {
            expected = 2500;
        }
        Integer time = endMillis - startMillis;
        assertLessThanOrEqual(String.format(
                "Time should be less than %sms, was: %sms", expected, time),
                time, expected);
    }

    private int checkDebugLog() {
        List<WebElement> messages = findElements(By
                .className("v-debugwindow-message"));
        List<WebElement> times = findElements(By
                .className("v-debugwindow-time"));
        int processingCount = 0;
        for (int i = 0; i < messages.size(); ++i) {
            WebElement message = messages.get(i);
            WebElement time = times.get(i);
            String timeString = time.getAttribute("innerHTML");
            timeString = timeString.substring(0, timeString.length() - 2);
            if (startMillis == 0) {
                startMillis = Integer.valueOf(timeString);
            }
            String json = message.getAttribute("innerHTML");
            if (isLoadCellDataLogJSON(json)) {
                Assert.fail("Cell data was loaded when it was unnecessary to do so.");
            }
            if (isProcessingTimeLogJSON(json)) {
                ++processingCount;
                endMillis = Integer.valueOf(timeString);
            }
        }
        return processingCount;
    }

    private boolean isLoadCellDataLogJSON(String json) {
        final String LOAD_CELL_DATA_RPC_SUBSTRING = "updateBottomRightCellValues";
        return json.contains(LOAD_CELL_DATA_RPC_SUBSTRING);
    }

    private boolean isProcessingTimeLogJSON(String json) {
        final String PROCESSING_TIME_SUBSCRING = "Processing time was ";
        return json.contains(PROCESSING_TIME_SUBSCRING);
    }

}
