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
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.breadcrumbs.testbench.BreadcrumbsElement;
import com.vaadin.flow.component.breadcrumbs.testbench.BreadcrumbsItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link DataDrivenBreadcrumbsPage}, covering requirement
 * 16: a trail built from loaded domain data with a data-derived ancestor that
 * has no backing {@code @Route}.
 */
@TestPath("vaadin-breadcrumbs/data-driven")
public class DataDrivenBreadcrumbsIT extends AbstractComponentIT {

    private BreadcrumbsElement breadcrumbs;

    @Before
    public void init() {
        open();
        breadcrumbs = $(BreadcrumbsElement.class).waitForFirst();
    }

    @Test
    public void pageOpened_trailMatchesLoadedData() {
        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals(4, items.size());
        Assert.assertEquals("Home", items.get(0).getText());
        Assert.assertEquals("Customers", items.get(1).getText());
        // Data-derived ancestor with no backing @Route.
        Assert.assertEquals("Enterprise", items.get(2).getText());
        // Data-derived current-page label, no path.
        Assert.assertEquals("Acme Corp", items.get(3).getText());

        Assert.assertEquals("/customers", items.get(1).getPath());
        Assert.assertEquals("/customers/enterprise", items.get(2).getPath());
        Assert.assertNull(items.get(3).getPath());
        Assert.assertTrue(items.get(3).isCurrent());
    }
}
