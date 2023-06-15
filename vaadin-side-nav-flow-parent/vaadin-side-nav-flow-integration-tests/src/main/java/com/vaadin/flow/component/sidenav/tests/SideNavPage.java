/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        sideNav.addItem(nonNavigableParent);

        SideNavItem labelOnly = new SideNavItem("Label only");
        labelOnly.setId("label-only");
        nonNavigableParent.addItem(labelOnly);

        SideNavItem classTarget = new SideNavItem("Target using class",
                SideNavTargetView.class);
        classTarget.setId("class-target");
        nonNavigableParent.addItem(classTarget);

        Avatar vaadinAvatar = new Avatar("Vaadin");
        SideNavItem classTargetWithComponent = new SideNavItem(
                "Target using class with component", SideNavTargetView.class,
                vaadinAvatar);
        classTargetWithComponent.setId("class-target-prefix-component");
        nonNavigableParent.addItem(classTargetWithComponent);

        SideNavItem navigableParent = new SideNavItem("Navigable parent",
                "vaadin-side-nav/side-nav-test-target-view");
        navigableParent.setId("navigable-parent");
        sideNav.addItem(navigableParent);

        SideNavItem pathTarget = new SideNavItem("Target using path",
                "vaadin-side-nav/side-nav-test-target-view");
        pathTarget.setId("path-target");
        navigableParent.addItem(pathTarget);

        SideNavItem pathTargetWithIcon = new SideNavItem(
                "Target using path with icon",
                "vaadin-side-nav/side-nav-test-target-view",
                VaadinIcon.GLOBE.create());
        pathTargetWithIcon.setId("path-target-icon");
        navigableParent.addItem(pathTargetWithIcon);

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

        NativeButton test = new NativeButton("test",
                event -> navigableParent.setLabel(null));
        add(test);

        NativeButton test2 = new NativeButton("test2",
                event -> navigableParent.setLabel(""));
        add(test2);

        NativeButton test3 = new NativeButton("test3",
                event -> navigableParent.setLabel("aaa"));
        add(test3);

        NativeButton addMatchingPathAliasToNonNavigableParent = new NativeButton(
                "Add matching path alias to non-navigable parent",
                event -> nonNavigableParent
                        .addPathAlias("vaadin-side-nav/side-nav-test"));
        addMatchingPathAliasToNonNavigableParent
                .setId("add-matching-path-alias-to-non-navigable-parent");
        add(addMatchingPathAliasToNonNavigableParent);

        NativeButton setDummyPathToNonNavigableParent = new NativeButton(
                "Set dummy path to non-navigable parent",
                event -> nonNavigableParent.setPath("dummy-path"));
        setDummyPathToNonNavigableParent
                .setId("set-dummy-path-to-non-navigable-parent");
        add(setDummyPathToNonNavigableParent);
    }
}
