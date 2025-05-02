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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

@CssImport("./menu-bar-not-animated-styles.css")
@Route("vaadin-menu-bar/menu-bar-theme")
public class MenuBarThemePage extends Div {

    public static final String MENU_BAR_THEME = "menu-bar-theme";
    public static final String MENU_ITEM_THEME = "menu-item-theme";
    public static final String SUB_ITEM_THEME = "sub-item-theme";

    public MenuBarThemePage() {
        MenuBar menuBar = new MenuBar();

        MenuItem item1 = menuBar.addItem("item 1");
        MenuItem item2 = menuBar.addItem(new Span("item 2"));

        item1.getSubMenu().addItem("sub item 1");
        MenuItem subItem2 = item1.getSubMenu().addItem(new Span("sub item 2"));

        MenuItem subItem3 = item1.getSubMenu().addItem(new Span("sub item 3"));

        subItem2.getSubMenu().addItem(new Span("sub sub item 1"));

        NativeButton setWidthButton = new NativeButton("set width 140px", e -> {
            setWidth("140px");
        });
        setWidthButton.setId("set-width");

        NativeButton resetWidthButton = new NativeButton("reset width", e -> {
            setWidth("auto");
        });
        resetWidthButton.setId("reset-width");

        NativeButton toggleItem1VisibilityButton = new NativeButton(
                "toggle item 1 visibility",
                e -> item1.setVisible(!item1.isVisible()));
        toggleItem1VisibilityButton.setId("toggle-item-1-visibility");

        NativeButton toggleMenuBarThemeButton = new NativeButton("toggle theme",
                e -> {
                    if (menuBar.hasThemeName(MENU_BAR_THEME)) {
                        menuBar.removeThemeName(MENU_BAR_THEME);
                    } else {
                        menuBar.addThemeName(MENU_BAR_THEME);
                    }
                });
        toggleMenuBarThemeButton.setId("toggle-theme");

        NativeButton toggleItem1ThemeButton = new NativeButton(
                "toggle item theme", e -> {
                    if (item1.hasThemeName(MENU_ITEM_THEME)) {
                        item1.removeThemeNames(MENU_ITEM_THEME);
                    } else {
                        item1.addThemeNames(MENU_ITEM_THEME);
                    }
                });
        toggleItem1ThemeButton.setId("toggle-item-1-theme");

        NativeButton toggleSubItemThemeButton = new NativeButton(
                "toggle sub theme", e -> {
                    if (subItem2.hasThemeName(SUB_ITEM_THEME)) {
                        subItem2.removeThemeNames(SUB_ITEM_THEME);
                    } else {
                        subItem2.addThemeNames(SUB_ITEM_THEME);
                    }
                });
        toggleSubItemThemeButton.setId("toggle-sub-theme");

        add(setWidthButton, resetWidthButton, toggleItem1VisibilityButton,
                toggleMenuBarThemeButton, toggleItem1ThemeButton,
                toggleSubItemThemeButton);

        add(new Hr(), menuBar);
    }
}
