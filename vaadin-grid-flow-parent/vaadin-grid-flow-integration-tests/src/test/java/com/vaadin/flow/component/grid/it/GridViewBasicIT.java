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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.junit.Assert;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo")
public class GridViewBasicIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void disabledGrid_itemsAreDisabled() {
        GridElement grid = $(GridElement.class).id("disabled-grid");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);
        Assert.assertFalse("Grid should be disabled", grid.isEnabled());

        GridTRElement row = grid.getRow(0);
        GridTHTDElement cell = row.getCell(grid.getColumn("Action"));
        WebElement button = cell.getContext().findElement(By.tagName("button"));

        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());

        grid.scrollToRow(499);
        waitUntil(driver -> grid.getRowCount() == 500);

        row = grid.getRow(499);
        cell = row.getCell(grid.getColumn("Action"));
        button = cell.getContext().findElement(By.tagName("button"));

        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());
    }

    @Test
    public void dataIsShown() throws InterruptedException {
        GridElement grid = $(GridElement.class).id("basic");

        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Person 1", grid.getCell(0, 0).getText());
        grid.scrollToRow(185);
        waitUntil(driver -> grid.getFirstVisibleRowIndex() >= 185);
        Assert.assertEquals("Person 186", grid.getCell(185, 0).getText());
    }

    @Test
    public void noHeaderIsShown() throws InterruptedException {
        GridElement grid = $(GridElement.class).id("noHeader");

        Assert.assertFalse(grid.getHeaderCell(0).isDisplayed());
        Assert.assertFalse(grid.getHeaderCell(1).isDisplayed());
    }

    @Test
    public void lazyDataIsShown() throws InterruptedException {
        GridElement grid = $(GridElement.class).id("lazy-loading");
        scrollToElement(grid);

        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        grid.scrollToRow(1010);
        waitUntil(driver -> grid.getFirstVisibleRowIndex() >= 1010);
        Assert.assertEquals("Person 1011", grid.getCell(1010, 0).getText());
    }
}
