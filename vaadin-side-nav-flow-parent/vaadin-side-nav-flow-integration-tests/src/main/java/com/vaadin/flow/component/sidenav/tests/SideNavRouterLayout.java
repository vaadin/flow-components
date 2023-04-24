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

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

@Route("vaadin-side-nav/side-nav-router-layout-test")
public class SideNavRouterLayout extends HorizontalLayout
        implements RouterLayout {

    public SideNavRouterLayout() {
        SideNav sideNav = new SideNav();
        sideNav.setLabel("Navigation");
        sideNav.setMaxWidth("400px");
        add(sideNav);

        SideNavItem target1 = new SideNavItem("Target 1",
                SideNavRouterLayoutTarget1Page.class);
        target1.setId("target-1");
        sideNav.addItem(target1);

        SideNavItem target2 = new SideNavItem("Target 2",
                SideNavRouterLayoutTarget2Page.class);
        target2.setId("target-2");
        sideNav.addItem(target2);
    }
}
