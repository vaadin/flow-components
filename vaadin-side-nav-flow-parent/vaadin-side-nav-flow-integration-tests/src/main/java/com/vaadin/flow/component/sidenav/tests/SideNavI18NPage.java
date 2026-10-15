/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.sidenav.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;

@Route("vaadin-side-nav/side-nav-i18n")
public class SideNavI18NPage extends Div {

    public SideNavI18NPage() {
        SideNav sideNav = new SideNav();
        sideNav.addItem(new SideNavItem("Item 1"));
        sideNav.addItem(new SideNavItem("Item 2"));

        NativeButton setI18n = new NativeButton("Set i18n", e -> sideNav
                .setI18n(new SideNav.SideNavI18n().setToggle("Updated")));
        setI18n.setId("set-i18n");

        add(sideNav, setI18n);
    }
}
