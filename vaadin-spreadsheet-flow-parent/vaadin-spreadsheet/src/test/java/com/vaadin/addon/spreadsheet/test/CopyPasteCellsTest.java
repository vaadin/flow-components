package com.vaadin.addon.spreadsheet.test;

import com.google.common.base.Predicate;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.By;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;

public class CopyPasteCellsTest extends AbstractSpreadsheetTestCase {

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
    public void spreadsheetHandlerOnPaste_PasteCellsWhichOtherCellsDependingOn_UpdatesDependentCells() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.getCellAt("A1").setValue("1");
        spreadsheet.getCellAt("A2").setValue("2");
        spreadsheet.getCellAt("A3").setValue("3");
        spreadsheet.getCellAt("B3").setValue("=A3+1");
        spreadsheet.getCellAt("E4").setValue("=E3+10");

        copyPasteRegion("A3", "B3", "D3", false);
        final String expectedValue = "14";
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E4").getValue().equals(expectedValue);
            }
        });
    }

    @Test
    public void spreadsheetHandlerOnPaste_PasteCells_SmallServerJsonResponse() {
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("500x200test.xlsx", this);
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        final int EXPECTED_JSON_LENGTH_LIMIT = 50;
        spreadsheet.getCellAt("B3").setValue("=A3+1");

        copyPasteRegion("A3", "B3", "D3", true);

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return !getJson().equals("");
            }
        });
        String json = getJson();

        assertLessThanOrEqual("Json size is too big expected= " +
                        EXPECTED_JSON_LENGTH_LIMIT + ", actual = " + json.length(),
                json.length(), EXPECTED_JSON_LENGTH_LIMIT);
    }

    private void copyPasteRegion(String startCopyCell, String endCopyCell, String pasteStartCell, boolean clearLog) {
        sheetController.selectRegion(startCopyCell, endCopyCell);
        copy();
        sheetController.clickCell(pasteStartCell);
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return "D3".equals(sheetController.getSelectedCell());
            }
        });
        if (clearLog) {
            clearLog();
        }
        paste();
    }

    private String getJson() {
        Actions actions = new Actions(getDriver());
        actions.sendKeys(Keys.TAB);
        actions.sendKeys(Keys.SPACE).perform();
        findElement(By.className("v-debugwindow-tab")).click();

        List<WebElement> messages = findElements(By
                .className("v-debugwindow-message"));
        for (WebElement message : messages) {
            if (isUpdateCellValuesJSON(message.getAttribute("innerHTML"))) {
                String text = message.getAttribute("innerHTML");
                int startIndex = text.indexOf('(') + 1;
                int endIndex = text.indexOf(')');
                return text.substring(startIndex, endIndex);
            }
        }
        return "";
    }

    private boolean isUpdateCellValuesJSON(String json) {
        final String RPC_START_SUBSTRING = "Server to client RPC call";
        final String UPDATE_SUBSTRING = "update";
        return json.startsWith(RPC_START_SUBSTRING) && json.toLowerCase().contains(UPDATE_SUBSTRING);
    }


    private void paste() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "v")).build().perform();
    }

    private void copy() {
        new Actions(getDriver())
                .sendKeys(Keys.chord(Keys.CONTROL, "c")).build().perform();
    }


}
