/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/column-auto-width")
public class ColumnAutoWidthIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        waitUntil(driver -> (Boolean) executeScript(
                "return arguments[0]._getColumns()[0].width !== '100px'",
                grid));
    }

    @Test
    public void columnWidthsAreSetCorrectly() {
        List<String> colWidths = getColumnWidths();

        assertCssPixelValueCloseTo(86, colWidths.get(0));
        assertCssPixelValueCloseTo(421, colWidths.get(2));
        assertCssPixelValueCloseTo(243, colWidths.get(3));
    }

    @Test
    public void updateItems_updatesComponentRendererColumnWidth() {
        $("button").id("update-items").click();

        List<String> colWidths = getColumnWidths();

        assertCssPixelValueCloseTo(156, colWidths.get(0));
    }

    @SuppressWarnings("unchecked")
    private List<String> getColumnWidths() {
        return (List<String>) executeScript(
                "return arguments[0]._getColumns().map(col => col.width)",
                grid);
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
     * (with a margin of ±5 pixels)
     *
     * @param expected
     *            expected value
     * @param cssValue
     *            CSS pixel value
     */
    private void assertCssPixelValueCloseTo(int expected, String cssValue) {
        final int delta = 5;
        int value = cssPixelValueToInteger(cssValue);
        int min = expected - delta;
        int max = expected + delta;
        boolean valueInAllowedRange = (min <= value) && (value <= max);
        Assert.assertTrue(
                "CSS value '" + cssValue + "' does not match the expected "
                        + expected + " (±" + delta + ") pixels.",
                valueInAllowedRange);
    }
}
