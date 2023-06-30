/*
 * Copyright 2023 Vaadin Ltd.
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
