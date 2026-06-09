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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.SerializableRunnable;

class GridContextMenuTest {

    private MenuManager menuManager = Mockito.mock(MenuManager.class);

    private class TestContextMenu extends ContextMenu {

        @Override
        protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager(
                SerializableRunnable contentReset) {
            return menuManager;
        }
    }

    @Test
    void setNonGridTargetForGridContextMenu_throws() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> gridContextMenu.setTarget(new NativeButton()));
    }

    @Test
    void addItemsWithNullClickListener_doesNotThrow() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();

        GridMenuItem<Object> foo = gridContextMenu.addItem("foo",
                (ComponentEventListener<GridContextMenuItemClickEvent<Object>>) null);
        gridContextMenu.addItem(new NativeButton(),
                (ComponentEventListener<GridContextMenuItemClickEvent<Object>>) null);

        foo.getSubMenu().addItem("bar",
                (ComponentEventListener<GridContextMenuItemClickEvent<Object>>) null);
        foo.getSubMenu().addItem(new NativeButton(),
                (ComponentEventListener<GridContextMenuItemClickEvent<Object>>) null);
    }

    @Test
    void addTextItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        menu.addItem("foo",
                (ComponentEventListener<ClickEvent<MenuItem>>) null);

        Mockito.verify(menuManager).addItem("foo",
                (ComponentEventListener<ClickEvent<MenuItem>>) null);
    }

    @Test
    void addComponentItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        Component component = Mockito.mock(Component.class);
        menu.addItem(component,
                (ComponentEventListener<ClickEvent<MenuItem>>) null);

        Mockito.verify(menuManager).addItem(component,
                (ComponentEventListener<ClickEvent<MenuItem>>) null);
    }

    @Test
    void setTarget_targetIsGrid_getterReturnsSetTarget() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();
        Grid<Object> grid = new Grid<>();
        gridContextMenu.setTarget(grid);

        Assertions.assertEquals(grid, gridContextMenu.getTarget());
    }
}
