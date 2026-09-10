/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class StylingPartsIT extends AbstractSpreadsheetIT {

    private static final String SCROLL_TABS_BUTTON = "scroll-tabs-button";
    private static final String SCROLL_TABS_BUTTON_DISABLED = "scroll-tabs-button-disabled";
    private static final String NEW_TAB_BUTTON = "new-tab-button";
    private static final String SCROLL_TABS_TO_END_BUTTON = "scroll-tabs-to-end-button";
    private static final String SCROLL_TABS_FORWARD_BUTTON = "scroll-tabs-forward-button";
    private static final String SCROLL_TABS_BACKWARD_BUTTON = "scroll-tabs-backward-button";
    private static final String SCROLL_TABS_TO_START_BUTTON = "scroll-tabs-to-start-button";

    @Before
    public void init() {
        open();

        createNewSpreadsheet();
    }

    @Test
    public void cellStyling_partAttributeExists() {
        var spreadsheet = getSpreadsheet();

        var cell = spreadsheet.getCellAt("B2");
        assertPartEquals(cell, "cell");

        var columnHeader = spreadsheet.getColumnHeader(1);
        assertPartContains(columnHeader, "column-header");

        var rowHeader = spreadsheet.getRowHeader(1);
        assertPartContains(rowHeader, "row-header");

        var addressField = spreadsheet.getAddressField();
        assertPartEquals(addressField, "address-field");

        var formulaField = spreadsheet.getFormulaField();
        assertPartEquals(formulaField, "formula-field");

        var tab = findElementInShadowRoot(By.className("sheet-tab"));
        assertPartContains(tab, "tab");

        //@formatter:off
        var scrollTabButtons = Map.of(
                "scroll-tabs-beginning", SCROLL_TABS_TO_START_BUTTON,
                "scroll-tabs-left", SCROLL_TABS_BACKWARD_BUTTON,
                "scroll-tabs-right", SCROLL_TABS_FORWARD_BUTTON,
                "scroll-tabs-end", SCROLL_TABS_TO_END_BUTTON,
                "add-new-tab", NEW_TAB_BUTTON
        );
        //@formatter:on

        for (var entry : scrollTabButtons.entrySet()) {
            var button = findElementInShadowRoot(By.className(entry.getKey()));
            assertPartContains(button, entry.getValue());
        }

        var selectionCorner = findElementInShadowRoot(
                By.className("selection-corner"));
        assertPartEquals(selectionCorner, "selection-corner");

        var cellInput = getInlineEditor("A1");
        assertPartEquals(cellInput, "cell-input");
    }

    @Test
    public void rangeSelection_partAttributeExists() {
        selectRegion("H3", "I5");
        var spreadsheet = getSpreadsheet();

        // Verify selected headers and cells in range
        for (int row = 3; row <= 5; row++) {
            assertPartEquals(spreadsheet.getRowHeader(row), "row-header",
                    "header-selected");
            for (int col = 8; col <= 9; col++) {
                var cell = spreadsheet.getCellAt(row, col);
                if (row == 3 && col == 8) {
                    // The top-left cell of the range does not have the
                    // cell-range part
                    assertPartEquals(cell, "cell");
                    continue;
                }
                assertPartContains(cell, "cell-range");
            }
        }
        for (int col = 8; col <= 9; col++) {
            assertPartEquals(spreadsheet.getColumnHeader(col), "column-header",
                    "header-selected");
        }

        // Clear selection and verify parts are removed
        clickCell("A1");

        for (int row = 3; row <= 5; row++) {
            assertPartEquals(spreadsheet.getRowHeader(row), "row-header");
            for (int col = 8; col <= 9; col++) {
                var cell = spreadsheet.getCellAt(row, col);
                assertPartDoesNotContain(cell, "cell-range");
            }
        }
        for (int col = 8; col <= 9; col++) {
            assertPartEquals(spreadsheet.getColumnHeader(col), "column-header");
        }
    }

    @Test
    public void cellWithComment_partAttributeExists() {
        loadTestFixture(TestFixtures.Comments);
        var spreadsheet = getSpreadsheet();
        var firstCell = spreadsheet.getCellAt("A1");

        // The indicator is a sibling of the cell rather than a child, so
        // it is found through the sheet rather than under the cell.
        var commentTriangle = findElementInShadowRoot(
                By.cssSelector("[part~=\"comment-triangle\"]"));
        assertPartEquals(commentTriangle, "comment-triangle");
    }

    @Test
    public void cellWithFormulaError_partAttributeExists() {
        var spreadsheet = getSpreadsheet();
        var errorCell = spreadsheet.getCellAt("A1");
        // Set a formula that produces an error
        errorCell.setValue("=XYZ");

        waitUntil(d -> !errorCell
                .findElements(By.className("invalidformula-indicator"))
                .isEmpty());
        var errorIndicator = errorCell
                .findElement(By.className("invalidformula-indicator"));
        assertPartEquals(errorIndicator, "invalid-triangle");
    }

    @Test
    public void popupButton_partAttributeExists() {
        loadTestFixture(TestFixtures.SpreadsheetTable);
        var spreadsheet = getSpreadsheet();
        var popupButtonCell = spreadsheet.getCellAt("B2");
        var popupButton = findElementInShadowRoot(
                By.cssSelector("[part~=\"popup-button\"]"));
        assertPartEquals(popupButton, "popup-button");
    }

    @Test
    public void sheetTab_partAttributeExists() {
        var spreadsheet = getSpreadsheet();
        spreadsheet.addSheet();

        var sheetTabs = findElementsInShadowRoot(By.className("sheet-tab"));

        assertPartEquals(sheetTabs.get(0), "tab");
        assertPartEquals(sheetTabs.get(1), "tab", "tab-selected");
    }

    @Test
    public void scrollTabs_partAttributeExists() {
        loadFile("ConditionalFormatterSamples.xlsx");
        getDriver().manage().window().setSize(new Dimension(800, 600));
        var spreadsheet = getSpreadsheet();

        var scrollTabBeginning = findElementInShadowRoot(
                By.className("scroll-tabs-beginning"));
        assertPartEquals(scrollTabBeginning, SCROLL_TABS_BUTTON_DISABLED,
                SCROLL_TABS_TO_START_BUTTON);

        var scrollTabLeft = findElementInShadowRoot(
                By.className("scroll-tabs-left"));
        assertPartEquals(scrollTabLeft, SCROLL_TABS_BUTTON_DISABLED,
                SCROLL_TABS_BACKWARD_BUTTON);

        var scrollTabRight = findElementInShadowRoot(
                By.className("scroll-tabs-right"));
        assertPartEquals(scrollTabRight, SCROLL_TABS_BUTTON,
                SCROLL_TABS_FORWARD_BUTTON);
        var scrollTabEnd = findElementInShadowRoot(
                By.className("scroll-tabs-end"));
        assertPartEquals(scrollTabEnd, SCROLL_TABS_BUTTON,
                SCROLL_TABS_TO_END_BUTTON);

        // Click to scroll tabs and verify state changes
        scrollTabEnd.click();

        assertPartEquals(scrollTabBeginning, SCROLL_TABS_BUTTON,
                SCROLL_TABS_TO_START_BUTTON);
        assertPartEquals(scrollTabLeft, SCROLL_TABS_BUTTON,
                SCROLL_TABS_BACKWARD_BUTTON);
        assertPartEquals(scrollTabRight, SCROLL_TABS_BUTTON_DISABLED,
                SCROLL_TABS_FORWARD_BUTTON);
        assertPartEquals(scrollTabEnd, SCROLL_TABS_BUTTON_DISABLED,
                SCROLL_TABS_TO_END_BUTTON);
    }

    /**
     * Helper method to check if an element's part attribute contains all the
     * expected parts, regardless of order.
     */
    private void assertPartContains(WebElement element,
            String... expectedParts) {
        String partAttribute = element.getAttribute("part");
        Assert.assertNotNull("Part attribute should not be null",
                partAttribute);

        Set<String> actualParts = Arrays.stream(partAttribute.split("\\s+"))
                .collect(Collectors.toSet());
        Set<String> expectedPartsSet = Arrays.stream(expectedParts)
                .collect(Collectors.toSet());

        for (String expectedPart : expectedPartsSet) {
            Assert.assertTrue(String.format(
                    "Element should contain part '%s'. Actual parts: %s",
                    expectedPart, actualParts),
                    actualParts.contains(expectedPart));
        }
    }

    /**
     * Helper method to check if an element's part attribute exactly matches the
     * expected parts, regardless of order.
     */
    private void assertPartEquals(WebElement element, String... expectedParts) {
        String partAttribute = element.getAttribute("part");
        Assert.assertNotNull("Part attribute should not be null",
                partAttribute);

        Set<String> actualParts = Arrays.stream(partAttribute.split("\\s+"))
                .collect(Collectors.toSet());
        Set<String> expectedPartsSet = Arrays.stream(expectedParts)
                .collect(Collectors.toSet());

        Assert.assertEquals(String.format(
                "Part attribute should exactly match expected parts. Expected: %s, Actual: %s",
                expectedPartsSet, actualParts), expectedPartsSet, actualParts);
    }

    /**
     * Helper method to assert that a part does not contain certain parts.
     */
    private void assertPartDoesNotContain(WebElement element,
            String... unwantedParts) {
        String partAttribute = element.getAttribute("part");
        if (partAttribute == null) {
            return; // No parts to check
        }

        Set<String> actualParts = Arrays.stream(partAttribute.split("\\s+"))
                .collect(Collectors.toSet());

        for (String unwantedPart : unwantedParts) {
            Assert.assertFalse(String.format(
                    "Element should not contain part '%s'. Actual parts: %s",
                    unwantedPart, actualParts),
                    actualParts.contains(unwantedPart));
        }
    }
}
