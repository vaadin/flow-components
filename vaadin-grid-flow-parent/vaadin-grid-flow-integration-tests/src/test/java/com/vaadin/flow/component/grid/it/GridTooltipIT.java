/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/tooltip")
public class GridTooltipIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void tooltipGenerator_shouldNotProduceErrors() {
        checkLogsForErrors(
                msg -> !msg.contains("Cannot read properties of undefined"));
    }

    @Test
    @Ignore
    public void hoverOverTooltipColumnCell_showTooltip() {
        var grid = $(GridElement.class).first();
        flushScrolling(grid);
        showTooltip(grid.getCell("Jack"));
        Assert.assertEquals("First name of the person is Jack",
                getActiveTooltipText());
    }

    @Test
    @Ignore
    public void toggleGrid_hoverOverTooltipColumnCell_showTooltip() {
        // Remove the grid
        clickElementWithJs("toggle-grid-button");
        // Add the grid
        clickElementWithJs("toggle-grid-button");

        var grid = $(GridElement.class).first();
        flushScrolling(grid);
        showTooltip(grid.getCell("Jack"));
        Assert.assertEquals("First name of the person is Jack",
                getActiveTooltipText());
    }

    @Test
    @Ignore
    public void dynamicallyAddGenerator_hoverOverTooltipColumnCell_showTooltip() {
        var grid = $(GridElement.class).first();
        clickElementWithJs("set-age-tooltip-button");
        flushScrolling(grid);
        showTooltip(grid.getCell("33"));
        Assert.assertEquals("Age of the person is 33", getActiveTooltipText());
    }

    private void showTooltip(GridTHTDElement cell) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseenter', {bubbles:true}))",
                cell);
    }

    private String getActiveTooltipText() {
        return findElement(By.tagName("vaadin-tooltip-overlay")).getText();
    }

    /**
     * Forces the grid to remove the `scrolling` attribute on the grid scroller,
     * which would otherwise prevent a tooltip to open on mouseenter
     *
     * @param grid
     *            the grid to flush
     */
    private void flushScrolling(GridElement grid) {
        getCommandExecutor().executeScript(
                "const grid = arguments[0]; if (grid._debounceScrolling) { grid._debounceScrolling.flush() }",
                grid);
    }
}
