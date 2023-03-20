
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

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

        GridElement grid = $(GridElement.class).id("scroll-to-end-grid");

        Assert.assertEquals(0, grid.getFirstVisibleRowIndex());
        Assert.assertEquals(1, grid.getLastVisibleRowIndex());

        button.click();
        Assert.assertEquals(0, grid.getFirstVisibleRowIndex());
        Assert.assertEquals(3, grid.getLastVisibleRowIndex());
    }

}
