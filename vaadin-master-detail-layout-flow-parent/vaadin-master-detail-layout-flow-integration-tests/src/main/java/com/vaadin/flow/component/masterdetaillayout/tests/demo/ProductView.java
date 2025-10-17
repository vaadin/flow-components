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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

@Route(value = "vaadin-master-detail-layout/demo/category/:categoryName/product/:productId", layout = CategoryView.class)
public class ProductView extends VerticalLayout implements BeforeEnterObserver {
    private final H2 title;
    private final Binder<DemoData.Product> binder;

    private String categoryName;

    public ProductView() {
        addClassName("product-view");
        setHeightFull();

        title = new H2("Product");
        add(title);

        var formLayout = new FormLayout();
        var name = new TextField("Name");
        var category = new TextField("Category");
        var price = new NumberField("Price");
        var dateAdded = new DatePicker("Date added");
        formLayout.add(name, category, price, dateAdded);
        add(formLayout);

        binder = new Binder<>(DemoData.Product.class);
        binder.bind(name, "name");
        binder.bind(category, "category");
        binder.bind(price, "price");
        binder.bind(dateAdded, "dateAdded");

        var save = new Button("Save", e -> close());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var cancel = new Button("Cancel", e -> close());
        var footer = new HorizontalLayout(save, cancel);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(footer);
    }

    private void close() {
        UI.getCurrent().navigate(CategoryView.class, new RouteParameters(
                new RouteParam("categoryName", this.categoryName)));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        var categoryName = beforeEnterEvent.getRouteParameters()
                .get("categoryName").orElse(null);
        var product = beforeEnterEvent.getRouteParameters().getLong("productId")
                .map(DemoData::getById).orElse(null);

        if (product == null || categoryName == null) {
            beforeEnterEvent.rerouteToError(NotFoundException.class);
            return;
        }

        this.categoryName = categoryName;
        title.setText("Product: " + product.name());
        binder.readRecord(product);
    }
}
