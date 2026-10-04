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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

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

    @Test
    void noContextMenu_getContextMenusIsEmpty() {
        Assertions.assertEquals(List.of(), new Grid<>().getContextMenus());
    }

    @Test
    void addContextMenu_getContextMenusReturnsGridContextMenusOnly() {
        Grid<Object> grid = new Grid<>();
        GridContextMenu<Object> first = grid.addContextMenu();
        new ContextMenu(grid);
        GridContextMenu<Object> second = new GridContextMenu<>(grid);

        Assertions.assertEquals(List.of(first, second), grid.getContextMenus());
        Assertions.assertEquals(List.of(first, second),
                GridContextMenu.getContextMenus(grid));
    }

    @Test
    void addContextMenu_contextMenuGetContextMenusReturnsPlainContextMenusOnly() {
        Grid<Object> grid = new Grid<>();
        grid.addContextMenu();
        ContextMenu plain = new ContextMenu(grid);

        Assertions.assertEquals(List.of(plain),
                ContextMenu.getContextMenus(grid));
    }

    @Test
    void multipleContextMenus_clearTargetOfOne_getContextMenusKeepsOthers() {
        Grid<Object> grid = new Grid<>();
        GridContextMenu<Object> first = grid.addContextMenu();
        GridContextMenu<Object> second = grid.addContextMenu();
        GridContextMenu<Object> third = grid.addContextMenu();

        second.setTarget(null);

        Assertions.assertEquals(List.of(first, third), grid.getContextMenus());
    }

    @Test
    void dynamicContentHandler_targetColumnIdIsUpdatedBeforeHandlerRuns() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(item -> item).setId("first");
        grid.addColumn(item -> item).setId("second");
        GridContextMenu<String> contextMenu = grid.addContextMenu();

        AtomicReference<String> columnIdInHandler = new AtomicReference<>();
        contextMenu.setDynamicContentHandler(item -> {
            columnIdInHandler.set(grid.getElement()
                    .getProperty("_contextMenuTargetColumnId"));
            return false;
        });

        fireBeforeOpenEvent(grid, "second");
        Assertions.assertEquals("second", columnIdInHandler.get());

        fireBeforeOpenEvent(grid, "first");
        Assertions.assertEquals("first", columnIdInHandler.get());
    }

    @Test
    void dynamicContentProvider_receivesClickedItemAndColumn() {
        Grid<String> grid = new Grid<>();
        grid.setItems("foo", "bar");
        Grid.Column<String> first = grid.addColumn(item -> item);
        Grid.Column<String> second = grid.addColumn(item -> item);
        GridContextMenu<String> contextMenu = grid.addContextMenu();

        AtomicReference<GridContextMenu.DynamicContentContext<String>> contextInProvider = new AtomicReference<>();
        contextMenu.setDynamicContentProvider(context -> {
            contextInProvider.set(context);
            return false;
        });

        String barKey = grid.getDataCommunicator().getKeyMapper().key("bar");
        fireBeforeOpenEvent(grid, barKey, "", getInternalId(second));
        Assertions.assertEquals("bar", contextInProvider.get().item());
        Assertions.assertSame(second, contextInProvider.get().column());

        fireBeforeOpenEvent(grid, "", "", getInternalId(first));
        Assertions.assertNull(contextInProvider.get().item());
        Assertions.assertSame(first, contextInProvider.get().column());

        // Not an application column, e.g. the selection column
        fireBeforeOpenEvent(grid, barKey, "", "");
        Assertions.assertEquals("bar", contextInProvider.get().item());
        Assertions.assertNull(contextInProvider.get().column());
    }

    @Test
    void dynamicContentProviderAndHandler_replaceEachOther() {
        GridContextMenu<String> contextMenu = new Grid<String>()
                .addContextMenu();
        SerializablePredicate<String> handler = item -> true;
        SerializablePredicate<GridContextMenu.DynamicContentContext<String>> provider = context -> true;

        contextMenu.setDynamicContentHandler(handler);
        Assertions.assertSame(handler, contextMenu.getDynamicContentHandler());

        contextMenu.setDynamicContentProvider(provider);
        Assertions.assertSame(provider,
                contextMenu.getDynamicContentProvider());
        Assertions.assertNull(contextMenu.getDynamicContentHandler());

        AtomicReference<Boolean> called = new AtomicReference<>(false);
        contextMenu.setDynamicContentProvider(context -> {
            called.set(true);
            return false;
        });
        contextMenu.setDynamicContentHandler(item -> false);
        Assertions.assertNull(contextMenu.getDynamicContentProvider());
        fireBeforeOpenEvent((Grid<?>) contextMenu.getTarget(), "", "", "");
        Assertions.assertFalse(called.get());
    }

    private static String getInternalId(Grid.Column<?> column) {
        return column.getElement().getProperty("_flowId");
    }

    private static void fireBeforeOpenEvent(Grid<?> grid, String columnId) {
        fireBeforeOpenEvent(grid, "", columnId, "");
    }

    private static void fireBeforeOpenEvent(Grid<?> grid, String key,
            String columnId, String internalColumnId) {
        var detail = JacksonUtils.createObjectNode();
        detail.put("key", key);
        detail.put("columnId", columnId);
        detail.put("internalColumnId", internalColumnId);
        var eventData = JacksonUtils.createObjectNode();
        eventData.set(ContextMenuBase.EVENT_DETAIL, detail);
        grid.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(grid.getElement(),
                        "vaadin-context-menu-before-open", eventData));
    }
}
