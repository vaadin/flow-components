/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
