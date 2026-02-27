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

import static com.vaadin.flow.component.grid.it.GridBindItemsPage.ADD_ITEM_BUTTON;
import static com.vaadin.flow.component.grid.it.GridBindItemsPage.GRID_ID;
import static com.vaadin.flow.component.grid.it.GridBindItemsPage.ITEM_COUNT_SPAN;
import static com.vaadin.flow.component.grid.it.GridBindItemsPage.REMOVE_ITEM_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/grid-bind-items")
public class GridBindItemsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id(GRID_ID);
    }

    @Test
    public void bindItems_initialItemsDisplayed() {
        Assert.assertEquals("Initial item count should be 3", "3",
                $("span").id(ITEM_COUNT_SPAN).getText());
        Assert.assertEquals("Grid should have 3 rows", 3, grid.getRowCount());

        Assert.assertEquals("First row first name should be Alice", "Alice",
                grid.getCell(0, 0).getText());
        Assert.assertEquals("First row last name should be Smith", "Smith",
                grid.getCell(0, 1).getText());

        Assert.assertEquals("Second row first name should be Bob", "Bob",
                grid.getCell(1, 0).getText());
        Assert.assertEquals("Third row first name should be Charlie", "Charlie",
                grid.getCell(2, 0).getText());
    }

    @Test
    public void bindItems_addItem_gridUpdated() {
        Assert.assertEquals("Initial grid row count", 3, grid.getRowCount());

        $(ButtonElement.class).id(ADD_ITEM_BUTTON).click();

        Assert.assertEquals("Item count should be 4", "4",
                $("span").id(ITEM_COUNT_SPAN).getText());
        Assert.assertEquals("Grid should have 4 rows", 4, grid.getRowCount());

        Assert.assertEquals("New row should contain 'New Person 4'",
                "New Person 4", grid.getCell(3, 0).getText());
    }

    @Test
    public void bindItems_removeItem_gridUpdated() {
        Assert.assertEquals("Initial grid row count", 3, grid.getRowCount());

        $(ButtonElement.class).id(REMOVE_ITEM_BUTTON).click();

        Assert.assertEquals("Item count should be 2", "2",
                $("span").id(ITEM_COUNT_SPAN).getText());
        Assert.assertEquals("Grid should have 2 rows", 2, grid.getRowCount());

        Assert.assertEquals("First row still has Alice", "Alice",
                grid.getCell(0, 0).getText());
        Assert.assertEquals("Second row still has Bob", "Bob",
                grid.getCell(1, 0).getText());
    }

    @Test
    public void bindItems_multipleAdds_gridUpdatesCorrectly() {
        $(ButtonElement.class).id(ADD_ITEM_BUTTON).click();
        $(ButtonElement.class).id(ADD_ITEM_BUTTON).click();

        Assert.assertEquals("Item count should be 5", "5",
                $("span").id(ITEM_COUNT_SPAN).getText());
        Assert.assertEquals("Grid should have 5 rows", 5, grid.getRowCount());

        Assert.assertEquals("Fourth item", "New Person 4",
                grid.getCell(3, 0).getText());
        Assert.assertEquals("Fifth item", "New Person 5",
                grid.getCell(4, 0).getText());
    }

    @Test
    public void bindItems_addThenRemove_gridCorrect() {
        $(ButtonElement.class).id(ADD_ITEM_BUTTON).click();
        Assert.assertEquals("After add: 4 rows", 4, grid.getRowCount());

        $(ButtonElement.class).id(REMOVE_ITEM_BUTTON).click();
        Assert.assertEquals("After remove: 3 rows", 3, grid.getRowCount());

        // Verify original items are still there
        Assert.assertEquals("Alice", grid.getCell(0, 0).getText());
        Assert.assertEquals("Bob", grid.getCell(1, 0).getText());
        Assert.assertEquals("Charlie", grid.getCell(2, 0).getText());
    }
}
