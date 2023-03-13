package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

@TestPath("vaadin-spreadsheet")
public class CellShiftValuesUndoRedoIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        createNewSpreadsheet();
    }

    @Test
    public void undoRedo_CellShiftValues_ValuesAreUpdatedAsExpectedWithNoErrors() {
        loadFile("500x200test.xlsx");
        SheetCellElement target = getSpreadsheet().getCellAt("A9");
        Assert.assertEquals("9", target.getValue());

        selectCell("A1");

        WebElement selectionCorner = findElementInShadowRoot(
                By.className("sheet-selection"))
                .findElement(By.className("s-corner"));
        // drag corner element of the selected cell to the target cell
        new Actions(driver).dragAndDrop(selectionCorner, target).perform();

        ensureValueEquals(getSpreadsheet(), "A9", "1");
        undo();
        ensureValueEquals(getSpreadsheet(), "A9", "9");
        redo();
        ensureValueEquals(getSpreadsheet(), "A9", "1");

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

}
