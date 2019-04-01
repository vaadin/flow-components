package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@BodySize
@Theme(Lumo.class)
public class AppRouterLayout extends AppLayout {

    {
        final DrawerToggle drawerToggle = new DrawerToggle();
        final RouterLink home = new RouterLink("Home", Home.class);
        final RouterLink page1 = new RouterLink("Page 1", Page1.class);
        final RouterLink page2 = new RouterLink("Page 2", Page2.class);
        final VerticalLayout layout = new VerticalLayout(home, page1, page2);
        addToDrawer(layout);
        addToNavbar(drawerToggle);
    }
}
