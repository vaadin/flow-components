/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

/**
 * Integration tests for the GridPageSizePage view.
 */
@TestPath("grid-page-size")
public class GridPageSizePageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
    }

    @Test
    public void gridWithPageSize10_changeTo80_revertBackTo10() {
        WebElement grid = findElement(By.tagName("vaadin-grid"));

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

    private void assertCellContents(WebElement grid) {
        /*
         * Smoke test for the cell contents. The actual contents depends on the
         * size of the Grid, the window size, and the scroll position. The
         * headers and footers are also part of the Grid contents.
         */
        List<WebElement> cells = grid
                .findElements(By.tagName("vaadin-grid-cell-content"));

        int offset = getCellsOffsetFromTheHeaders(grid, cells);

        assertCellContent("0", cells.get(offset));
        assertCellContent("24", cells.get(offset + 48));
    }

    private int getCellsOffsetFromTheHeaders(WebElement grid,
            List<WebElement> cells) {
        int numberOfColumns = grid
                .findElements(By.tagName("vaadin-grid-column")).size();
        for (int i = numberOfColumns; i < cells.size(); i++) {
            WebElement cell = cells.get(i);
            String content = cell.getAttribute("innerHTML");
            if (!content.trim().isEmpty()) {
                return i;
            }
        }
        return 0;
    }

    private void assertCellContent(String expected, WebElement cell) {
        Assert.assertEquals("Wrong content of the rendered cell", expected,
                cell.getAttribute("innerHTML"));
    }

    private void assertPageSize(WebElement grid, int pageSize) {
        Object pageSizeFromGrid = executeScript("return arguments[0].pageSize",
                grid);
        Assert.assertEquals(
                "The pageSize of the webcomponent should be " + pageSize,
                pageSize, Integer.parseInt(String.valueOf(pageSizeFromGrid)));
    }

}
