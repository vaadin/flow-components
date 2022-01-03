/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;

public class MenuBarTest {

    private MenuBar menuBar;
    private MenuItem item1, item2;

    @Before
    public void init() {
        menuBar = new MenuBar();
        item1 = menuBar.addItem("foo");
        item2 = menuBar.addItem(new Span("bar"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void rootMenuItem_setCheckableTrue_throws() {
        item1.setCheckable(true);
    }

    // Note that the MenuManager is already tested in vaadin-context-menu-flow.

    @Test
    public void getChildren_getItems() {
        assertChildrenAndItems(item1, item2);
    }

    @Test
    public void addChildrenWithClickListeners_getChildren_getItems() {
        MenuItem item3 = menuBar.addItem("foo", e -> {
        });
        MenuItem item4 = menuBar.addItem(new Span("bar"), e -> {
        });
        assertChildrenAndItems(item1, item2, item3, item4);
    }

    @Test
    public void remove_getChildren_getItems() {
        menuBar.remove(item1);
        assertChildrenAndItems(item2);
    }

    @Test
    public void removeAll_getChildren_getItems() {
        menuBar.removeAll();
        assertChildrenAndItems();
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNonChildItem_throws() {
        MenuItem item = new MenuBar().addItem("foo");
        menuBar.remove(item);
    }

    @Test
    public void openOnHover_falseByDefault() {
        Assert.assertFalse(menuBar.isOpenOnHover());
    }

    @Test
    public void setOpenOnHover_isOpenOnHover() {
        menuBar.setOpenOnHover(true);
        Assert.assertTrue(menuBar.isOpenOnHover());
    }

    private void assertChildrenAndItems(MenuItem... expected) {
        Object[] menuItems = menuBar.getChildren().toArray();
        Assert.assertArrayEquals(expected, menuItems);

        menuItems = menuBar.getItems().toArray();
        Assert.assertArrayEquals(expected, menuItems);
    }
}
