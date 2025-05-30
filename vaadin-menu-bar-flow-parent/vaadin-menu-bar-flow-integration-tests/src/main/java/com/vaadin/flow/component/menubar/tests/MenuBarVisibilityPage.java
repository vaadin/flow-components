/*
 * Copyright 2000-2025 Vaadin Ltd.
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

@Route("vaadin-menu-bar/menu-bar-visibility")
public class MenuBarVisibilityPage extends Div {
    public MenuBarVisibilityPage() {
        MenuBar menuBar = new MenuBar();
        MenuItem menuItem = menuBar.addItem("Item 1");
        MenuItem menuItem2 = menuBar.addItem("Item 2");
        menuItem2.setVisible(false);

        NativeButton toggleMenuBarVisibility = new NativeButton(
                "Toggle menu bar visibility", (event) -> {
                    menuBar.setVisible(!menuBar.isVisible());
                });
        toggleMenuBarVisibility.setId("toggle-menu-bar-visibility");

        NativeButton toggleMenuItemVisibility = new NativeButton(
                "Toggle item 1 visibility", (event) -> {
                    menuItem.setVisible(!menuItem.isVisible());
                });
        toggleMenuItemVisibility.setId("toggle-menu-item-visibility");

        NativeButton toggleOtherItemVisibility = new NativeButton(
                "Toggle item 2 visibility", (event) -> {
                    menuItem2.setVisible(!menuItem2.isVisible());
                });
        toggleOtherItemVisibility.setId("toggle-other-item-visibility");

        NativeButton toggleMenuItemEnabled = new NativeButton(
                "Toggle menu item enabled", (event) -> {
                    menuItem.setEnabled(!menuItem.isEnabled());
                });
        toggleMenuItemEnabled.setId("toggle-menu-item-enabled");

        add(menuBar, toggleMenuBarVisibility, toggleMenuItemVisibility,
                toggleOtherItemVisibility, toggleMenuItemEnabled);
    }
}
