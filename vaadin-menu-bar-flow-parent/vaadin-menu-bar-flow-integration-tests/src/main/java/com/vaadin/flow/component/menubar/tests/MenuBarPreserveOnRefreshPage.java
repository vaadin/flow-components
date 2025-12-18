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
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-menu-bar/preserve-on-refresh")
@PreserveOnRefresh
public class MenuBarPreserveOnRefreshPage extends Div {

    public MenuBarPreserveOnRefreshPage() {
        MenuBar menuBar = new MenuBar();

        MenuItem item1 = menuBar.addItem("item 1");
        MenuItem item2 = menuBar.addItem(new Span("item 2"));

        NativeButton toggleItem2VisibilityButton = new NativeButton(
                "toggle item 2 visibility",
                e -> item2.setVisible(!item2.isVisible()));
        toggleItem2VisibilityButton.setId("toggle-item-2-visibility");

        add(toggleItem2VisibilityButton);

        add(new Hr(), menuBar);
    }

}
