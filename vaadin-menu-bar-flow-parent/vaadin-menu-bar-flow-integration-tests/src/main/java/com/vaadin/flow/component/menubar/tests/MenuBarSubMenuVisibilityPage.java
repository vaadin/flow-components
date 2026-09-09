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

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

@Route("vaadin-menu-bar/sub-menu-visibility")
public class MenuBarSubMenuVisibilityPage extends Div {

    public MenuBarSubMenuVisibilityPage() {
        add(createRootItemLayout(), createSubItemLayout());
    }

    private Div createRootItemLayout() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("root-item-menu-bar");
        MenuItem rootItem = menuBar.addItem("Root item");
        rootItem.getSubMenu().addItem("Sub item 1");
        rootItem.getSubMenu().addItem("Sub item 2");
        rootItem.setVisible(false);

        NativeButton showRootItem = new NativeButton("Show root item",
                event -> rootItem.setVisible(true));
        showRootItem.setId("show-root-item");

        return new Div(menuBar, showRootItem);
    }

    private Div createSubItemLayout() {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("sub-item-menu-bar");
        MenuItem rootItem = menuBar.addItem("Root item");
        MenuItem subItem = rootItem.getSubMenu().addItem("Sub item");
        subItem.getSubMenu().addItem("Sub sub item");
        subItem.setVisible(false);

        NativeButton showSubItem = new NativeButton("Show sub item",
                event -> subItem.setVisible(true));
        showSubItem.setId("show-sub-item");

        return new Div(menuBar, showSubItem);
    }
}
