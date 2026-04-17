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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.breadcrumb.testbench.BreadcrumbElement;
import com.vaadin.flow.component.breadcrumb.testbench.BreadcrumbItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link BreadcrumbPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("breadcrumb-test")
public class BreadcrumbIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void staticBreadcrumb_rendersExpectedNumberOfItems() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("static-breadcrumb");
        List<BreadcrumbItemElement> items = breadcrumb.getItems();
        Assert.assertEquals("Static breadcrumb should have 4 items", 4,
                items.size());
    }

    @Test
    public void staticBreadcrumb_lastItemIsCurrent() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("static-breadcrumb");
        List<BreadcrumbItemElement> items = breadcrumb.getItems();

        Assert.assertFalse("First item should not be current",
                items.get(0).isCurrent());
        Assert.assertTrue("Last item should be current",
                items.get(3).isCurrent());

        BreadcrumbItemElement currentItem = breadcrumb.getCurrentItem();
        Assert.assertNotNull("Current item should exist", currentItem);
        Assert.assertEquals("Current item text should be 'Details'", "Details",
                currentItem.getText());
    }

    @Test
    public void clickAncestorItem_urlChanges() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("static-breadcrumb");
        List<BreadcrumbItemElement> items = breadcrumb.getItems();

        // Click the "Products" item which has path "products"
        items.get(1).clickLink();
        waitUntil(ExpectedConditions.urlContains("products"));
    }

    @Test
    public void nonClickableItem_hasNoPath() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("non-clickable-breadcrumb");
        List<BreadcrumbItemElement> items = breadcrumb.getItems();

        Assert.assertEquals("Non-clickable breadcrumb should have 3 items", 3,
                items.size());
        Assert.assertNotNull("First item should have a path",
                items.get(0).getPath());
        Assert.assertNull("Middle item should have no path (non-clickable)",
                items.get(1).getPath());
    }

    @Test
    public void customSeparator_rendersInSeparatorSlot() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("separator-breadcrumb");

        // Verify the separator is rendered in the slot
        String separatorText = breadcrumb.$("span")
                .attributeContains("slot", "separator").first().getText();
        Assert.assertEquals("Custom separator should render '>'", ">",
                separatorText);
    }

    @Test
    public void iconOnlyRootItem_rendersPrefixComponent() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("icon-breadcrumb");
        List<BreadcrumbItemElement> items = breadcrumb.getItems();

        Assert.assertEquals("Icon breadcrumb should have 3 items", 3,
                items.size());

        // The first item has an icon prefix but no label text
        BreadcrumbItemElement rootItem = items.get(0);
        Assert.assertNotNull("Root item should have a path",
                rootItem.getPath());

        // Verify the prefix span is present in the first item
        boolean hasPrefixSpan = rootItem.$("span")
                .attributeContains("slot", "prefix").exists();
        Assert.assertTrue("Icon-only root item should have a prefix component",
                hasPrefixSpan);
    }

    @Test
    public void dynamicBreadcrumb_setItemsRendersCorrectly() {
        BreadcrumbElement breadcrumb = $(BreadcrumbElement.class)
                .id("dynamic-breadcrumb");
        List<BreadcrumbItemElement> items = breadcrumb.getItems();

        Assert.assertEquals("Dynamic breadcrumb should have 3 items", 3,
                items.size());
        Assert.assertEquals("First item should be 'Dashboard'", "Dashboard",
                items.get(0).getText());
        Assert.assertEquals("Second item should be 'Settings'", "Settings",
                items.get(1).getText());
        Assert.assertTrue("Last item should be current",
                items.get(2).isCurrent());
    }
}
