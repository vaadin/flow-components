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
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-select-on-item-click")
public class GridSelectOnItemClickIT extends AbstractComponentIT {
    private GridElement grid;
    private TestBenchElement selectedItems;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        selectedItems = $("span").id("selected-items");
    }

    @Test
    public void clickRow_togglesSelection() {
        grid.getCell(0, 1).click();
        Assert.assertTrue(grid.getRow(0).isSelected());
        Assert.assertEquals("Item 0", selectedItems.getText());

        grid.getCell(0, 1).click();
        Assert.assertFalse(grid.getRow(0).isSelected());
        Assert.assertEquals("", selectedItems.getText());
    }
}
