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

@Route("vaadin-side-nav/side-nav-hierarchy-test")
public class SideNavHierarchyPage extends Div {

    public SideNavHierarchyPage() {
        setMaxWidth("400px");

        SideNav sideNav = new SideNav();
        sideNav.setLabel("Navigation");
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
        classTargetWithComponent.setId("class-target-component");
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

        NativeButton addSubItem = new NativeButton("Add item to 2nd level",
                event -> navigableParent.addItem(new SideNavItem("Added item",
                        "vaadin-side-nav/side-nav-test-target-view")));
        addSubItem.setId("add-sub-item");
        add(addSubItem);

        NativeButton addItem = new NativeButton("Remove item from 2nd level",
                event -> navigableParent.removeItem(pathTarget));
        addItem.setId("remove-sub-item");
        add(addItem);

        NativeButton removeAllSubItems = new NativeButton(
                "Remove all sub items",
                event -> navigableParent.removeAllItems());
        removeAllSubItems.setId("remove-all-sub-items");
        add(removeAllSubItems);

        NativeButton expandItem = new NativeButton("Expand item",
                event -> navigableParent.setExpanded(true));
        expandItem.setId("expand-item");
        add(expandItem);

        NativeButton collapseItem = new NativeButton("Collapse item",
                event -> navigableParent.setExpanded(false));
        collapseItem.setId("collapse-item");
        add(collapseItem);

        NativeButton setPath = new NativeButton("Set another path",
                event -> navigableParent.setPath("vaadin-side-nav/side-nav-test"));
        setPath.setId("set-path");
        add(setPath);

        NativeButton setLabel = new NativeButton("Change label",
                event -> navigableParent.setLabel("Changed label"));
        setLabel.setId("change-label");
        add(setLabel);
    }
}
