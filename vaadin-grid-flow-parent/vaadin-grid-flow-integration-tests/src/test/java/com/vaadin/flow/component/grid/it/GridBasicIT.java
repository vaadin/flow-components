/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/basic")
public class GridBasicIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void dataIsShown() {
        GridElement grid = $(GridElement.class).id("basic");

        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        Assert.assertEquals("Person 1", grid.getCell(0, 0).getText());
        grid.scrollToRow(185);
        waitUntil(driver -> grid.getFirstVisibleRowIndex() >= 185);
        Assert.assertEquals("Person 186", grid.getCell(185, 0).getText());
    }

    @Test
    public void noHeaderIsShown() {
        GridElement grid = $(GridElement.class).id("noHeader");

        Assert.assertFalse(grid.getHeaderCell(0).isDisplayed());
        Assert.assertFalse(grid.getHeaderCell(1).isDisplayed());
    }

    @Test
    public void lazyDataIsShown() {
        GridElement grid = $(GridElement.class).id("lazy-loading");
        scrollToElement(grid);

        Assert.assertEquals("Name", grid.getHeaderCell(0).getText());
        grid.scrollToRow(1010);
        waitUntil(driver -> grid.getFirstVisibleRowIndex() >= 1010);
        Assert.assertEquals("Person 1011", grid.getCell(1010, 0).getText());
    }
}
