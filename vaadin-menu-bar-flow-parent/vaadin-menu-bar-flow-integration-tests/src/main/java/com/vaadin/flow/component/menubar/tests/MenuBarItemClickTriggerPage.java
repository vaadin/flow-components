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
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.trigger.MenuBarItemClickTrigger;
import com.vaadin.flow.component.trigger.internal.SetPropertyAction;
import com.vaadin.flow.router.Route;

@Route("vaadin-menu-bar/menu-bar-item-click-trigger")
public class MenuBarItemClickTriggerPage extends Div {

    static final String RESULT_ID = "result";
    static final String ROOT_MESSAGE = "root clicked";
    static final String SUB_MESSAGE = "sub clicked";

    public MenuBarItemClickTriggerPage() {
        var result = new Div();
        result.setId(RESULT_ID);
        add(result);

        var menuBar = new MenuBar();

        MenuItem rootItem = menuBar.addItem("Set root");
        new MenuBarItemClickTrigger(rootItem).triggers(
                new SetPropertyAction<>(result, "textContent", ROOT_MESSAGE));

        MenuItem parent = menuBar.addItem("File");
        MenuItem subItem = parent.getSubMenu().addItem("Set sub");
        new MenuBarItemClickTrigger(subItem).triggers(
                new SetPropertyAction<>(result, "textContent", SUB_MESSAGE));

        add(menuBar);
    }
}
