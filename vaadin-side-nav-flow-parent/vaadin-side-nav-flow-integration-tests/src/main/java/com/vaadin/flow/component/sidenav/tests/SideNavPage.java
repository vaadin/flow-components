/*
 * Copyright 2000-2024 Vaadin Ltd.
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
                SideNavTargetView.class);
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
