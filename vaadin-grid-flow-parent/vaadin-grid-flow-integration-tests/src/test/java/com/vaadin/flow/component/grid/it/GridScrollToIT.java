/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
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
        grid = $(GridElement.class).waitForFirst();
    }

    @Test
    public void grid_scrollToEnd() {
        $("button").id("scroll-to-end").click();

        Assert.assertEquals(
                "Last visible index did not equal last item index after scroll to end.",
                1000L, grid.getLastVisibleRowIndex());
    }

    @Test
    public void grid_scrollToRow500() {
        $("button").id("scroll-to-row-500").click();

        Assert.assertEquals(
                "First visible index did not equal scrollToIndex parameter.",
                500L, grid.getFirstVisibleRowIndex());
    }

    @Test
    public void grid_scrollToEnd_scrollToStart() {
        grid_scrollToEnd();

        $("button").id("scroll-to-start").click();

        Assert.assertEquals(
                "First visible index did not equal 0 after scroll to start.",
                0L, grid.getFirstVisibleRowIndex());
    }

    @Test
    public void grid_addItems_scrollToEnd() {
        WebElement button = $("button").id("add-row-and-scroll-to-end");
        button.click();

        Assert.assertEquals(1002, grid.getLastVisibleRowIndex());

        button.click();
        Assert.assertEquals(1004, grid.getLastVisibleRowIndex());
    }

    @Test
    public void grid_addItem_scrollToIndex() {
        $("button").id("add-row-and-scroll-to-index").click();

        checkLogsForErrors();
        Assert.assertEquals(1001, grid.getLastVisibleRowIndex());
        Assert.assertEquals("1001",
                grid.getCell(grid.getLastVisibleRowIndex(), 0).getText());
    }

    @Test
    public void grid_scrollToItem500() {
        $("button").id("scroll-to-item-500").click();

        checkLogsForErrors();
        Assert.assertEquals(500, grid.getFirstVisibleRowIndex());
        Assert.assertEquals("500",
                grid.getCell(grid.getFirstVisibleRowIndex(), 0).getText());
    }

    @Test
    public void grid_addItem_scrollToItem() {
        $("button").id("add-row-and-scroll-to-item").click();

        checkLogsForErrors();
        Assert.assertEquals(1001, grid.getLastVisibleRowIndex());
        Assert.assertEquals("1001",
                grid.getCell(grid.getLastVisibleRowIndex(), 0).getText());
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
        var slot = grid.getCell(grid.getFirstVisibleRowIndex(), 0)
                .findElement(By.tagName("slot"));
        var content = grid
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
