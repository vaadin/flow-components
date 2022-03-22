package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class CellShiftingTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetElement spreadSheet;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        headerPage.createNewSpreadsheet();
        spreadSheet = $(SpreadsheetElement.class).first();
    }

    @Test
    public void cellValueShifting_verticalShifting_valuesUpdated() {
        String value = "value";
        shiftValue("A1", "A6", value);

        assertEquals(value, spreadSheet.getCellAt("A2").getValue());
    }

    @Test
    public void cellValueShifting_horizontalShifting_valuesUpdated() {
        String value = "value";
        shiftValue("A1", "F1", value);

        assertEquals(value, spreadSheet.getCellAt("B1").getValue());
    }

    @Test
    public void cellValueShifting_horizontalShifting_shiftingIndicatorNotVisible() {
        String value = "value";
        shiftValue("A1", "A6", value);

        SheetCellElement cellA2 = spreadSheet.getCellAt("A2");
        assertEquals(value, cellA2.getValue());
        // open input
        cellA2.doubleClick();
        WebElement shiftSelection = spreadSheet.findElement(By
                .className("paintmode"));
        // verify shifting indicator is not visible (SHEET-62)
        assertFalse(shiftSelection.isDisplayed());
    }

    private void shiftValue(String firstAddress, String lastAddress,
            String value) {
        SheetCellElement firstCell = spreadSheet.getCellAt(firstAddress);
        SheetCellElement lastCell = spreadSheet.getCellAt(lastAddress);
        firstCell.setValue(value);
        firstCell.click();
        WebElement shiftHandle = spreadSheet.findElement(By
                .className("s-corner"));
        new Actions(driver).dragAndDrop(shiftHandle, lastCell).perform();
        waitUntilCellHasValue(lastCell, value);
    }

    private void waitUntilCellHasValue(final SheetCellElement cell,
            final String expectedValue) {
        waitUntil(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver arg0) {
                return expectedValue.equals(cell.getValue());
            }

            @Override
            public String toString() {
                return "cell value to be updated";
            }
        });
    }

}
