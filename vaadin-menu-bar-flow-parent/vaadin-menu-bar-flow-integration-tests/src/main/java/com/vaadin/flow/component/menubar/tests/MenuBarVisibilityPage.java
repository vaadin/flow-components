/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
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
        MenuItem menuItem = menuBar.addItem("Item");

        NativeButton toggleMenuBarVisibility = new NativeButton(
                "Toggle menu bar visibility", (event) -> {
                    menuBar.setVisible(!menuBar.isVisible());
                });
        toggleMenuBarVisibility.setId("toggle-menu-bar-visibility");

        NativeButton toggleMenuItemVisibility = new NativeButton(
                "Toggle menu item visibility", (event) -> {
                    menuItem.setVisible(!menuItem.isVisible());
                });
        toggleMenuItemVisibility.setId("toggle-menu-item-visibility");

        NativeButton toggleMenuItemEnabled = new NativeButton(
                "Toggle menu item enabled", (event) -> {
                    menuItem.setEnabled(!menuItem.isEnabled());
                });
        toggleMenuItemEnabled.setId("toggle-menu-item-enabled");

        add(menuBar, toggleMenuBarVisibility, toggleMenuItemVisibility,
                toggleMenuItemEnabled);
    }
}
