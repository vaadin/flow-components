/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
