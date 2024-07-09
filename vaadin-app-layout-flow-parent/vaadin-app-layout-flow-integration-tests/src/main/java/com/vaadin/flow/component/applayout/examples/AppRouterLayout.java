/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@BodySize
@Theme(Lumo.class)
public class AppRouterLayout extends AppLayout {

    public static final String CUSTOM_TOGGLE_ID = "toggle-with-icon";
    public static final String CUSTOM_ICON_ID = "custom-icon";

    public AppRouterLayout() {
        RouterLink home = new RouterLink("Home", Home.class);
        RouterLink page1 = new RouterLink("Page 1", Page1.class);
        RouterLink page2 = new RouterLink("Page 2", Page2.class);
        VerticalLayout layout = new VerticalLayout(home, page1, page2);
        addToDrawer(layout);
        addToNavbar(new DrawerToggle());
        DrawerToggle customToggle = new DrawerToggle();
        customToggle.setId(CUSTOM_TOGGLE_ID);
        Icon icon = VaadinIcon.FLIP_H.create();
        icon.setId(CUSTOM_ICON_ID);
        customToggle.setIcon(icon);
        addToNavbar(customToggle);
    }
}
