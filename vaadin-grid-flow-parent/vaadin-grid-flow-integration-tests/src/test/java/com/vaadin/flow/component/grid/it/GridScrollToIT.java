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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the GridPageSizePage view.
 */
@TestPath("vaadin-grid/grid-scroll-to")
public class GridScrollToIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
        grid = $(GridElement.class).first();
    }

    @Test
    public void grid_scrollToEnd() {
        $("button").id("scroll-to-end").click();

        Assert.assertEquals(
                "Last visible index did not equal last item index after scroll to end.",
                1000L, grid.getProperty("lastVisibleIndex"));
    }

    @Test
    public void grid_scrollToRow500() {
        $("button").id("scroll-to-row-500").click();

        Assert.assertEquals(
                "First visible index did not equal scrollToIndex parameter.",
                500L, grid.getProperty("firstVisibleIndex"));
    }

    @Test
    public void grid_scrollToEnd_scrollToStart() {
        grid_scrollToEnd();

        $("button").id("scroll-to-start").click();

        Assert.assertEquals(
                "First visible index did not equal 0 after scroll to start.",
                0L, grid.getProperty("firstVisibleIndex"));
    }

    @Test
    public void grid_addItems_scrollToEnd() {
        WebElement button = $("button").id("add-row-and-scroll-to-end");
        button.click();

        GridElement grid = $(GridElement.class).id("scroll-to-end-grid");

        Assert.assertEquals(0, grid.getFirstVisibleRowIndex());
        Assert.assertEquals(1, grid.getLastVisibleRowIndex());

        button.click();
        Assert.assertEquals(0, grid.getFirstVisibleRowIndex());
        Assert.assertEquals(3, grid.getLastVisibleRowIndex());
    }

    @Test
    public void grid_addItem_scrollToIndex_twice() {
        $("button").id("add-row-and-scroll-to-index").click();
        // Wait until finished loading
        waitUntil(e -> !grid.getRow(grid.getFirstVisibleRowIndex())
                .hasAttribute("loading"));

        $("button").id("add-row-and-scroll-to-index").click();
        waitUntil(e -> !grid.getRow(grid.getFirstVisibleRowIndex())
                .hasAttribute("loading"));

        // Find the content element of the first visible row cell
        TestBenchElement slot = grid.getCell(grid.getFirstVisibleRowIndex(), 0)
                .findElement(By.tagName("slot"));
        TestBenchElement content = grid
                .findElement(By.cssSelector("vaadin-grid-cell-content[slot='"
                        + slot.getPropertyString("name") + "']"));
        // Expect the content element to be displayed
        Assert.assertTrue(content.isDisplayed());
    }

    @Test
    public void grid_smallPageSize_addItem_scrollToIndex_twice() {
        // Set page size to 5
        $("button").id("set-small-page-size").click();

        $("button").id("add-row-and-scroll-to-index").click();
        // Wait until finished loading
        waitUntil(e -> !grid.getRow(grid.getFirstVisibleRowIndex())
                .hasAttribute("loading"));

        $("button").id("add-row-and-scroll-to-index").click();
        waitUntil(e -> !grid.getRow(grid.getFirstVisibleRowIndex())
                .hasAttribute("loading"));
    }

}
