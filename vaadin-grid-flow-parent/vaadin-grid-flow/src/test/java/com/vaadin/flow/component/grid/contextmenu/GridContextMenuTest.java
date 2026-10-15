/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.contextmenu;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.SerializableRunnable;

public class GridContextMenuTest {

    private MenuManager menuManager = Mockito.mock(MenuManager.class);

    private class TestContextMenu extends ContextMenu {

        @Override
        protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager(
                SerializableRunnable contentReset) {
            return menuManager;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNonGridTargetForGridContextMenu_throws() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();
        gridContextMenu.setTarget(new NativeButton());
    }

    @Test
    public void addItemsWithNullClickListener_doesNotThrow() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();

        GridMenuItem<Object> foo = gridContextMenu.addItem("foo", null);
        gridContextMenu.addItem(new NativeButton(), null);

        foo.getSubMenu().addItem("bar", null);
        foo.getSubMenu().addItem(new NativeButton(), null);
    }

    @Test
    public void addTextItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        menu.addItem("foo", null);

        Mockito.verify(menuManager).addItem("foo", null);
    }

    @Test
    public void addComponentItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        Component component = Mockito.mock(Component.class);
        menu.addItem(component, null);

        Mockito.verify(menuManager).addItem(component, null);
    }

    @Test
    public void setTarget_targetIsGrid_getterReturnsSetTarget() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();
        Grid<Object> grid = new Grid<>();
        gridContextMenu.setTarget(grid);

        Assert.assertEquals(grid, gridContextMenu.getTarget());
    }
}
