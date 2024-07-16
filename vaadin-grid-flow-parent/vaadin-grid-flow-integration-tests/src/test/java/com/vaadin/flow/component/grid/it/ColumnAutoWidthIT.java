/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/column-auto-width")
public class ColumnAutoWidthIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(ColumnAutoWidthPage.GRID_ID));
        grid = $(GridElement.class).id(ColumnAutoWidthPage.GRID_ID);
        waitUntil(driver -> (Boolean) executeScript(
                "return arguments[0]._getColumns()[0].width !== '100px'",
                grid));
    }

    /**
     * Converts a CSS pixel value string to an integer. Assumes that the given
     * string is a CSS value in pixels (e.g. "100px").
     *
     * @param cssValue
     *            Css pixel value (e.g. "100px")
     * @return Number of pixels as an integer
     */
    private int cssPixelValueToInteger(String cssValue) {
        return Integer.parseInt(cssValue.replaceFirst("px", ""));
    }

    /**
     * Assert that the given CSS pixel value is close to the expected value
     * (with a margin of ±<code>delta</code>)
     *
     * @param expected
     *            expected value
     * @param cssValue
     *            CSS pixel value
     * @param delta
     *            allowed margin of deviation from the expected value
     */
    private void assertCssPixelValueCloseTo(int expected, String cssValue,
            int delta) {
        int value = cssPixelValueToInteger(cssValue);
        int min = expected - delta;
        int max = expected + delta;
        boolean valueInAllowedRange = (min <= value) && (value <= max);
        Assert.assertTrue(
                "CSS value '" + cssValue + "' does not match the expected "
                        + expected + " (±" + delta + ") pixels.",
                valueInAllowedRange);
    }

    @Test
    public void columnWidthsAreSetCorrectly() {
        @SuppressWarnings("unchecked")
        List<String> colWidths = (List<String>) executeScript(
                "return arguments[0]._getColumns().map(col => col.width)",
                grid);

        assertCssPixelValueCloseTo(55, colWidths.get(0), 5);
        assertCssPixelValueCloseTo(420, colWidths.get(2), 5);
        assertCssPixelValueCloseTo(243, colWidths.get(3), 5);
    }
}
