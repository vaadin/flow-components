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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarItemElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarSubMenuElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/sub-menu-visibility")
public class MenuBarSubMenuVisibilityIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void hiddenRootItem_show_subMenuRenders() {
        MenuBarElement menuBar = $(MenuBarElement.class)
                .id("root-item-menu-bar");
        Assert.assertTrue("Expected no buttons while the root item is hidden",
                menuBar.getButtons().isEmpty());

        $("button").id("show-root-item").click();

        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(0)
                .openSubMenu();
        Assert.assertArrayEquals(new String[] { "Sub item 1", "Sub item 2" },
                getMenuItemContents(subMenu.getMenuItems()));
    }

    @Test
    public void hiddenSubItem_show_subSubMenuRenders() {
        MenuBarElement menuBar = $(MenuBarElement.class)
                .id("sub-item-menu-bar");

        $("button").id("show-sub-item").click();

        MenuBarSubMenuElement subMenu = menuBar.getButtons().get(0)
                .openSubMenu();
        MenuBarSubMenuElement subSubMenu = subMenu.getMenuItem("Sub item")
                .orElseThrow().openSubMenu();
        Assert.assertArrayEquals(new String[] { "Sub sub item" },
                getMenuItemContents(subSubMenu.getMenuItems()));
    }

    private String[] getMenuItemContents(List<MenuBarItemElement> menuItems) {
        return menuItems.stream().map(MenuBarItemElement::getText)
                .toArray(String[]::new);
    }
}
