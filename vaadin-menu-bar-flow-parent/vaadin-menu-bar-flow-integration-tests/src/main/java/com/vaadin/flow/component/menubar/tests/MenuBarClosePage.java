/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

/**
 * IT page for testing the close method.
 */
@Route("vaadin-menu-bar/close")
public class MenuBarClosePage extends Div {

    public MenuBarClosePage() {
        MenuBar menuBar = new MenuBar();
        final SubMenu submenu = menuBar.addItem("Open").getSubMenu();
        final NativeButton closeButton = new NativeButton("Close",
                e -> menuBar.close());
        closeButton.setId("close-button");
        submenu.add(new Div(new Span("menu contents"), closeButton));
        add(menuBar);
    }
}
