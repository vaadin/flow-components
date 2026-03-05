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

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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

        Assert.assertEquals("First item should be 'Item 1'", "Item 1",
                VirtualListHelpers.getItemText(virtualList, 0));
        Assert.assertEquals("Second item should be 'Item 2'", "Item 2",
                VirtualListHelpers.getItemText(virtualList, 1));
        Assert.assertEquals("Third item should be 'Item 3'", "Item 3",
                VirtualListHelpers.getItemText(virtualList, 2));
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

        Assert.assertEquals("New item should be 'Item 4'", "Item 4",
                VirtualListHelpers.getItemText(virtualList, 3));
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

        Assert.assertEquals("First item still 'Item 1'", "Item 1",
                VirtualListHelpers.getItemText(virtualList, 0));
        Assert.assertEquals("Second item still 'Item 2'", "Item 2",
                VirtualListHelpers.getItemText(virtualList, 1));
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

        Assert.assertEquals("Fourth item should be 'Item 4'", "Item 4",
                VirtualListHelpers.getItemText(virtualList, 3));
        Assert.assertEquals("Fifth item should be 'Item 5'", "Item 5",
                VirtualListHelpers.getItemText(virtualList, 4));
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
        Assert.assertEquals("Item 1", VirtualListHelpers.getItemText(virtualList, 0));
        Assert.assertEquals("Item 2", VirtualListHelpers.getItemText(virtualList, 1));
        Assert.assertEquals("Item 3", VirtualListHelpers.getItemText(virtualList, 2));
    }

    @Test
    public void bindItems_updateItem_virtualListUpdated() {
        waitUntil(driver -> virtualList.getRowCount() > 0);
        Assert.assertEquals("Initial first item", "Item 1",
                VirtualListHelpers.getItemText(virtualList, 0));

        clickElementWithJs(UPDATE_ITEM_BUTTON);

        waitUntil(driver -> "Item 1 Updated"
                .equals(VirtualListHelpers.getItemText(virtualList, 0)));
        Assert.assertEquals("Updated first item", "Item 1 Updated",
                VirtualListHelpers.getItemText(virtualList, 0));

        // Item count should remain the same
        Assert.assertEquals("Item count should still be 3", "3",
                $("span").id(ITEM_COUNT_SPAN).getText());
        Assert.assertEquals("Virtual list should still have 3 items", 3,
                virtualList.getRowCount());

        // Other items should remain unchanged
        Assert.assertEquals("Second item unchanged", "Item 2",
                VirtualListHelpers.getItemText(virtualList, 1));
        Assert.assertEquals("Third item unchanged", "Item 3",
                VirtualListHelpers.getItemText(virtualList, 2));
    }
}
