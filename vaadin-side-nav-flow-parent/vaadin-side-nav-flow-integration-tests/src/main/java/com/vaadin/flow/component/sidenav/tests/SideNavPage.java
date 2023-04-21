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

@Route("vaadin-side-nav/side-nav-test")
public class SideNavPage extends Div {

    public SideNavPage() {
        setMaxWidth("400px");

        SideNav sideNav = new SideNav();
        sideNav.setLabel("Navigation Test");
        sideNav.setCollapsible(true);

        SideNavItem labelOnly = new SideNavItem("Label only");
        labelOnly.setId("label-only");
        sideNav.addItem(labelOnly);

        SideNavItem classTarget = new SideNavItem("Target using class",
                SideNavTargetView.class);
        classTarget.setId("class-target");
        sideNav.addItem(classTarget);

        Avatar vaadinAvatar = new Avatar("Vaadin");
        SideNavItem classTargetWithComponent = new SideNavItem(
                "Target using class with component", SideNavTargetView.class,
                vaadinAvatar);
        classTargetWithComponent.setId("class-target-component");
        sideNav.addItem(classTargetWithComponent);

        SideNavItem pathTarget = new SideNavItem("Target using path",
                "vaadin-side-nav/side-nav-test-target-view");
        pathTarget.setId("path-target");
        sideNav.addItem(pathTarget);

        SideNavItem pathTargetWithIcon = new SideNavItem(
                "Target using path with icon",
                "vaadin-side-nav/side-nav-test-target-view",
                VaadinIcon.GLOBE.create());
        pathTargetWithIcon.setId("path-target-icon");
        sideNav.addItem(pathTargetWithIcon);

        add(sideNav);

        NativeButton addItem = new NativeButton("Add item",
                event -> sideNav.addItem(new SideNavItem("Added item",
                        "vaadin-side-nav/side-nav-test-target-view")));
        addItem.setId("add-item");
        add(addItem);

        NativeButton removeItem = new NativeButton("Remove item",
                event -> sideNav.removeItem(classTarget));
        removeItem.setId("remove-item");
        add(removeItem);

        NativeButton removeAllItems = new NativeButton("Remove all items",
                event -> sideNav.removeAllItems());
        removeAllItems.setId("remove-all-items");
        add(removeAllItems);

        NativeButton changeLabel = new NativeButton(
                "Change vaadin-side-nav label",
                event -> sideNav.setLabel("Label changed"));
        changeLabel.setId("change-label");
        add(changeLabel);

        NativeButton makeNotCollapsible = new NativeButton(
                "Make not collapsible", event -> sideNav.setCollapsible(false));
        makeNotCollapsible.setId("make-not-collapsible");
        add(makeNotCollapsible);
    }
}
