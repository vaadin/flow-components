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
import org.openqa.selenium.By;

import com.vaadin.flow.component.breadcrumbs.testbench.BreadcrumbsElement;
import com.vaadin.flow.component.breadcrumbs.testbench.BreadcrumbsItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link ManualBreadcrumbsPage}.
 */
@TestPath("vaadin-breadcrumbs/manual")
public class ManualBreadcrumbsIT extends AbstractComponentIT {

    private BreadcrumbsElement breadcrumbs;

    @Before
    public void init() {
        open();
        breadcrumbs = $(BreadcrumbsElement.class).waitForFirst();
    }

    @Test
    public void initialTrailRendered() {
        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals(3, items.size());

        Assert.assertEquals("Home", items.get(0).getText());
        Assert.assertEquals("vaadin-breadcrumbs/manual-target",
                items.get(0).getPath());
        Assert.assertFalse(items.get(0).isCurrent());

        Assert.assertEquals("Docs", items.get(1).getText());
        Assert.assertEquals("/docs", items.get(1).getPath());
        Assert.assertFalse(items.get(1).isCurrent());

        Assert.assertEquals("Current", items.get(2).getText());
        Assert.assertNull(items.get(2).getPath());
        Assert.assertTrue(items.get(2).isCurrent());
    }

    @Test
    public void clickAddItem_itemAddedToTrail() {
        findElement(By.id("add-item")).click();
        waitUntil(driver -> breadcrumbs.getItems().size() == 4);

        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals("Home", items.get(0).getText());
        Assert.assertEquals("Docs", items.get(1).getText());
        Assert.assertEquals("Settings", items.get(2).getText());
        Assert.assertEquals("/settings", items.get(2).getPath());
        Assert.assertEquals("Current", items.get(3).getText());
    }

    @Test
    public void clickRemoveItem_itemRemovedFromTrail() {
        findElement(By.id("remove-item")).click();
        waitUntil(driver -> breadcrumbs.getItems().size() == 2);

        List<BreadcrumbsItemElement> items = breadcrumbs.getItems();
        Assert.assertEquals("Home", items.get(0).getText());
        Assert.assertEquals("Current", items.get(1).getText());
    }

    @Test
    public void navigateItem_navigatesToItemPath() {
        breadcrumbs.getItems().get(0).navigate();
        waitUntil(driver -> driver.getCurrentUrl()
                .endsWith("vaadin-breadcrumbs/manual-target"));
    }
}
