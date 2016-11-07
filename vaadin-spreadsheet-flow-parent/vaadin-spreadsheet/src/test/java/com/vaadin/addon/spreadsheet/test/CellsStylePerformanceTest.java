package com.vaadin.addon.spreadsheet.test;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.google.common.base.Predicate;
import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.By;

public class CellsStylePerformanceTest extends AbstractSpreadsheetTestCase {

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private SheetController sheetController;


    @Override
    public void setUp() throws Exception {
        setDebug(true);
        super.setUp();
        sheetController = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
    }

    @Test
    public void spreadsheetWithManyStyles_setValueInCell_styleUpdateTimeLessThanASecond() {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile("cell_styles_performance.xlsx", this);
        clearLog();
        SheetCellElement cell = spreadsheetPage.getCellAt(1, 1);
        cell.setValue("foo");
        Integer expected = 1000;

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return !getJson().equals("");
            }
        });
        Integer time = Integer.parseInt(getJson());
        assertLessThanOrEqual(String.format(
                "Time should be less than %sms, was: %sms", expected, time),
                time, expected);
    }


    private String getJson() {
        Actions actions = new Actions(getDriver());
        actions.sendKeys(Keys.TAB);
        actions.sendKeys(Keys.SPACE).perform();
        findElement(By.className("v-debugwindow-tab")).click();
        List<WebElement> messages = findElements(By
                .className("v-debugwindow-message"));
        for (WebElement message : messages) {
            if (isStyleUpdateLogJSON(message.getAttribute("innerHTML"))) {
                String json = message.getAttribute("innerHTML");
                String timeString = json.replaceFirst("Style update took:", "").replaceFirst("ms", "");
                return timeString;
            }
        }
        return "";
    }

    private boolean isStyleUpdateLogJSON(String json) {
        final String RPC_START_SUBSTRING = "Style update took:";
        return json.startsWith(RPC_START_SUBSTRING);
    }

}
