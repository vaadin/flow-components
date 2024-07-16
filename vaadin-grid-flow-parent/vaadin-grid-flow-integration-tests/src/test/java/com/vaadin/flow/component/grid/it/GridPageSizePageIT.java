/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
@TestPath("vaadin-grid/grid-page-size")
public class GridPageSizePageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
    }

    @Test
    public void gridWithPageSize10_changeTo80_revertBackTo10() {
        GridElement grid = $(GridElement.class).first();

        assertPageSize(grid, 10);
        assertCellContents(grid);

        WebElement input = findElement(By.id("size-input"));
        WebElement button = findElement(By.id("size-submit"));

        input.sendKeys("80");
        blur();
        button.click();

        assertPageSize(grid, 80);
        assertCellContents(grid);

        input.clear();
        input.sendKeys("10");
        blur();
        button.click();

        assertPageSize(grid, 10);
        assertCellContents(grid);
    }

    private void assertCellContents(GridElement grid) {
        /*
         * Smoke test for the cell contents. The actual contents depends on the
         * size of the Grid, the window size, and the scroll position. The
         * headers and footers are also part of the Grid contents.
         */
        Assert.assertEquals("Wrong content for first content cell", "0",
                grid.getCell(0, 0).getText());
        Assert.assertEquals("Wrong content for 24th content cell", "24",
                grid.getCell(24, 0).getText());
    }

    private void assertPageSize(WebElement grid, int pageSize) {
        Object pageSizeFromGrid = executeScript("return arguments[0].pageSize",
                grid);
        Assert.assertEquals(
                "The pageSize of the webcomponent should be " + pageSize,
                pageSize, Integer.parseInt(String.valueOf(pageSizeFromGrid)));
    }

}
