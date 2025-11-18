/**
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class StylingPartsIT extends AbstractSpreadsheetIT {

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

        var tab = spreadsheet.$(TestBenchElement.class)
                .withClassName("sheet-tabsheet-tab").first();
        assertPartContains(tab, "tabsheet-tab");

        var scrollTabButtons = Map.of("scroll-tabs-beginning",
                "scroll-tabs-button-start", "scroll-tabs-left",
                "scroll-tabs-button-left", "scroll-tabs-right",
                "scroll-tabs-button-right", "scroll-tabs-end",
                "scroll-tabs-button-end", "add-new-tab", "add-new-tab-button");

        for (var entry : scrollTabButtons.entrySet()) {
            var button = spreadsheet.$(TestBenchElement.class)
                    .withClassName(entry.getKey()).first();
            assertPartContains(button, entry.getValue());
        }

        var selectionCorner = spreadsheet.$(TestBenchElement.class)
                .withClassName("s-corner").first();
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
                    "selected-header");
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
                    "selected-header");
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

        var commentTriangle = firstCell
                .findElement(By.className("cell-comment-triangle"));
        assertPartEquals(commentTriangle, "comment-triangle");
    }

    @Test
    public void cellWithFormulaError_partAttributeExists() {
        var spreadsheet = getSpreadsheet();
        var errorCell = spreadsheet.getCellAt("A1");
        // Set a formula that produces an error
        errorCell.setValue("=XYZ");

        var errorIndicator = errorCell
                .findElement(By.className("cell-invalidformula-triangle"));
        assertPartEquals(errorIndicator, "invalid-triangle");
    }

    @Test
    public void popupButton_partAttributeExists() {
        loadTestFixture(TestFixtures.SpreadsheetTable);
        var spreadsheet = getSpreadsheet();
        var popupButtonCell = spreadsheet.getCellAt("B2");
        var popupButton = popupButtonCell
                .findElement(By.className("popupbutton"));
        assertPartEquals(popupButton, "popup-button");
    }

    @Test
    public void sheetTab_partAttributeExists() {
        var spreadsheet = getSpreadsheet();
        spreadsheet.addSheet();

        var sheetTabs = spreadsheet.$(TestBenchElement.class)
                .withClassName("sheet-tabsheet-tab");

        assertPartEquals(sheetTabs.get(0), "tabsheet-tab");
        assertPartEquals(sheetTabs.get(1), "tabsheet-tab", "selected-tab");
    }

    @Test
    public void scrollTabs_partAttributeExists() {
        loadFile("ConditionalFormatterSamples.xlsx");
        getDriver().manage().window().setSize(new Dimension(800, 600));
        var spreadsheet = getSpreadsheet();

        var scrollTabBeginning = spreadsheet.$(TestBenchElement.class)
                .withClassName("scroll-tabs-beginning").first();
        assertPartEquals(scrollTabBeginning, "scroll-tabs-disabled-button",
                "scroll-tabs-button-start");

        var scrollTabLeft = spreadsheet.$(TestBenchElement.class)
                .withClassName("scroll-tabs-left").first();
        assertPartEquals(scrollTabLeft, "scroll-tabs-disabled-button",
                "scroll-tabs-button-left");

        var scrollTabRight = spreadsheet.$(TestBenchElement.class)
                .withClassName("scroll-tabs-right").first();
        assertPartEquals(scrollTabRight, "scroll-tabs-button",
                "scroll-tabs-button-right");

        var scrollTabEnd = spreadsheet.$(TestBenchElement.class)
                .withClassName("scroll-tabs-end").first();
        assertPartEquals(scrollTabEnd, "scroll-tabs-button",
                "scroll-tabs-button-end");

        // Click to scroll tabs and verify state changes
        scrollTabEnd.click();

        assertPartEquals(scrollTabBeginning, "scroll-tab", "scroll-tab-start");
        assertPartEquals(scrollTabLeft, "scroll-tab", "scroll-tab-left");
        assertPartEquals(scrollTabRight, "scroll-tab-disabled",
                "scroll-tab-right");
        assertPartEquals(scrollTabEnd, "scroll-tab-disabled", "scroll-tab-end");
    }

    /**
     * Helper method to check if an element's part attribute contains all the
     * expected parts, regardless of order.
     */
    private void assertPartContains(TestBenchElement element,
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
