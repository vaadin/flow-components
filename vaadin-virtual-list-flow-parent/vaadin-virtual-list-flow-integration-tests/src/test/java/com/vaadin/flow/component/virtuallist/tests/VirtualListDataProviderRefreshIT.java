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
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-virtual-list/data-provider-refresh")
public class VirtualListDataProviderRefreshIT extends AbstractComponentIT {
    private VirtualListElement virtualList;
    private TestBenchElement updateItemFive;
    private TestBenchElement updateAllItems;

    @Before
    public void init() {
        open();
        virtualList = $(VirtualListElement.class).waitForFirst();
        updateItemFive = $("button").id("update-item-five");
        updateAllItems = $("button").id("update-all-items");
    }

    @Test
    public void refreshItem_itemUpdated() {
        // Wait for initial renderer
        virtualList.$("span").waitForFirst();
        // Check initial render
        assertItemText(4, "Item5");

        updateItemFive.click();

        // Check updated render
        assertItemText(4, "Item5 updated");
    }

    @Test
    public void refreshAll_allItemsUpdated() {
        // Wait for initial renderer
        virtualList.$("span").waitForFirst();
        // Check initial render
        assertItemText(0, "Item1");
        assertItemText(7, "Item8");
        assertItemText(12, "Item13");

        updateAllItems.click();

        // Check updated render
        assertItemText(0, "Item1 updated");
        assertItemText(7, "Item8 updated");
        assertItemText(12, "Item13 updated");
    }

    private void assertItemText(int itemIndex, String expectedText) {
        TestBenchElement item = virtualList.$("span").get(itemIndex);
        Assert.assertEquals(expectedText, item.getText());
    }
}
