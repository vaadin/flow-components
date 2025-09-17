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
package com.vaadin.flow.component.breadcrumb;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link Breadcrumb}.
 */
public class BreadcrumbTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createBreadcrumbInDefaultState() {
        Breadcrumb breadcrumb = new Breadcrumb();

        Assert.assertEquals("Initial item count is invalid", 0,
                breadcrumb.getItemCount());
        Assert.assertEquals("Tag name is invalid", "vaadin-breadcrumb",
                breadcrumb.getElement().getTag());
    }

    @Test
    public void createBreadcrumbWithItems() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2, item3);

        Assert.assertEquals("Initial item count is invalid", 3,
                breadcrumb.getItemCount());
        Assert.assertEquals("First item is invalid", item1,
                breadcrumb.getItemAt(0));
        Assert.assertEquals("Second item is invalid", item2,
                breadcrumb.getItemAt(1));
        Assert.assertEquals("Third item is invalid", item3,
                breadcrumb.getItemAt(2));
    }

    @Test
    public void addItems() {
        Breadcrumb breadcrumb = new Breadcrumb();
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");

        breadcrumb.add(item1, item2);

        Assert.assertEquals("Item count after add is invalid", 2,
                breadcrumb.getItemCount());
        Assert.assertEquals("First item is invalid", item1,
                breadcrumb.getItemAt(0));
        Assert.assertEquals("Second item is invalid", item2,
                breadcrumb.getItemAt(1));
    }

    @Test
    public void addNullItems_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Items to add cannot be null");

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.add((BreadcrumbItem[]) null);
    }

    @Test
    public void addItemWithNull_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Individual item to add cannot be null");

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.add(new BreadcrumbItem("Home"), null);
    }

    @Test
    public void removeItems() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2, item3);

        breadcrumb.remove(item2);

        Assert.assertEquals("Item count after remove is invalid", 2,
                breadcrumb.getItemCount());
        Assert.assertEquals("First item is invalid", item1,
                breadcrumb.getItemAt(0));
        Assert.assertEquals("Second item is invalid", item3,
                breadcrumb.getItemAt(1));
    }

    @Test
    public void removeAll() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2);

        breadcrumb.removeAll();

        Assert.assertEquals("Item count after removeAll is invalid", 0,
                breadcrumb.getItemCount());
    }

    @Test
    public void getItemAt_invalidIndex_throwsException() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: 0, Size: 0");

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.getItemAt(0);
    }

    @Test
    public void getItemAt_negativeIndex_throwsException() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: -1, Size: 0");

        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.getItemAt(-1);
    }

    @Test
    public void indexOf_returnsCorrectIndex() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2, item3);

        Assert.assertEquals("Index of item1 is invalid", 0,
                breadcrumb.indexOf(item1));
        Assert.assertEquals("Index of item2 is invalid", 1,
                breadcrumb.indexOf(item2));
        Assert.assertEquals("Index of item3 is invalid", 2,
                breadcrumb.indexOf(item3));
    }

    @Test
    public void indexOf_itemNotFound_returnsNegativeOne() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem itemNotAdded = new BreadcrumbItem("Details");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2);

        Assert.assertEquals("Index of not added item should be -1", -1,
                breadcrumb.indexOf(itemNotAdded));
    }

    @Test
    public void indexOf_nullItem_returnsNegativeOne() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        Breadcrumb breadcrumb = new Breadcrumb(item1);

        Assert.assertEquals("Index of null should be -1", -1,
                breadcrumb.indexOf(null));
    }

    @Test
    public void replace_validIndex() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details");
        BreadcrumbItem newItem = new BreadcrumbItem("Categories");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2, item3);

        breadcrumb.replace(1, newItem);

        Assert.assertEquals("Item count after replace is invalid", 3,
                breadcrumb.getItemCount());
        Assert.assertEquals("First item is invalid", item1,
                breadcrumb.getItemAt(0));
        Assert.assertEquals("Replaced item is invalid", newItem,
                breadcrumb.getItemAt(1));
        Assert.assertEquals("Third item is invalid", item3,
                breadcrumb.getItemAt(2));
    }

    @Test
    public void replace_invalidIndex_throwsException() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: 3, Size: 2");

        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem newItem = new BreadcrumbItem("Categories");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2);

        breadcrumb.replace(3, newItem);
    }

    @Test
    public void replace_nullItem_throwsException() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("New item cannot be null");

        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        Breadcrumb breadcrumb = new Breadcrumb(item1);

        breadcrumb.replace(0, null);
    }

    @Test
    public void getItems_returnsAllItems() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home");
        BreadcrumbItem item2 = new BreadcrumbItem("Products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details");
        Breadcrumb breadcrumb = new Breadcrumb(item1, item2, item3);

        BreadcrumbItem[] items = breadcrumb.getItems()
                .toArray(BreadcrumbItem[]::new);

        Assert.assertEquals("Item count is invalid", 3, items.length);
        Assert.assertEquals("First item is invalid", item1, items[0]);
        Assert.assertEquals("Second item is invalid", item2, items[1]);
        Assert.assertEquals("Third item is invalid", item3, items[2]);
    }

    @Test
    public void themeVariants() {
        Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addThemeVariants(BreadcrumbVariant.LUMO_SMALL);
        Assert.assertTrue("Should have small variant",
                breadcrumb.getThemeNames().contains("small"));

        breadcrumb.addThemeVariants(BreadcrumbVariant.LUMO_LARGE);
        Assert.assertTrue("Should have large variant",
                breadcrumb.getThemeNames().contains("large"));

        breadcrumb.removeThemeVariants(BreadcrumbVariant.LUMO_SMALL);
        Assert.assertFalse("Should not have small variant",
                breadcrumb.getThemeNames().contains("small"));
    }
}
