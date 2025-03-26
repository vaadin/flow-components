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

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

@ParentLayout(MainView.class)
@Route(value = "vaadin-master-detail-layout/demo/category/:categoryName", layout = MainView.class)
public class CategoryView extends MasterDetailLayout
        implements BeforeEnterObserver {
    private H1 title;
    private Grid<DemoData.Product> grid;
    private String categoryName;

    public CategoryView() {
        addClassName("category-view");

        setMaster(createMasterContent());
        setMasterMinSize("500px");
        setDetailMinSize("300px");

        getElement().setProperty("stackThreshold", "800px");
    }

    private Component createMasterContent() {
        title = new H1("Category");

        grid = new Grid<>();
        grid.addColumn(DemoData.Product::id).setHeader("ID");
        grid.addColumn(DemoData.Product::name).setHeader("Name");
        grid.addColumn(DemoData.Product::price).setHeader("Price");
        grid.addColumn(DemoData.Product::dateAdded).setHeader("Date Added");
        grid.addSelectionListener(event -> {
            var item = event.getFirstSelectedItem().orElse(null);
            if (item != null) {
                UI.getCurrent().navigate(ProductView.class,
                        new RouteParameters(
                                new RouteParam("categoryName", categoryName),
                                new RouteParam("productId", item.id())));
            } else {
                UI.getCurrent().navigate(CategoryView.class,
                        new RouteParameters(
                                new RouteParam("categoryName", categoryName)));
            }
        });

        var masterLayout = new VerticalLayout(title, grid);
        masterLayout.setFlexGrow(1, grid);
        masterLayout.setHeightFull();

        return masterLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        var categoryName = beforeEnterEvent.getRouteParameters()
                .get("categoryName").orElse(null);
        var productId = beforeEnterEvent.getRouteParameters()
                .getLong("productId").orElse(null);

        if (categoryName == null) {
            beforeEnterEvent.rerouteToError(NotFoundException.class);
            return;
        }

        if (!Objects.equals(categoryName, this.categoryName)) {
            this.categoryName = categoryName;
            title.setText("Category: " + categoryName + "s");
            grid.setItems(DemoData.getByCategory(categoryName));
        }

        if (productId != null) {
            var product = DemoData.getById(productId);
            grid.select(product);
        } else {
            grid.deselectAll();
        }
    }
}
