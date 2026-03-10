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
package com.vaadin.flow.component.virtuallist.tests;

import static com.vaadin.flow.component.virtuallist.tests.VirtualListBindItemsPage.ADD_ITEM_BUTTON;
import static com.vaadin.flow.component.virtuallist.tests.VirtualListBindItemsPage.ITEM_COUNT_SPAN;
import static com.vaadin.flow.component.virtuallist.tests.VirtualListBindItemsPage.REMOVE_ITEM_BUTTON;
import static com.vaadin.flow.component.virtuallist.tests.VirtualListBindItemsPage.UPDATE_ITEM_BUTTON;
import static com.vaadin.flow.component.virtuallist.tests.VirtualListBindItemsPage.VIRTUAL_LIST_ID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nimbusds.jose.shaded.jcip.NotThreadSafe;
import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@NotThreadSafe
@TestPath("vaadin-virtual-list/virtual-list-bind-items")
public class VirtualListBindItemsIT extends AbstractComponentIT {

    private VirtualListElement virtualList;

    @Before
    public void init() {
        open();
        virtualList = $(VirtualListElement.class).id(VIRTUAL_LIST_ID);
    }

    @Test
    public void bindItems_initialItemsDisplayed() {
        Assert.assertEquals("Initial item count should be 3", "3",
                $("span").id(ITEM_COUNT_SPAN).getText());

        waitUntil(driver -> virtualList.getRowCount() > 0);
        Assert.assertEquals("Virtual list should have 3 items", 3,
                virtualList.getRowCount());

        waitUntil(driver -> "Item 1".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 0)));
        waitUntil(driver -> "Item 2".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 1)));
        waitUntil(driver -> "Item 3".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 2)));
    }

    @Test
    public void bindItems_addItem_virtualListUpdated() {
        waitUntil(driver -> virtualList.getRowCount() > 0);
        Assert.assertEquals("Initial virtual list item count", 3,
                virtualList.getRowCount());

        clickElementWithJs(ADD_ITEM_BUTTON);

        Assert.assertEquals("Item count should be 4", "4",
                $("span").id(ITEM_COUNT_SPAN).getText());
        waitUntil(driver -> virtualList.getRowCount() == 4);
        Assert.assertEquals("Virtual list should have 4 items", 4,
                virtualList.getRowCount());

        waitUntil(driver -> "Item 4".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 3)));
    }

    @Test
    public void bindItems_removeItem_virtualListUpdated() {
        waitUntil(driver -> virtualList.getRowCount() > 0);
        Assert.assertEquals("Initial virtual list item count", 3,
                virtualList.getRowCount());

        clickElementWithJs(REMOVE_ITEM_BUTTON);

        Assert.assertEquals("Item count should be 2", "2",
                $("span").id(ITEM_COUNT_SPAN).getText());
        waitUntil(driver -> virtualList.getRowCount() == 2);
        Assert.assertEquals("Virtual list should have 2 items", 2,
                virtualList.getRowCount());

        waitUntil(driver -> "Item 1".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 0)));
        waitUntil(driver -> "Item 2".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 1)));
    }

    @Test
    public void bindItems_multipleAdds_virtualListUpdatesCorrectly() {
        waitUntil(driver -> virtualList.getRowCount() > 0);

        clickElementWithJs(ADD_ITEM_BUTTON);
        clickElementWithJs(ADD_ITEM_BUTTON);

        Assert.assertEquals("Item count should be 5", "5",
                $("span").id(ITEM_COUNT_SPAN).getText());
        waitUntil(driver -> virtualList.getRowCount() == 5);
        Assert.assertEquals("Virtual list should have 5 items", 5,
                virtualList.getRowCount());

        waitUntil(driver -> "Item 4".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 3)));
        waitUntil(driver -> "Item 5".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 4)));
    }

    @Test
    public void bindItems_addThenRemove_virtualListCorrect() {
        waitUntil(driver -> virtualList.getRowCount() > 0);

        clickElementWithJs(ADD_ITEM_BUTTON);
        waitUntil(driver -> virtualList.getRowCount() == 4);
        Assert.assertEquals("After add: 4 items", 4, virtualList.getRowCount());

        clickElementWithJs(REMOVE_ITEM_BUTTON);
        waitUntil(driver -> virtualList.getRowCount() == 3);
        Assert.assertEquals("After remove: 3 items", 3,
                virtualList.getRowCount());

        // Verify original items are still there
        waitUntil(driver -> "Item 1".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 0)));
        waitUntil(driver -> "Item 2".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 1)));
        waitUntil(driver -> "Item 3".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 2)));
    }

    @Test
    public void bindItems_updateItem_virtualListUpdated() {
        waitUntil(driver -> virtualList.getRowCount() > 0);
        waitUntil(driver -> "Item 1".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 0)));

        clickElementWithJs(UPDATE_ITEM_BUTTON);

        waitUntil(driver -> "Item 1 Updated".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 0)));

        // Item count should remain the same
        Assert.assertEquals("Item count should still be 3", "3",
                $("span").id(ITEM_COUNT_SPAN).getText());
        Assert.assertEquals("Virtual list should still have 3 items", 3,
                virtualList.getRowCount());

        // Other items should remain unchanged
        waitUntil(driver -> "Item 2".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 1)));
        waitUntil(driver -> "Item 3".equals(
                VirtualListHelpers.getItemText(driver, virtualList, 2)));
    }
}
