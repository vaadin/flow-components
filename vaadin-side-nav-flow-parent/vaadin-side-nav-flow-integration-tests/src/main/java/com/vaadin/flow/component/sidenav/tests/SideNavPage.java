/**
 * Copyright 2000-2026 Vaadin Ltd.
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

/**
 * Test view for testing {@link SideNav} component
 */
@Route("vaadin-side-nav/side-nav-test")
public class SideNavPage extends Div {

    public SideNavPage() {
        setMaxWidth("400px");

        SideNav sideNav = new SideNav();
        sideNav.setLabel("Navigation Test");
        sideNav.setCollapsible(true);
        add(sideNav);

        SideNavItem nonNavigableParent = new SideNavItem(
                "Non-navigable parent");
        nonNavigableParent.setId("non-navigable-parent");
        nonNavigableParent.addItem(new SideNavItem("Item 1"));
        nonNavigableParent.addItem(new SideNavItem("Item 2"));
        nonNavigableParent.addItem(new SideNavItem("Item 3"));
        sideNav.addItem(nonNavigableParent);

        SideNavItem navigableParent = new SideNavItem("Navigable parent",
                "vaadin-side-nav/side-nav-test-target-view");
        navigableParent.setId("navigable-parent");
        navigableParent.addItem(new SideNavItem("Item 1",
                "vaadin-side-nav/side-nav-test-target-view"));
        navigableParent.addItem(new SideNavItem("Item 2"));
        sideNav.addItem(navigableParent);

        SideNavItem currentItem = new SideNavItem("Current item",
                "vaadin-side-nav/side-nav-test");
        currentItem.setId("current-item");
        sideNav.addItem(currentItem);

        Div expandedStatePrintout = new Div();
        expandedStatePrintout.setId("expanded-state-printout");
        add(expandedStatePrintout);

        NativeButton printItemExpandedState = new NativeButton(
                "Print out item expanded state", event -> expandedStatePrintout
                        .setText(String.valueOf(navigableParent.isExpanded())));
        printItemExpandedState.setId("print-item-expanded-state");
        add(printItemExpandedState);

        NativeButton printExpandedState = new NativeButton(
                "Print out side-nav expanded state",
                event -> expandedStatePrintout
                        .setText(String.valueOf(sideNav.isExpanded())));
        printExpandedState.setId("print-side-nav-expanded-state");
        add(printExpandedState);

    }
}
