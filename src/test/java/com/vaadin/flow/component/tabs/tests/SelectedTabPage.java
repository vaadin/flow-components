/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;

@Route("selected-tab")
public class SelectedTabPage extends Div {

    public SelectedTabPage() {
        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("foo");
        tabs.add(tab1);
        Tab tab2 = new Tab("bar");
        tabs.add(tab2);
        tab2.setId("second");

        NativeButton button = new NativeButton("Show tabs selection", event -> {
            Div div = new Div();
            div.addClassName("first");
            div.setText("The first tab is selected: " + tab1.isSelected());
            add(div);
            div = new Div();
            div.addClassName("second");
            div.setText("The second tab is selected: " + tab2.isSelected());
            add(div);
        });

        button.setId("show-selection");

        NativeButton delete = new NativeButton("Delete selected tab",
                event -> tabs.remove(tabs.getSelectedTab()));
        delete.setId("delete");

        NativeButton add = new NativeButton("Add new tab as the first",
                event -> tabs.addComponentAsFirst(new Tab("baz")));
        add.setId("add");

        Div selectedTab = new Div();
        tabs.addSelectedChangeListener(
                event -> selectedTab.setText(tabs.getSelectedTab().getLabel()));

        selectedTab.setId("selection-event");

        add(tabs, button, delete, add, selectedTab);
    }
}
