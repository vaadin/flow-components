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
package com.vaadin.flow.component.masterdetaillayout.tests.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

@Route("vaadin-master-detail-layout/demo")
@CssImport("./styles.css")
public class MainView extends MasterDetailLayout {
    public MainView() {
        addClassName("main-view");

        setMaster(createMasterContent());
        setMasterSize("200px");
    }

    private Component createMasterContent() {
        var title = new Span("Demo");
        title.addClassName("title");

        var nav = new SideNav();
        nav.setWidthFull();
        var fruits = new SideNavItem("Fruits", CategoryView.class,
                new RouteParameters(new RouteParam("categoryName", "Fruit")));
        fruits.setMatchNested(true);
        var vegetables = new SideNavItem("Vegetables", CategoryView.class,
                new RouteParameters(
                        new RouteParam("categoryName", "Vegetable")));
        vegetables.setMatchNested(true);
        nav.addItem(fruits, vegetables);

        var masterLayout = new VerticalLayout();
        masterLayout.addClassName("nav-pane");
        masterLayout.setHeightFull();
        masterLayout.add(title, nav);

        return masterLayout;
    }
}
