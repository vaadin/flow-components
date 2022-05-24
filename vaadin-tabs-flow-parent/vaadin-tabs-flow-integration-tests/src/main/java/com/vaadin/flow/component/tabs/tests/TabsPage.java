/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Tabs} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-tabs/tabs")
public class TabsPage extends Div {

    public TabsPage() {
        createTabsWithPages();
        createTabsWithThemeVariants();
    }

    private void createTabsWithPages() {
        Tab tab1 = new Tab("Tab one");
        Div page1 = new Div();
        page1.setText("Page#1");

        Tab tab2 = new Tab("Tab two");
        Div page2 = new Div();
        page2.setText("Page#2");

        Tab tab3 = new Tab("Tab three");
        Div page3 = new Div();
        page3.setText("Page#3");

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        tabsToPages.put(tab3, page3);
        Tabs tabs = new Tabs(tab1, tab2, tab3);
        Div pages = new Div(page1);

        tabs.addSelectedChangeListener(event -> {
            pages.removeAll();
            pages.add(tabsToPages.get(event.getSelectedTab()));
        });

        tabs.setId("tabs-with-pages");
        tab1.setId("tab1");
        tab2.setId("tab2");
        tab3.setId("tab3");
        page1.setId("page1");
        page2.setId("page2");
        page3.setId("page3");
        addCard("Tabs with pages", tabs, pages);
    }

    private void createTabsWithThemeVariants() {
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
        tabs.setId("tabs-with-theme");

        NativeButton removeVariantButton = new NativeButton(
                "Remove theme variant", e -> {
                    tabs.removeThemeVariants(TabsVariant.LUMO_SMALL);
                });
        removeVariantButton.setId("remove-theme-variant-button");
        addCard("Tabs theme variant", tabs, removeVariantButton);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
