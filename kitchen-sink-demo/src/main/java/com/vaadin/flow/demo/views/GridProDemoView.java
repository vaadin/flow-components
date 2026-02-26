/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.demo.views;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for GridPro component.
 */
@Route(value = "grid-pro", layout = MainLayout.class)
@PageTitle("Grid Pro | Vaadin Kitchen Sink")
public class GridProDemoView extends VerticalLayout {

    public GridProDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Grid Pro Component"));
        add(new Paragraph("Grid Pro provides inline editing capabilities with cell editors."));

        // Basic editable grid
        GridPro<Product> basic = new GridPro<>();
        basic.addEditColumn(Product::getName)
            .text(Product::setName)
            .setHeader("Product Name");
        basic.addEditColumn(Product::getCategory)
            .select(Product::setCategory, "Electronics", "Clothing", "Food", "Books", "Other")
            .setHeader("Category");
        basic.addEditColumn(Product::getPrice)
            .text((item, value) -> item.setPrice(Double.parseDouble(value)))
            .setHeader("Price");
        basic.addEditColumn(Product::getStock)
            .text((item, value) -> item.setStock(Integer.parseInt(value)))
            .setHeader("Stock");
        basic.addEditColumn(Product::isActive)
            .checkbox(Product::setActive)
            .setHeader("Active");
        basic.setItems(getSampleProducts());
        basic.setHeight("350px");
        addSection("Inline Editable Grid", basic);

        // With edit listener
        GridPro<Product> withListener = new GridPro<>();
        withListener.addEditColumn(Product::getName)
            .text(Product::setName)
            .setHeader("Product Name");
        withListener.addEditColumn(Product::getPrice)
            .text((item, value) -> item.setPrice(Double.parseDouble(value)))
            .setHeader("Price");
        withListener.addEditColumn(Product::getStock)
            .text((item, value) -> item.setStock(Integer.parseInt(value)))
            .setHeader("Stock");
        withListener.addItemPropertyChangedListener(event -> {
            Notification.show("Changed " + event.getItem().getName());
        });
        withListener.setItems(getSampleProducts());
        withListener.setHeight("350px");
        addSection("With Change Listener", withListener);

        // Mixed editable and non-editable columns
        GridPro<Product> mixed = new GridPro<>();
        mixed.addColumn(Product::getId).setHeader("ID");
        mixed.addEditColumn(Product::getName)
            .text(Product::setName)
            .setHeader("Product Name");
        mixed.addColumn(Product::getCategory).setHeader("Category (Read-only)");
        mixed.addEditColumn(Product::getPrice)
            .text((item, value) -> item.setPrice(Double.parseDouble(value)))
            .setHeader("Price");
        mixed.addColumn(product -> product.isActive() ? "Yes" : "No")
            .setHeader("Active (Read-only)");
        mixed.setItems(getSampleProducts());
        mixed.setHeight("350px");
        addSection("Mixed Editable/Read-only Columns", mixed);

        // Enter next row on Enter key
        GridPro<Product> enterNext = new GridPro<>();
        enterNext.setEnterNextRow(true);
        enterNext.addEditColumn(Product::getName)
            .text(Product::setName)
            .setHeader("Product Name");
        enterNext.addEditColumn(Product::getPrice)
            .text((item, value) -> item.setPrice(Double.parseDouble(value)))
            .setHeader("Price");
        enterNext.addEditColumn(Product::getStock)
            .text((item, value) -> item.setStock(Integer.parseInt(value)))
            .setHeader("Stock");
        enterNext.setItems(getSampleProducts());
        enterNext.setHeight("350px");
        addSection("Enter Next Row on Enter Key", enterNext);

        // Single click edit
        GridPro<Product> singleClick = new GridPro<>();
        singleClick.setSingleCellEdit(true);
        singleClick.addEditColumn(Product::getName)
            .text(Product::setName)
            .setHeader("Product Name");
        singleClick.addEditColumn(Product::getCategory)
            .select(Product::setCategory, "Electronics", "Clothing", "Food", "Books", "Other")
            .setHeader("Category");
        singleClick.addEditColumn(Product::getPrice)
            .text((item, value) -> item.setPrice(Double.parseDouble(value)))
            .setHeader("Price");
        singleClick.setItems(getSampleProducts());
        singleClick.setHeight("350px");
        addSection("Single Click Edit Mode", singleClick);
    }

    private List<Product> getSampleProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1, "Laptop", "Electronics", 999.99, 50, true));
        products.add(new Product(2, "T-Shirt", "Clothing", 29.99, 200, true));
        products.add(new Product(3, "Coffee Beans", "Food", 14.99, 150, true));
        products.add(new Product(4, "Java Programming Book", "Books", 49.99, 75, true));
        products.add(new Product(5, "Headphones", "Electronics", 149.99, 100, true));
        products.add(new Product(6, "Jeans", "Clothing", 79.99, 120, false));
        products.add(new Product(7, "Chocolate", "Food", 4.99, 500, true));
        products.add(new Product(8, "Mouse", "Electronics", 29.99, 200, true));
        return products;
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }

    public static class Product {
        private int id;
        private String name;
        private String category;
        private double price;
        private int stock;
        private boolean active;

        public Product(int id, String name, String category, double price, int stock, boolean active) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.active = active;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
