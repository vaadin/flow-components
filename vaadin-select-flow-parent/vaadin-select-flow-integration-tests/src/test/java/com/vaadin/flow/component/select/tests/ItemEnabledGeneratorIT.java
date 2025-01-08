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
package com.vaadin.flow.component.select.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-select/")
public class ItemEnabledGeneratorIT extends AbstractSelectIT {

    @Test
    public void testItemEnabledProvider_setOddItemsDisabled_itemHasDisabledAttribute() {
        page.toggleItemEnabledProvider(true);
        verify.itemEnabled(itemIndex -> itemIndex % 2 == 0);

        page.toggleItemEnabledProvider(false);
        verify.itemEnabled(integer -> true);
    }

    @Test
    public void testItemEnabledProvider_initiallySetOddItemsDisabled_itemHasDisabledAttribute() {
        openWithExtraParameter("itemEnabledProvider");
        verify.itemEnabled(itemIndex -> itemIndex % 2 == 0);
    }

    @Test
    public void testItemEnabledProvider_setOddItemsDisabled_userCanSelectItemEnabledItems() {
        page.toggleItemEnabledProvider(true);

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("Invalid amout of items", getInitialNumberOfItems(),
                items.size());
        int valueChangeEvents = 0;
        String previousValue = null;
        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            if (i % 2 == 0) {
                itemElement.click();
                verify.valueChangeEvent("Item-" + i, previousValue, true,
                        valueChangeEvents);
                verify.selectedItem("Item-" + i);
                valueChangeEvents++;
                previousValue = "Item-" + i;
            } else {
                verify.userSelectionDoesntFireEvent(i);
            }
        }
    }

    @Test
    public void testItemDisabled_userEnablesItem_selectionNotAcceptedOnServer() {
        page.toggleItemEnabledProvider(true);

        selectElement.getItems().get(1).setProperty("disabled", false);

        verify.userSelectionDoesntFireEvent(1);
    }

    @Test
    public void testItemsDisabled_selectDisabledAndEnabled_itemsStayDisabled() {
        page.toggleItemEnabledProvider(true);
        page.toggleEnabled(false);
        Assert.assertFalse(selectElement.isEnabled());

        page.toggleEnabled(true);
        verify.itemEnabled(itemIndex -> itemIndex % 2 == 0);
    }

    @Override
    protected int getInitialNumberOfItems() {
        return 6;
    }
}
