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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for Tabs and TabSheet components.
 */
@Route(value = "tabs", layout = MainLayout.class)
@PageTitle("Tabs | Vaadin Kitchen Sink")
public class TabsDemoView extends VerticalLayout {

    public TabsDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Tabs Components"));
        add(new Paragraph("Tabs organize content into separate views."));

        // Basic tabs
        Tabs basic = new Tabs(
            new Tab("Dashboard"),
            new Tab("Orders"),
            new Tab("Customers"),
            new Tab("Products")
        );
        addSection("Basic Tabs", basic);

        // Tabs with icons
        Tab homeTab = new Tab(VaadinIcon.HOME.create(), new Paragraph("Home"));
        Tab settingsTab = new Tab(VaadinIcon.COG.create(), new Paragraph("Settings"));
        Tab userTab = new Tab(VaadinIcon.USER.create(), new Paragraph("Profile"));
        Tabs withIcons = new Tabs(homeTab, settingsTab, userTab);
        addSection("Tabs with Icons", withIcons);

        // Icon-only tabs
        Tabs iconOnly = new Tabs(
            createIconTab(VaadinIcon.ENVELOPE, "Mail"),
            createIconTab(VaadinIcon.CALENDAR, "Calendar"),
            createIconTab(VaadinIcon.BELL, "Notifications"),
            createIconTab(VaadinIcon.COG, "Settings")
        );
        addSection("Icon-only Tabs", iconOnly);

        // Small variant
        Tabs small = new Tabs(
            new Tab("Tab 1"),
            new Tab("Tab 2"),
            new Tab("Tab 3")
        );
        small.addThemeVariants(TabsVariant.LUMO_SMALL);
        addSection("Small Variant", small);

        // Centered tabs
        Tabs centered = new Tabs(
            new Tab("First"),
            new Tab("Second"),
            new Tab("Third")
        );
        centered.addThemeVariants(TabsVariant.LUMO_CENTERED);
        addSection("Centered Tabs", centered);

        // Equal width tabs
        Tabs equalWidth = new Tabs(
            new Tab("Tab A"),
            new Tab("Tab B"),
            new Tab("Tab C")
        );
        equalWidth.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        equalWidth.setWidthFull();
        addSection("Equal Width Tabs", equalWidth);

        // Vertical tabs
        Tabs vertical = new Tabs(
            new Tab("Overview"),
            new Tab("Details"),
            new Tab("History"),
            new Tab("Settings")
        );
        vertical.setOrientation(Tabs.Orientation.VERTICAL);
        vertical.setHeight("200px");
        addSection("Vertical Tabs", vertical);

        // With disabled tab
        Tab enabledTab1 = new Tab("Enabled");
        Tab disabledTab = new Tab("Disabled");
        disabledTab.setEnabled(false);
        Tab enabledTab2 = new Tab("Also Enabled");
        Tabs withDisabled = new Tabs(enabledTab1, disabledTab, enabledTab2);
        addSection("With Disabled Tab", withDisabled);

        // With event listener
        Tabs withEvent = new Tabs(
            new Tab("First"),
            new Tab("Second"),
            new Tab("Third")
        );
        withEvent.addSelectedChangeListener(event ->
            Notification.show("Selected: " + event.getSelectedTab().getLabel()));
        addSection("With Event Listener", withEvent);

        // TabSheet (tabs with content)
        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Dashboard", createContent("Dashboard content goes here."));
        tabSheet.add("Orders", createContent("Order management interface."));
        tabSheet.add("Customers", createContent("Customer list and details."));
        tabSheet.add("Reports", createContent("Sales and analytics reports."));
        tabSheet.setWidthFull();
        addSection("TabSheet (Tabs with Content)", tabSheet);

        // TabSheet with prefix/suffix
        TabSheet richTabSheet = new TabSheet();
        Tab tab1 = new Tab("Home");
        tab1.addComponentAsFirst(VaadinIcon.HOME.create());
        richTabSheet.add(tab1, createContent("Welcome home!"));

        Tab tab2 = new Tab("Messages");
        tab2.addComponentAsFirst(VaadinIcon.ENVELOPE.create());
        richTabSheet.add(tab2, createContent("Your messages."));

        Tab tab3 = new Tab("Settings");
        tab3.addComponentAsFirst(VaadinIcon.COG.create());
        richTabSheet.add(tab3, createContent("Application settings."));

        richTabSheet.setWidthFull();
        addSection("TabSheet with Icons", richTabSheet);
    }

    private Tab createIconTab(VaadinIcon icon, String ariaLabel) {
        Tab tab = new Tab(icon.create());
        tab.setAriaLabel(ariaLabel);
        return tab;
    }

    private Div createContent(String text) {
        Div content = new Div();
        content.setText(text);
        content.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.MEDIUM);
        content.setMinHeight("100px");
        return content;
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
