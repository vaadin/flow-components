package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.testbench.By;

/**
 * Test for spreadsheets that have both hidden and frozen rows/columns.
 *
 */
public class HiddenAndFrozenTest extends AbstractSpreadsheetTestCase {

    @Test
    public void freezePane_sheetWithHiddenAndFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("hidden_and_frozen.xlsx", 11, 9,
                "O15");
    }

    @Test
    public void freezePane_sheetWithMoreHiddenThanFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("more_hidden_than_frozen.xlsx", 1,
                1, "E7");
    }

    @Test
    public void freezePane_sheetWithRandomHiddenAndFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("randomly_hidden_and_frozen.xlsx",
                5, 3, "H15");
    }
    
    @Test
    public void freezePane_sheetWithImplicitlyHiddenFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        assertFreezePanePositionedCorrectly("scrolled_frozen.xlsx", 2, 4, "G5");
    }

    private void assertFreezePanePositionedCorrectly(String filename,
            int frozenRows, int frozenColumns, String regularCell) {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(filename, this);
        WebElement topLeft = spreadsheetPage.findElement(By
                .className("top-left-pane"));

        assertVisibleChildCount(topLeft, "rh", frozenRows);
        assertVisibleChildCount(topLeft, "ch", frozenColumns);
        assertEquals("First regular cell not where it's supposed to be,",
                "regular", spreadsheetPage.getCellValue(regularCell));
    }

    private void assertVisibleChildCount(WebElement parent,
            String childClassName, int expectedChildCount) {
        List<WebElement> childElements = parent.findElements(By
                .className(childClassName));
        int actualChildCount = 0;
        for (WebElement childElement : childElements) {
            if ("none".equals(childElement.getCssValue("display"))
                    || "0px".equals(childElement.getCssValue("height"))) {
                continue;
            }
            ++actualChildCount;
        }

        assertEquals(
                String.format("Unexpected child count (%s),", childClassName),
                expectedChildCount, actualChildCount);
    }
}
