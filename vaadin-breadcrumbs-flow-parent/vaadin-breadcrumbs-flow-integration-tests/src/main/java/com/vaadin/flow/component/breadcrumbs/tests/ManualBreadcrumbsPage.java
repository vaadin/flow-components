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
package com.vaadin.flow.component.breadcrumbs.tests;

import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs.Mode;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link Breadcrumbs} in {@link Mode#MANUAL} mode.
 */
@Route("vaadin-breadcrumbs/manual")
public class ManualBreadcrumbsPage extends Div {

    public ManualBreadcrumbsPage() {
        Breadcrumbs breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        breadcrumbs.setId("breadcrumbs");

        BreadcrumbsItem home = new BreadcrumbsItem("Home",
                ManualBreadcrumbsTargetPage.class);
        BreadcrumbsItem docs = new BreadcrumbsItem("Docs", "/docs");
        BreadcrumbsItem current = new BreadcrumbsItem("Current");
        breadcrumbs.add(home, docs, current);

        add(breadcrumbs);

        NativeButton addItem = new NativeButton("Add item", event -> {
            BreadcrumbsItem settings = new BreadcrumbsItem("Settings",
                    "/settings");
            breadcrumbs.addComponentAtIndex(breadcrumbs.getComponentCount() - 1,
                    settings);
        });
        addItem.setId("add-item");
        add(addItem);

        NativeButton removeItem = new NativeButton("Remove item",
                event -> breadcrumbs.remove(docs));
        removeItem.setId("remove-item");
        add(removeItem);
    }
}
