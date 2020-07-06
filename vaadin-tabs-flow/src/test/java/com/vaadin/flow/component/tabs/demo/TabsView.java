/*
 * Copyright 2000-2017 Vaadin Ltd.
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

package com.vaadin.flow.component.tabs.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Tabs} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-tabs")
public class TabsView extends DemoView {

    @Override
    public void initView() {
        createHorizontalTabs();
        createVerticalTabs();
        createScrollableHorizontalTabs();
        createScrollableVerticalTabs();
        createDisabledTabs();
        createFullWidthTabs();
        createPreselectedTabs();
        createTabsWithPages();
        createTabsWithCustomContent();
    }

    private void createHorizontalTabs() {
        // begin-source-example
        // source-example-heading: Horizontal tabs
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);
        // end-source-example

        tabs.setId("horizontal-tabs");
        addCard("Horizontal tabs", tabs);
    }

    private void createVerticalTabs() {
        // begin-source-example
        // source-example-heading: Vertical tabs
        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        tabs.add(tab1, tab2, tab3);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        // end-source-example

        tabs.setId("vertical-tabs");
        addCard("Vertical tabs", tabs);
    }

    private void createScrollableHorizontalTabs() {
        // begin-source-example
        // source-example-heading: Scrollable horizontal tabs
        Tabs tabs = new Tabs(new Tab("Tab one"), new Tab("Tab two"),
                new Tab("Tab three"), new Tab("Tab four"), new Tab("Tab five"),
                new Tab("Tab six"), new Tab("Tab seven"), new Tab("Tab eight"),
                new Tab("Tab nine"), new Tab("Tab ten"), new Tab("Tab eleven"),
                new Tab("Tab twelve"), new Tab("Tab thirteen"),
                new Tab("Tab fourteen"), new Tab("Tab fifteen"));
        // end-source-example

        tabs.setId("scrollable-horizontal-tabs");
        addCard("Scrollable horizontal tabs", tabs);
    }

    private void createScrollableVerticalTabs() {
        // begin-source-example
        // source-example-heading: Scrollable vertical tabs
        Tabs tabs = new Tabs(new Tab("Tab one"), new Tab("Tab two"),
                new Tab("Tab three"), new Tab("Tab four"), new Tab("Tab five"),
                new Tab("Tab six"), new Tab("Tab seven"), new Tab("Tab eight"),
                new Tab("Tab nine"), new Tab("Tab ten"), new Tab("Tab eleven"),
                new Tab("Tab twelve"), new Tab("Tab thirteen"),
                new Tab("Tab fourteen"), new Tab("Tab fifteen"));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setHeight("130px");
        // end-source-example

        tabs.setId("scrollable-vertical-tabs");
        addCard("Scrollable vertical tabs", tabs);
    }

    private void createDisabledTabs() {
        // begin-source-example
        // source-example-heading: Disabled tabs
        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Disabled tab");
        tab3.setEnabled(false);
        Tab tab4 = new Tab("Tab four");
        Tab tab5 = new Tab("Tab five");
        tabs.add(tab1, tab2, tab3, tab4, tab5);
        // end-source-example

        tabs.setId("disabled-tabs");
        addCard("Disabled tabs", tabs);
    }

    private void createFullWidthTabs() {
        // begin-source-example
        // source-example-heading: Tabs covering the full width of the tab bar
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        Tabs tabs = new Tabs(tab1, tab2, tab3);
        tabs.setFlexGrowForEnclosedTabs(1);
        // end-source-example

        tabs.setId("full-width-tabs");
        addCard("Tabs covering the full width of the tab bar", tabs);
    }

    private void createPreselectedTabs() {
        // begin-source-example
        // source-example-heading: Pre-selected tabs
        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        tabs.add(tab1, tab2, tab3);
        tabs.setSelectedTab(tab2);
        // end-source-example

        tabs.setId("preselected-tabs");
        addCard("Pre-selected tabs", tabs);
    }

    private void createTabsWithPages() {
        // begin-source-example
        // source-example-heading: Tabs with pages
        Tab tab1 = new Tab("Tab one");
        Div page1 = new Div();
        page1.setText("Page#1");

        Tab tab2 = new Tab("Tab two");
        Div page2 = new Div();
        page2.setText("Page#2");
        page2.setVisible(false);

        Tab tab3 = new Tab("Tab three");
        Div page3 = new Div();
        page3.setText("Page#3");
        page3.setVisible(false);

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        tabsToPages.put(tab3, page3);
        Tabs tabs = new Tabs(tab1, tab2, tab3);
        Div pages = new Div(page1, page2, page3);
        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());

        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });
        // end-source-example

        tabs.setId("tabs-with-pages");
        tab1.setId("tab1");
        tab2.setId("tab2");
        tab3.setId("tab3");
        page1.setId("page1");
        page2.setId("page2");
        page3.setId("page3");
        addCard("Tabs with pages", tabs, pages);
    }

    private void createTabsWithCustomContent() {
        // begin-source-example
        // source-example-heading: Tabs with custom content
        Image image1 = new Image(
                "https://api.adorable.io/avatars/100/peter.png", "Peter");
        image1.setWidth("24px");
        image1.setHeight("24px");
        image1.getStyle().set("borderRadius", "50%");
        Span badge1 = new Span("cool guy");
        badge1.getStyle().set("fontSize", "75%");
        VerticalLayout layout1 = new VerticalLayout(badge1, image1);
        layout1.getStyle().set("alignItems", "center");
        Tab tab1 = new Tab(layout1);
        Tab tab2 = new Tab(new Checkbox("What?"));
        Tab tab3 = new Tab(new Icon(VaadinIcon.COG));
        Tabs tabs = new Tabs(tab1, tab2, tab3);
        // end-source-example

        tabs.setId("tabs-with-custom-content");
        addCard("Tabs with custom content", tabs);
    }
}
