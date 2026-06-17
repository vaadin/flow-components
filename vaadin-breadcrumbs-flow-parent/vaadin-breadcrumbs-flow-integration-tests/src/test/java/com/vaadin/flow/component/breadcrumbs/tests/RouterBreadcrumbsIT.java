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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.breadcrumbs.testbench.BreadcrumbsElement;
import com.vaadin.flow.component.breadcrumbs.testbench.BreadcrumbsItemElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link RouterBreadcrumbsPage}.
 */
public class RouterBreadcrumbsIT extends AbstractComponentIT {

    private BreadcrumbsElement open(String route, int expectedItemCount) {
        getDriver().get(getRootURL() + "/" + route);
        BreadcrumbsElement breadcrumbs = $(BreadcrumbsElement.class)
                .waitForFirst();
        // The router trail is built asynchronously after navigation, so wait
        // for it to settle before asserting.
        waitUntil(d -> breadcrumbs.getItems().size() == expectedItemCount);
        return breadcrumbs;
    }

    @Test
    public void rootView_oneItemTrailWithPageTitle() {
        BreadcrumbsElement breadcrumbs = open("vaadin-breadcrumbs/router-home",
                1);

        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals("Home", items.get(0).getText());
    }

    @Test
    public void leafView_trailMatchesRouteHierarchy() {
        BreadcrumbsElement breadcrumbs = open("vaadin-breadcrumbs/router-acme",
                3);

        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("Home", items.get(0).getText());
        Assert.assertEquals("Customers", items.get(1).getText());
        Assert.assertEquals("Acme", items.get(2).getText());
    }

    @Test
    public void dynamicTitleView_lastItemUsesDynamicTitle() {
        BreadcrumbsElement breadcrumbs = open(
                "vaadin-breadcrumbs/router-dynamic", 3);

        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals(3, items.size());
        // The current item uses the view's HasDynamicTitle value.
        Assert.assertEquals("Acme Dynamic", items.get(2).getText());
    }

    @Test
    public void ancestorPaths_resolveToLoadableUrls() {
        BreadcrumbsElement breadcrumbs = open("vaadin-breadcrumbs/router-acme",
                3);

        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        // Each ancestor path resolves to a URL that loads successfully.
        String homePath = items.get(0).getPath();
        String customersPath = items.get(1).getPath();
        Assert.assertEquals("vaadin-breadcrumbs/router-home", homePath);
        Assert.assertEquals("vaadin-breadcrumbs/router-customers",
                customersPath);

        BreadcrumbsElement homeBreadcrumbs = open(homePath, 1);
        Assert.assertEquals("Home",
                homeBreadcrumbs.getItems().get(0).getText());

        BreadcrumbsElement customersBreadcrumbs = open(customersPath, 2);
        Assert.assertEquals("Customers",
                customersBreadcrumbs.getItems().get(1).getText());
    }

    @Test
    public void leafView_lastItemIsCurrentWithoutPath() {
        BreadcrumbsElement breadcrumbs = open("vaadin-breadcrumbs/router-acme",
                3);

        BreadcrumbsItemElement current = breadcrumbs.getItems().get(2);
        Assert.assertTrue(current.isCurrent());
        Assert.assertNull(current.getPath());
        Assert.assertEquals(current, breadcrumbs.getCurrentItem());
    }
}
