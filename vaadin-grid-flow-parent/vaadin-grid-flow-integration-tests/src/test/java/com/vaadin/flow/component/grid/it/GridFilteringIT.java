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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.grid.it.GridFilteringPage.GRID_FILTER_ID;
import static com.vaadin.flow.component.grid.it.GridFilteringPage.LAZY_FILTERABLE_GRID_ID;

@TestPath("vaadin-grid/grid-filtering")
public class GridFilteringIT extends AbstractComponentIT {

    @Test
    public void gridInNotLoadingState() {
        open();

        TextFieldElement filter = $(TextFieldElement.class).id("filter");
        WebElement input = filter.$("input").first();
        input.sendKeys("w");

        // Blur input to get value change
        executeScript("arguments[0].blur();", input);

        WebElement grid = findElement(By.id("data-grid"));
        // empty Grid content
        Object size = executeScript("return arguments[0].size", grid);
        Assert.assertEquals("0", size.toString());

        input.sendKeys(Keys.BACK_SPACE);

        // Blur input to get value change
        executeScript("arguments[0].blur();", input);

        waitUntil(driver -> executeScript("return arguments[0].size", grid)
                .toString().equals("3"));

        waitUntil(driver -> "false".equals(grid.getAttribute("loading")));
    }

    @Test // for https://github.com/vaadin/flow/issues/9988
    public void lazyLoadingFiltering_filterAppliedAfterScrolling_gridItemsFilteredAndRenderedProperly() {
        open();

        // wait for grid to be loaded
        waitUntil(driver -> $(GridElement.class).id(LAZY_FILTERABLE_GRID_ID)
                .getRowCount() > 0);

        GridElement gridElement = $(GridElement.class)
                .id(LAZY_FILTERABLE_GRID_ID);

        // Scroll down and trigger the next backend call
        gridElement.scrollToRow(125);

        TextFieldElement filter = $(TextFieldElement.class).id(GRID_FILTER_ID);

        // Apply external filter to reduce items count
        filter.sendKeys("123", Keys.ENTER);

        // Verify the grid contains only a filtered item
        waitUntil(driver -> gridElement.getRowCount() == 1
                && gridElement.getRow(0).getCell(gridElement.getColumn("Items"))
                        .getInnerHTML().equals("Item 123"));

        // No errors in browser logs
        checkLogsForErrors();

        // Remove the filter
        filter.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE,
                Keys.ENTER);

        // Verify that the filter has been removed in the grid
        waitUntil(driver -> gridElement.getRowCount() > 1
                && gridElement.getRow(0).getCell(gridElement.getColumn("Items"))
                        .getInnerHTML().equals("Item 0"));

        // No errors in browser logs after filter removal
        checkLogsForErrors();
    }
}
