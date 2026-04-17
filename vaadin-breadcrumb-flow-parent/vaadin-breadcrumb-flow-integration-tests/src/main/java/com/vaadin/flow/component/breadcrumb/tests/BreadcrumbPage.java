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
package com.vaadin.flow.component.breadcrumb.tests;

import com.vaadin.flow.component.breadcrumb.Breadcrumb;
import com.vaadin.flow.component.breadcrumb.Breadcrumb.BreadcrumbI18n;
import com.vaadin.flow.component.breadcrumb.BreadcrumbItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Test view for the {@link Breadcrumb} component.
 */
@Route("breadcrumb-test")
public class BreadcrumbPage extends Div {

    public BreadcrumbPage() {
        // Static breadcrumb trail
        Breadcrumb staticBreadcrumb = new Breadcrumb();
        staticBreadcrumb.setId("static-breadcrumb");
        staticBreadcrumb.addItem(new BreadcrumbItem("Home", "/"),
                new BreadcrumbItem("Products", "products"),
                new BreadcrumbItem("Laptops", "products/laptops"),
                new BreadcrumbItem("Details").asCurrent());
        add(staticBreadcrumb);

        // Dynamic trail built via setItems
        Breadcrumb dynamicBreadcrumb = new Breadcrumb();
        dynamicBreadcrumb.setId("dynamic-breadcrumb");
        dynamicBreadcrumb.setItems(new BreadcrumbItem("Dashboard", "dashboard"),
                new BreadcrumbItem("Settings", "settings"),
                new BreadcrumbItem("Profile").asCurrent());
        add(dynamicBreadcrumb);

        // Icon-only root item using a Span as the prefix component
        Breadcrumb iconBreadcrumb = new Breadcrumb();
        iconBreadcrumb.setId("icon-breadcrumb");
        Span homeIcon = new Span("\u2302"); // Unicode house character
        homeIcon.getElement().setAttribute("class", "home-icon");
        BreadcrumbItem iconOnlyRoot = new BreadcrumbItem(homeIcon, "/");
        iconBreadcrumb.addItem(iconOnlyRoot,
                new BreadcrumbItem("Category", "category"),
                new BreadcrumbItem("Page").asCurrent());
        add(iconBreadcrumb);

        // Non-clickable intermediate item (no path set)
        Breadcrumb nonClickableBreadcrumb = new Breadcrumb();
        nonClickableBreadcrumb.setId("non-clickable-breadcrumb");
        nonClickableBreadcrumb.addItem(new BreadcrumbItem("Home", "/"),
                new BreadcrumbItem("Non-clickable Section"),
                new BreadcrumbItem("Current Page").asCurrent());
        add(nonClickableBreadcrumb);

        // Custom separator
        Breadcrumb separatorBreadcrumb = new Breadcrumb();
        separatorBreadcrumb.setId("separator-breadcrumb");
        Span separator = new Span(">");
        separatorBreadcrumb.setSeparator(separator);
        separatorBreadcrumb.addItem(new BreadcrumbItem("Home", "/"),
                new BreadcrumbItem("Section", "section"),
                new BreadcrumbItem("Page").asCurrent());
        add(separatorBreadcrumb);

        // I18n configuration
        Breadcrumb i18nBreadcrumb = new Breadcrumb();
        i18nBreadcrumb.setId("i18n-breadcrumb");
        i18nBreadcrumb.setI18n(
                new BreadcrumbI18n().setNavigationLabel("Breadnavigation")
                        .setOverflow("More pages"));
        i18nBreadcrumb.addItem(new BreadcrumbItem("Startseite", "/"),
                new BreadcrumbItem("Kategorie", "category"),
                new BreadcrumbItem("Seite").asCurrent());
        add(i18nBreadcrumb);
    }
}
