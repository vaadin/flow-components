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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for SideNav component.
 */
@Route(value = "side-nav", layout = MainLayout.class)
@PageTitle("Side Nav | Vaadin Kitchen Sink")
public class SideNavDemoView extends VerticalLayout {

    public SideNavDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Side Nav Component"));
        add(new Paragraph("SideNav provides hierarchical navigation typically used in sidebars."));

        HorizontalLayout examples = new HorizontalLayout();
        examples.setSpacing(true);
        examples.setWidthFull();

        // Basic side nav
        Div basicContainer = createNavContainer("Basic Side Nav");
        SideNav basic = new SideNav();
        basic.addItem(new SideNavItem("Dashboard"));
        basic.addItem(new SideNavItem("Orders"));
        basic.addItem(new SideNavItem("Customers"));
        basic.addItem(new SideNavItem("Products"));
        basic.addItem(new SideNavItem("Settings"));
        basicContainer.add(basic);
        examples.add(basicContainer);

        // With icons
        Div iconContainer = createNavContainer("With Icons");
        SideNav withIcons = new SideNav();
        withIcons.addItem(createNavItem("Home", VaadinIcon.HOME));
        withIcons.addItem(createNavItem("Users", VaadinIcon.USERS));
        withIcons.addItem(createNavItem("Messages", VaadinIcon.ENVELOPE));
        withIcons.addItem(createNavItem("Calendar", VaadinIcon.CALENDAR));
        withIcons.addItem(createNavItem("Settings", VaadinIcon.COG));
        iconContainer.add(withIcons);
        examples.add(iconContainer);

        add(examples);

        // Hierarchical navigation
        Div hierarchicalContainer = createNavContainer("Hierarchical Navigation");
        hierarchicalContainer.setWidth("300px");
        SideNav hierarchical = new SideNav();

        SideNavItem dashboard = createNavItem("Dashboard", VaadinIcon.DASHBOARD);
        hierarchical.addItem(dashboard);

        SideNavItem products = createNavItem("Products", VaadinIcon.PACKAGE);
        products.addItem(new SideNavItem("All Products"));
        products.addItem(new SideNavItem("Categories"));
        products.addItem(new SideNavItem("Inventory"));
        hierarchical.addItem(products);

        SideNavItem orders = createNavItem("Orders", VaadinIcon.CART);
        orders.addItem(new SideNavItem("All Orders"));
        orders.addItem(new SideNavItem("Pending"));
        orders.addItem(new SideNavItem("Completed"));
        orders.addItem(new SideNavItem("Returns"));
        hierarchical.addItem(orders);

        SideNavItem customers = createNavItem("Customers", VaadinIcon.USERS);
        customers.addItem(new SideNavItem("All Customers"));
        customers.addItem(new SideNavItem("VIP"));
        hierarchical.addItem(customers);

        SideNavItem reports = createNavItem("Reports", VaadinIcon.CHART);
        reports.addItem(new SideNavItem("Sales"));
        reports.addItem(new SideNavItem("Revenue"));
        reports.addItem(new SideNavItem("Traffic"));
        hierarchical.addItem(reports);

        SideNavItem settings = createNavItem("Settings", VaadinIcon.COG);
        hierarchical.addItem(settings);

        hierarchicalContainer.add(hierarchical);
        addSection("Hierarchical Navigation", hierarchicalContainer);

        // Collapsible sections
        Div collapsibleContainer = createNavContainer("Collapsible Sections");
        collapsibleContainer.setWidth("300px");
        SideNav collapsible = new SideNav();

        collapsible.setLabel("Main Menu");

        SideNavItem mainSection = new SideNavItem("Main");
        mainSection.setPrefixComponent(VaadinIcon.HOME.create());
        mainSection.addItem(new SideNavItem("Dashboard"));
        mainSection.addItem(new SideNavItem("Analytics"));
        mainSection.setExpanded(true);
        collapsible.addItem(mainSection);

        SideNavItem managementSection = new SideNavItem("Management");
        managementSection.setPrefixComponent(VaadinIcon.COGS.create());
        managementSection.addItem(new SideNavItem("Users"));
        managementSection.addItem(new SideNavItem("Roles"));
        managementSection.addItem(new SideNavItem("Permissions"));
        collapsible.addItem(managementSection);

        SideNavItem contentSection = new SideNavItem("Content");
        contentSection.setPrefixComponent(VaadinIcon.FILE_TEXT.create());
        contentSection.addItem(new SideNavItem("Pages"));
        contentSection.addItem(new SideNavItem("Posts"));
        contentSection.addItem(new SideNavItem("Media"));
        collapsible.addItem(contentSection);

        collapsibleContainer.add(collapsible);
        addSection("Collapsible Sections", collapsibleContainer);
    }

    private SideNavItem createNavItem(String label, VaadinIcon icon) {
        SideNavItem item = new SideNavItem(label);
        item.setPrefixComponent(icon.create());
        return item;
    }

    private Div createNavContainer(String title) {
        Div container = new Div();
        container.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Padding.MEDIUM);
        container.setWidth("250px");

        Paragraph titleP = new Paragraph(title);
        titleP.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.Margin.Bottom.SMALL);
        container.add(titleP);

        return container;
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
