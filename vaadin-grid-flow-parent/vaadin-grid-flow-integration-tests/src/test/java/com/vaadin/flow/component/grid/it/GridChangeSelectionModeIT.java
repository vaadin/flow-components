/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/change-selection-mode")
public class GridChangeSelectionModeIT extends AbstractComponentIT {
    private GridElement grid;
    private TestBenchElement setSingleSelection;
    private TestBenchElement setMultiSelection;
    private TestBenchElement setNoneSelection;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        setSingleSelection = $("button").id("set-single-selection");
        setMultiSelection = $("button").id("set-multi-selection");
        setNoneSelection = $("button").id("set-none-selection");
    }

    @Test
    public void singleSelection_changeToMultiSelection_itemsDeselected() {
        setSingleSelection.click();
        grid.select(0);

        Assert.assertEquals(1, getSelectedItemsCount());

        setMultiSelection.click();

        Assert.assertEquals(0, getSelectedItemsCount());
    }

    @Test
    public void singleSelection_changeToNoneSelection_itemsDeselected() {
        setSingleSelection.click();
        grid.select(0);

        Assert.assertEquals(1, getSelectedItemsCount());

        setNoneSelection.click();

        Assert.assertEquals(0, getSelectedItemsCount());
    }

    @Test
    public void multiSelection_changeToSingleSelection_itemsDeselected() {
        setMultiSelection.click();
        grid.select(0);
        grid.select(1);

        Assert.assertEquals(2, getSelectedItemsCount());

        setSingleSelection.click();

        Assert.assertEquals(0, getSelectedItemsCount());
    }

    @Test
    public void multiSelection_changeToNoneSelection_itemsDeselected() {
        setMultiSelection.click();
        grid.select(0);
        grid.select(1);

        Assert.assertEquals(2, getSelectedItemsCount());

        setNoneSelection.click();

        Assert.assertEquals(0, getSelectedItemsCount());
    }

    private long getSelectedItemsCount() {
        return (Long) grid.getCommandExecutor().executeScript(
                "return arguments[0].selectedItems.length", grid);
    }
}
