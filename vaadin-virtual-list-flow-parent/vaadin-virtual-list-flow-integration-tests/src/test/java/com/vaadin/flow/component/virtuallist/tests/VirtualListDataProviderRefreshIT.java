/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
        virtualList.$("flow-component-renderer").waitForFirst();
        // Check initial render
        assertItemText(4, "Item5");

        updateItemFive.click();

        // Check updated render
        assertItemText(4, "Item5 updated");
    }

    @Test
    public void refreshAll_allItemsUpdated() {
        // Wait for initial renderer
        virtualList.$("flow-component-renderer").waitForFirst();
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
        TestBenchElement item = virtualList.$("flow-component-renderer")
                .get(itemIndex);
        Assert.assertEquals(expectedText, item.getText());
    }
}
