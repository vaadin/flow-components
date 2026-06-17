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

import java.io.Serializable;

import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs.Mode;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Test view building a {@link Mode#MANUAL} trail from loaded data (requirement
 * 16).
 */
@Route("vaadin-breadcrumbs/data-driven")
public class DataDrivenBreadcrumbsPage extends Div {

    private record Customer(String name,
            String segment) implements Serializable {
    }

    public DataDrivenBreadcrumbsPage() {
        // Simulate loading domain data for the current page.
        Customer customer = loadCustomer();

        Breadcrumbs breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        breadcrumbs.setId("breadcrumbs");

        BreadcrumbsItem home = new BreadcrumbsItem("Home",
                ManualBreadcrumbsTargetPage.class);
        BreadcrumbsItem customers = new BreadcrumbsItem("Customers",
                "/customers");
        // Data-derived ancestor with no backing route.
        BreadcrumbsItem segment = new BreadcrumbsItem(customer.segment(),
                "/customers/" + customer.segment().toLowerCase());
        // Data-derived current-page label, no path.
        BreadcrumbsItem current = new BreadcrumbsItem(customer.name());
        breadcrumbs.add(home, customers, segment, current);

        add(breadcrumbs);
    }

    private Customer loadCustomer() {
        return new Customer("Acme Corp", "Enterprise");
    }
}
