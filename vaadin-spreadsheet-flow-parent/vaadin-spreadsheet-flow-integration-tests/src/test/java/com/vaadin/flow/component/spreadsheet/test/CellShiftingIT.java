/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class CellShiftingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        createNewSpreadsheet();
    }

    @Test
    public void cellValueShifting_verticalShifting_valuesUpdated() {
        String value = "value";
        shiftValue("A1", "A6", value);

        Assert.assertEquals(value, getSpreadsheet().getCellAt("A2").getValue());
    }

    @Test
    public void cellValueShifting_horizontalShifting_valuesUpdated() {
        String value = "value";
        shiftValue("A1", "F1", value);

        Assert.assertEquals(value, getSpreadsheet().getCellAt("B1").getValue());
    }

    @Test
    public void cellValueShifting_horizontalShifting_shiftingIndicatorNotVisible() {
        String value = "value";
        shiftValue("A1", "A6", value);

        SheetCellElement cellA2 = getSpreadsheet().getCellAt("A2");
        Assert.assertEquals(value, cellA2.getValue());
        // open input
        cellA2.doubleClick();
        WebElement shiftSelection = findElementInShadowRoot(
                By.className("paintmode"));
        // verify shifting indicator is not visible (SHEET-62)
        assertFalse(shiftSelection.isDisplayed());
    }

    private void shiftValue(String firstAddress, String lastAddress,
            String value) {
        SheetCellElement firstCell = getSpreadsheet().getCellAt(firstAddress);
        SheetCellElement lastCell = getSpreadsheet().getCellAt(lastAddress);
        firstCell.setValue(value);
        selectCell(firstAddress);
        WebElement shiftHandle = findElementInShadowRoot(
                By.className("s-corner"));
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
