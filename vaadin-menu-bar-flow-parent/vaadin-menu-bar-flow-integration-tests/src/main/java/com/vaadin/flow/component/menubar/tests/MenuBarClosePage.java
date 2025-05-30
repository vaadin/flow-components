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
        submenu.addComponent(new Div(new Span("menu contents"), closeButton));
        add(menuBar);
    }
}
