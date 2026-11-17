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
package com.vaadin.flow.component.grid.contextmenu;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/context-menu-data")
public class GridContextMenuDataIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void updateItem_rowIsRefreshed() {
        GridElement grid = $(GridElement.class).id("context-menu-grid");

        clickMenuItem(grid, 2, "Update");

        assertFirstCells(grid, "Person 1", "Person 2", "Person 3 Updated",
                "Person 4");
    }

    @Test
    public void removeItem_rowIsRemoved() {
        GridElement grid = $(GridElement.class).id("context-menu-grid");

        clickMenuItem(grid, 1, "Remove");

        assertFirstCells(grid, "Person 1", "Person 3", "Person 4", "Person 5");
    }

    @Test
    public void openSubMenu_insertRowAbove_rowIsInserted() {
        GridElement grid = $(GridElement.class).id("context-submenu-grid");

        clickSubMenuItem(grid, "Insert a row above");

        assertFirstCells(grid, "Person 11", "Person 1", "Person 2", "Person 3");
    }

    @Test
    public void openSubMenu_insertRowBelow_rowIsInserted() {
        GridElement grid = $(GridElement.class).id("context-submenu-grid");

        clickSubMenuItem(grid, "Insert a row below");

        assertFirstCells(grid, "Person 1", "Person 11", "Person 2", "Person 3");
    }

    private void clickMenuItem(GridElement grid, int rowIndex, String text) {
        ContextMenuElement menu = ContextMenuElement
                .openByRightClick(grid.getCell(rowIndex, 0));
        menu.getMenuItem(text).orElseThrow().click();
    }

    private void clickSubMenuItem(GridElement grid, String text) {
        ContextMenuElement menu = ContextMenuElement
                .openByRightClick(grid.getCell(0, 0));
        ContextMenuItemElement insert = menu.getMenuItem("Insert")
                .orElseThrow();
        insert.openSubMenu().getMenuItem(text).orElseThrow().click();
    }

    private void assertFirstCells(GridElement grid, String... cellContents) {
        IntStream.range(0, cellContents.length).forEach(i -> Assert
                .assertEquals(cellContents[i], grid.getCell(i, 0).getText()));
    }
}
