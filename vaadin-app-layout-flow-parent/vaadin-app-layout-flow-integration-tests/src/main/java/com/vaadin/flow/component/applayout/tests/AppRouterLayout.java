/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.applayout.tests;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

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
