package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.By;

public class CellShiftValuesUndoRedoTest extends AbstractSpreadsheetTestCase {

    @Test
    public void undoRedo_CellShiftValues_ValuesAreUpdatedAsExpectedWithNoErrors() {
        headerPage.createNewSpreadsheet();
        headerPage.loadFile("500x200test.xlsx", this);
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .first();
        SheetCellElement target = spreadsheet.getCellAt("A9");
        Assert.assertEquals("9", target.getValue());

        spreadsheet.getCellAt("A1").click();

        WebElement selectionCorner = spreadsheet.findElement(
                By.className("sheet-selection")).findElement(
                By.className("s-corner"));
        // drag corner element of the selected cell to the target cell
        new Actions(driver).dragAndDrop(selectionCorner, target).perform();

        ensureValueEquals(spreadsheet, "A9", "1");
        undo();
        ensureValueEquals(spreadsheet, "A9", "9");
        redo();
        ensureValueEquals(spreadsheet, "A9", "1");

        assertNoErrorIndicatorDetected();
    }

    private void ensureValueEquals(final SpreadsheetElement spreadsheet,
            final String cellAddress, final String expectedValue) {
        waitUntil(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver arg0) {
                String value = spreadsheet.getCellAt(cellAddress).getValue();
                if (expectedValue != null) {
                    return expectedValue.equals(value);
                }
                return value == null;
            }
        });
    }

    private void undo() {
        new Actions(getDriver()).sendKeys(Keys.chord(Keys.CONTROL, "z"))
                .build().perform();
    }

    private void redo() {
        new Actions(getDriver()).sendKeys(Keys.chord(Keys.CONTROL, "y"))
                .build().perform();
    }

}
