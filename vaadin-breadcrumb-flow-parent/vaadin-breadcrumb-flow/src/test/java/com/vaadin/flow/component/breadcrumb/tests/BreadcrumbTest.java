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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.breadcrumb.Breadcrumb;
import com.vaadin.flow.component.breadcrumb.BreadcrumbItem;

class BreadcrumbTest {

    private Breadcrumb breadcrumb;

    @BeforeEach
    void setUp() {
        breadcrumb = new Breadcrumb();
    }

    @Test
    void addItem_appendsBothItemsAsChildren() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Docs", "/docs");

        breadcrumb.addItem(item1, item2);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(item2, items.get(1));
    }

    @Test
    void getItems_returnsAddedItemsInOrder() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Products", "/products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details", "/details");

        breadcrumb.addItem(item1);
        breadcrumb.addItem(item2);
        breadcrumb.addItem(item3);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(3, items.size());
        assertEquals("Home", items.get(0).getLabel());
        assertEquals("Products", items.get(1).getLabel());
        assertEquals("Details", items.get(2).getLabel());
    }

    @Test
    void addItemAsFirst_insertsBeforeExistingItems() {
        BreadcrumbItem existing = new BreadcrumbItem("Docs", "/docs");
        breadcrumb.addItem(existing);

        BreadcrumbItem first = new BreadcrumbItem("Home", "/");
        breadcrumb.addItemAsFirst(first);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals(first, items.get(0));
        assertEquals(existing, items.get(1));
    }

    @Test
    void addItemAtIndex_insertsAtCorrectPosition() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Details", "/details");
        breadcrumb.addItem(item1, item2);

        BreadcrumbItem middle = new BreadcrumbItem("Products", "/products");
        breadcrumb.addItemAtIndex(1, middle);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(3, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(middle, items.get(1));
        assertEquals(item2, items.get(2));
    }

    @Test
    void remove_removesItemFromChildren() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Docs", "/docs");
        breadcrumb.addItem(item1, item2);

        breadcrumb.remove(item1);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals(item2, items.get(0));
    }

    @Test
    void removeAll_removesAllBreadcrumbItems() {
        breadcrumb.addItem(new BreadcrumbItem("Home", "/"),
                new BreadcrumbItem("Docs", "/docs"),
                new BreadcrumbItem("Page", "/page"));

        breadcrumb.removeAll();

        assertTrue(breadcrumb.getItems().isEmpty());
    }

    @Test
    void setItems_varargs_replacesExistingItems() {
        breadcrumb.addItem(new BreadcrumbItem("Old", "/old"));

        BreadcrumbItem item1 = new BreadcrumbItem("New1", "/new1");
        BreadcrumbItem item2 = new BreadcrumbItem("New2", "/new2");
        breadcrumb.setItems(item1, item2);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(item2, items.get(1));
    }

    @Test
    void setItems_list_acceptsList() {
        breadcrumb.addItem(new BreadcrumbItem("Old", "/old"));

        BreadcrumbItem item1 = new BreadcrumbItem("New1", "/new1");
        breadcrumb.setItems(List.of(item1));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals(item1, items.get(0));
    }
}
