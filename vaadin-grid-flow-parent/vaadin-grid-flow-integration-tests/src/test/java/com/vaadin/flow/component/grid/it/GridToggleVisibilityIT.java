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

@TestPath("vaadin-grid/toggle-visibility")
public class GridToggleVisibilityIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void hiddenGrid_selectAndShow_rowIsSelected() {
        clickButton("select-and-show");

        GridElement grid = $(GridElement.class).id("hidden-grid");
        Assert.assertTrue(grid.getRow(0).isSelected());
        Assert.assertFalse(grid.getRow(1).isSelected());
        checkLogsForErrors();
    }

    @Test
    public void hiddenGrid_refreshAndShow_newItemsAreRendered() {
        clickButton("refresh-and-show");

        GridElement grid = $(GridElement.class).id("hidden-grid");
        Assert.assertEquals(1, grid.getRowCount());
        Assert.assertEquals("baz", grid.getCell(0, 0).getText());
        checkLogsForErrors();
    }

    @Test
    public void hiddenParent_toggleParent_gridIsRendered() {
        clickButton("toggle-parent");

        GridElement grid = $(GridElement.class).id("hidden-parent-grid");
        Assert.assertEquals("Item 0", grid.getCell(0, 0).getText());

        clickButton("toggle-parent");
        clickButton("toggle-parent");
        Assert.assertEquals("Item 0", grid.getCell(0, 0).getText());
        checkLogsForErrors();
    }

    @Test
    public void hideGrid_setItems_showGrid_componentsAreRendered() {
        GridElement grid = $(GridElement.class).id("component-grid");

        // The second round matters: the first one can pass on a grid that had
        // no rows before
        for (int round = 1; round <= 2; round++) {
            clickButton("hide-grid");
            clickButton("set-items");
            clickButton("show-grid");

            int generation = round + 1;
            Assert.assertEquals("round " + round,
                    "component of item " + generation + ".1",
                    grid.getCell(0, 1).getText());
        }
    }

    @Test
    public void hideGridClearItemsAndHideColumn_showGridWithNewItems_componentsAreRendered() {
        GridElement grid = $(GridElement.class).id("component-grid");

        clickButton("hide-grid-and-column");
        clickButton("show-grid-with-new-items");

        waitUntil(driver -> grid.getRowCount() > 0);

        // The text column is hidden, so the component column is the only one
        // left
        Assert.assertEquals("component of item 2.1",
                grid.getCell(0, 0).getText());
    }

    private void clickButton(String id) {
        $("button").id(id).click();
    }
}
