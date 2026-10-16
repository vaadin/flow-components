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
package com.vaadin.flow.component.menubar.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;

class MenuBarTest {

    private MenuBar menuBar;
    private MenuItem item1, item2;

    @BeforeEach
    void setup() {
        menuBar = new MenuBar();
        item1 = menuBar.addItem("foo");
        item2 = menuBar.addItem(new Span("bar"));
    }

    @Test
    void rootMenuItem_setCheckableTrue_throws() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> item1.setCheckable(true));
    }

    // Note that the MenuManager is already tested in vaadin-context-menu-flow.

    @Test
    void getChildren_getItems() {
        assertChildrenAndItems(item1, item2);
    }

    @Test
    void addChildrenWithClickListeners_getChildren_getItems() {
        MenuItem item3 = menuBar.addItem("foo", e -> {
        });
        MenuItem item4 = menuBar.addItem(new Span("bar"), e -> {
        });
        assertChildrenAndItems(item1, item2, item3, item4);
    }

    @Test
    void remove_getChildren_getItems() {
        menuBar.remove(item1);
        assertChildrenAndItems(item2);
    }

    @Test
    void removeAll_getChildren_getItems() {
        menuBar.removeAll();
        assertChildrenAndItems();
    }

    @Test
    void removeNonChildItem_throws() {
        MenuItem item = new MenuBar().addItem("foo");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> menuBar.remove(item));
    }

    @Test
    void openOnHover_falseByDefault() {
        Assertions.assertFalse(menuBar.isOpenOnHover());
    }

    @Test
    void setOpenOnHover_isOpenOnHover() {
        menuBar.setOpenOnHover(true);
        Assertions.assertTrue(menuBar.isOpenOnHover());
    }

    @Test
    void isReverseCollapseOrder() {
        Assertions.assertFalse(menuBar.isReverseCollapseOrder());
        Assertions.assertFalse(
                menuBar.getElement().getProperty("reverseCollapse", false));
    }

    @Test
    void setReverseCollapseOrder_isReverseCollapseOrder() {
        menuBar.setReverseCollapseOrder(true);
        Assertions.assertTrue(menuBar.isReverseCollapseOrder());
        Assertions.assertTrue(
                menuBar.getElement().getProperty("reverseCollapse", false));
    }

    @Test
    void isTabNavigation() {
        Assertions.assertFalse(menuBar.isTabNavigation());
        Assertions.assertFalse(
                menuBar.getElement().getProperty("tabNavigation", false));
    }

    @Test
    void setTabNavigation_isTabNavigation() {
        menuBar.setTabNavigation(true);
        Assertions.assertTrue(menuBar.isTabNavigation());
        Assertions.assertTrue(
                menuBar.getElement().getProperty("tabNavigation", false));
    }

    private void assertChildrenAndItems(MenuItem... expected) {
        Object[] menuItems = menuBar.getChildren().toArray();
        Assertions.assertArrayEquals(expected, menuItems);

        menuItems = menuBar.getItems().toArray();
        Assertions.assertArrayEquals(expected, menuItems);
    }
}
