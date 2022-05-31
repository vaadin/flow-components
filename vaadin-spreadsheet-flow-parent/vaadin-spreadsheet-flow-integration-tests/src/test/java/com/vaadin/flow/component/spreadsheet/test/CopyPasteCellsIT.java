package com.vaadin.flow.component.spreadsheet.test;

import java.util.List;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class CopyPasteCellsIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void spreadsheetHandlerOnPaste_PasteCellsWhichOtherCellsDependingOn_UpdatesDependentCells() {
        createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .first();
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
                return spreadsheet.getCellAt("E4").getValue()
                        .equals(expectedValue);
            }
        });
    }

    private void copyPasteRegion(String startCopyCell, String endCopyCell,
            String pasteStartCell, boolean clearLog) {
        selectRegion(startCopyCell, endCopyCell);
        copy();
        clickCell(pasteStartCell);

        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return "D3".equals(getSelectionFormula());
            }
        });
        paste();
    }

    private String getJson() {
        Actions actions = new Actions(getDriver());
        actions.sendKeys(Keys.TAB);
        actions.sendKeys(Keys.SPACE).perform();
        findElement(By.className("v-debugwindow-tab")).click();

        List<WebElement> messages = findElements(
                By.className("v-debugwindow-message"));
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
        return json.startsWith(RPC_START_SUBSTRING)
                && json.toLowerCase().contains(UPDATE_SUBSTRING);
    }
}
