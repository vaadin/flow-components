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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParent;

/**
 * Test views for {@link Breadcrumbs} in {@link Mode#ROUTER} mode, forming a
 * {@code @RouteParent} chain Home -&gt; Customers -&gt; Acme.
 */
@Route("vaadin-breadcrumbs/router-home")
@PageTitle("Home")
public class RouterBreadcrumbsPage extends Div {

    public RouterBreadcrumbsPage() {
        Breadcrumbs breadcrumbs = new Breadcrumbs();
        breadcrumbs.setId("breadcrumbs");
        add(breadcrumbs);
    }

    @Route("vaadin-breadcrumbs/router-customers")
    @RouteParent(RouterBreadcrumbsPage.class)
    @PageTitle("Customers")
    public static class RouterCustomersPage extends Div {

        public RouterCustomersPage() {
            Breadcrumbs breadcrumbs = new Breadcrumbs();
            breadcrumbs.setId("breadcrumbs");
            add(breadcrumbs);
        }
    }

    @Route("vaadin-breadcrumbs/router-acme")
    @RouteParent(RouterCustomersPage.class)
    @PageTitle("Acme")
    public static class RouterAcmePage extends Div {

        public RouterAcmePage() {
            Breadcrumbs breadcrumbs = new Breadcrumbs();
            breadcrumbs.setId("breadcrumbs");
            add(breadcrumbs);
        }
    }
}
