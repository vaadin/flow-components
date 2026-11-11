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
package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-tabs/repro-7882")
public class Repro7882View extends Div {

    public Repro7882View() {
        // Failing case from the issue: TabSheet
        TabSheet tabSheet = new TabSheet();
        tabSheet.setId("tabsheet");
        tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.add("Tab 2", new Span("Content 2"));

        // Control: plain Tabs
        Tabs tabs = new Tabs(new Tab("Tab A"), new Tab("Tab B"));
        tabs.setId("tabs");

        Span state = new Span();
        state.setId("state");
        Runnable update = () -> state
                .setText("tabsheet=" + tabSheet.getSelectedIndex() + " tabs="
                        + tabs.getSelectedIndex() + " count="
                        + tabSheet.getTabCount() + "/" + tabs.getTabCount());
        update.run();
        tabSheet.addSelectedChangeListener(e -> update.run());
        tabs.addSelectedChangeListener(e -> update.run());

        NativeButton removeSheet = new NativeButton("Remove first TabSheet tab",
                e -> {
                    tabSheet.remove(0);
                    update.run();
                });
        removeSheet.setId("remove-tabsheet");
        NativeButton removeTabs = new NativeButton("Remove first Tabs tab",
                e -> {
                    tabs.remove(tabs.getTabAt(0));
                    update.run();
                });
        removeTabs.setId("remove-tabs");
        NativeButton refresh = new NativeButton("Show server state",
                e -> update.run());
        refresh.setId("show-state");

        add(new Div(removeSheet, removeTabs, refresh), state, tabSheet, tabs);
    }
}
