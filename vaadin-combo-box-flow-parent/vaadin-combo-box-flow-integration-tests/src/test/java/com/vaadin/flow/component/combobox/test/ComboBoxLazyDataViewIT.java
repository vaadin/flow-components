/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.vaadin.flow.component.combobox.test.ComboBoxLazyDataViewPage.COMBO_BOX_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxLazyDataViewPage.GET_ITEMS_BUTTON_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxLazyDataViewPage.GET_ITEM_BUTTON_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxLazyDataViewPage.ITEMS_LIST_ID;
import static com.vaadin.flow.component.combobox.test.ComboBoxLazyDataViewPage.SWITCH_TO_UNKNOWN_COUNT_BUTTON_ID;

@TestPath("vaadin-combo-box/combobox-lazy-data-view-page")
public class ComboBoxLazyDataViewIT extends AbstractComboBoxIT {

    private ComboBoxElement comboBox;
    private TestBenchElement itemsList;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        comboBox = $(ComboBoxElement.class).id(COMBO_BOX_ID);
        itemsList = $("span").id(ITEMS_LIST_ID);
    }

    @Test
    public void getItem_withDefinedItemCountAndClientSideFilter_returnsItemFromNotFilteredSet() {
        comboBox.setFilter("777");

        waitForItems(comboBox, items -> items.size() == 1
                && "Item 777".equals(getItemLabel(items, 0)));

        clickButton(GET_ITEM_BUTTON_ID);

        // Checks the filter has been cleared after closing the drop down.
        // ComboBox clears the cache after closing, so the item's values are
        // not checked here
        waitForItems(comboBox, items -> items.size() == 1000);

        Assert.assertEquals("The client filter shouldn't impact the items",
                "Item 0", itemsList.getText());

        comboBox.openPopup();

        waitForItems(comboBox, items -> items.size() == 1000
                && "Item 0".equals(getItemLabel(items, 0)));

        clickButton(GET_ITEM_BUTTON_ID);

        Assert.assertEquals("The client filter shouldn't impact the items",
                "Item 0", itemsList.getText());
    }

    @Test
    public void getItem_withUnknownItemCountAndClientSideFilter_returnsItemFromNotFilteredSet() {
        clickButton(SWITCH_TO_UNKNOWN_COUNT_BUTTON_ID);
        comboBox.setFilter("777");

        waitForItems(comboBox, items -> items.size() == 1
                && "Item 777".equals(getItemLabel(items, 0)));

        clickButton(GET_ITEM_BUTTON_ID);

        // Checks the filter has been cleared after closing the drop down
        // ComboBox clears the cache after closing, so the item's values are
        // not checked here
        waitForItems(comboBox, items -> items.size() == 200);

        Assert.assertEquals("The client filter shouldn't impact the items",
                "Item 0", itemsList.getText());

        comboBox.openPopup();

        waitForItems(comboBox, items -> items.size() == 200
                && "Item 0".equals(getItemLabel(items, 0)));

        clickButton(GET_ITEM_BUTTON_ID);

        Assert.assertEquals("The client filter shouldn't impact the items",
                "Item 0", itemsList.getText());
    }

    @Test
    public void getItems_withUnknownItemCountAndClientSideFilter_returnsNotFilteredItems() {
        clickButton(SWITCH_TO_UNKNOWN_COUNT_BUTTON_ID);
        comboBox.setFilter("777");

        waitForItems(comboBox, items -> items.size() == 1
                && "Item 777".equals(getItemLabel(items, 0)));

        clickButton(GET_ITEMS_BUTTON_ID);

        // Checks the filter has been cleared after closing the drop down
        // ComboBox clears the cache after closing, so the item's values are
        // not checked here
        waitForItems(comboBox, items -> items.size() == 200);

        Assert.assertTrue("The client filter shouldn't impact the items",
                itemsList.getText().startsWith("Item 0,Item 1,Item 2")
                        && itemsList.getText()
                                .endsWith("Item 997,Item 998,Item 999"));

        comboBox.openPopup();

        waitForItems(comboBox,
                items -> items.size() == 200
                        && "Item 0".equals(getItemLabel(items, 0))
                        && "Item 49".equals(getItemLabel(items, 49)));

        clickButton(GET_ITEMS_BUTTON_ID);

        Assert.assertTrue("The client filter shouldn't impact the items",
                itemsList.getText().startsWith("Item 0,Item 1,Item 2")
                        && itemsList.getText()
                                .endsWith("Item 997,Item 998,Item 999"));
    }

    @Test
    public void getItems_withDefinedItemCountAndClientSideFilter_returnsNotFilteredItems() {
        comboBox.setFilter("777");

        waitForItems(comboBox, items -> items.size() == 1
                && "Item 777".equals(getItemLabel(items, 0)));

        clickButton(GET_ITEMS_BUTTON_ID);

        // Checks the filter has been cleared after closing the drop down
        // ComboBox clears the cache after closing, so the item's values are
        // not checked here
        waitForItems(comboBox, items -> items.size() == 1000);

        Assert.assertTrue("The client filter shouldn't impact the items",
                itemsList.getText().startsWith("Item 0,Item 1,Item 2")
                        && itemsList.getText()
                                .endsWith("Item 997,Item 998,Item 999"));

        comboBox.openPopup();

        waitForItems(comboBox,
                items -> items.size() == 1000
                        && "Item 0".equals(getItemLabel(items, 0))
                        && "Item 49".equals(getItemLabel(items, 49)));

        clickButton(GET_ITEMS_BUTTON_ID);

        Assert.assertTrue("The client filter shouldn't impact the items",
                itemsList.getText().startsWith("Item 0,Item 1,Item 2")
                        && itemsList.getText()
                                .endsWith("Item 997,Item 998,Item 999"));
    }

}
