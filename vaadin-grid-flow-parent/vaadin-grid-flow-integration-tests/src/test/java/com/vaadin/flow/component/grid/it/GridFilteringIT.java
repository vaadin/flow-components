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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-filtering")
public class GridFilteringIT extends AbstractComponentIT {

    @Test
    public void gridInNotLoadingState() {
        open();

        WebElement filter = $("vaadin-text-field").id("filter");
        WebElement input = getInShadowRoot(filter, By.cssSelector("input"));
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

    @Test
    public void gridShouldRenderCorrectly_whenColumnAndFilter_() {
        open();

        GridElement grid = $(GridElement.class).id("simple-grid-filtering");
        TestBenchElement filterButton = $(TestBenchElement.class)
                .id("filter-grid-and-hide-column");
        TestBenchElement clearButton = $(TestBenchElement.class)
                .id("clear-filter-and-show-column");

        waitUntil(driver -> grid.getRowCount() > 0);

        GridColumnElement columnElement = grid.getColumn("firstName");

        GridTHTDElement cell = grid.getRow(0).getCell(columnElement);

        Assert.assertEquals("Person 1", cell.getText());

        filterButton.click();
        clearButton.click();

        waitUntil(driver -> grid.getRowCount() > 0);

        cell = grid.getRow(0).getCell(columnElement);
        Assert.assertEquals("Person 1", cell.getText());
    }
}
