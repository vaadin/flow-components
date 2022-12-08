package com.vaadin.flow.component.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

/**
 * Test for spreadsheets that have both hidden and frozen rows/columns.
 *
 */
@TestPath("vaadin-spreadsheet")
public class HiddenAndFrozenIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void freezePane_sheetWithHiddenAndFrozenRowsAndColumns_freezePanePositionedCorrectly() {
        getDriver().manage().window().maximize();
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
        loadFile(filename);
        WebElement topLeft = findElementInShadowRoot(
                By.className("top-left-pane"));

        assertVisibleChildCount(topLeft, "rh", frozenRows);
        assertVisibleChildCount(topLeft, "ch", frozenColumns);
        Assert.assertEquals("First regular cell not where it's supposed to be,",
                "regular", getCellValue(regularCell));
    }

    private void assertVisibleChildCount(WebElement parent,
            String childClassName, int expectedChildCount) {
        List<WebElement> childElements = parent
                .findElements(By.className(childClassName));
        int actualChildCount = 0;
        for (WebElement childElement : childElements) {
            if ("none".equals(childElement.getCssValue("display"))
                    || "0px".equals(childElement.getCssValue("height"))) {
                continue;
            }
            ++actualChildCount;
        }

        Assert.assertEquals(
                String.format("Unexpected child count (%s),", childClassName),
                expectedChildCount, actualChildCount);
    }
}
