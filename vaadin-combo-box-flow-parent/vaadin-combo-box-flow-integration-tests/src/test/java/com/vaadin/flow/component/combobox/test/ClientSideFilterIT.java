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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.BACKEND_COMBO_BOX;
import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.BACKEND_COMBO_BOX_ITEM_COUNT_SPAN_ID;
import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.CLIENT_FILTER_COMBO_BOX;
import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.CLIENT_FILTER_COMBO_BOX_ITEM_COUNT_SPAN_ID;
import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.IN_MEMORY_COMBO_BOX;
import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.IN_MEMORY_COMBO_BOX_ITEM_COUNT_SPAN_ID;
import static com.vaadin.flow.component.combobox.test.ClientSideFilterPage.OPTIONS_COMBO_BOX;

@TestPath("vaadin-combo-box/clientside-filter")
public class ClientSideFilterIT extends AbstractComboBoxIT {
    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void filter_itemsShouldBeThere() {
        // First combobox.
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(OPTIONS_COMBO_BOX);

        comboBox.sendKeys("2");

        waitForItems(comboBox, items -> items.size() == 1
                && "Option 2".equals(getItemLabel(items, 0)));

        comboBox.sendKeys(Keys.BACK_SPACE);

        waitForItems(comboBox, items -> items.size() == 4);

        comboBox.sendKeys("3");

        waitForItems(comboBox, items -> items.size() == 1
                && "Option 3".equals(getItemLabel(items, 0)));

        comboBox.closePopup();

        // Second combobox.
        comboBox = $(ComboBoxElement.class).id(CLIENT_FILTER_COMBO_BOX);

        comboBox.sendKeys("mo");

        waitForItems(comboBox, items -> items.size() == 1
                && "Mozilla Firefox".equals(getItemLabel(items, 0)));

        comboBox.closePopup();
        comboBox.openPopup();

        waitForItems(comboBox,
                items -> items.size() == 5
                        && "Google Chrome".equals(getItemLabel(items, 0))
                        && "Mozilla Firefox".equals(getItemLabel(items, 1))
                        && "Opera".equals(getItemLabel(items, 2))
                        && "Apple Safari".equals(getItemLabel(items, 3))
                        && "Microsoft Edge".equals(getItemLabel(items, 4)));

    }

    @Test
    public void itemCountChange_clientFilterAppliedOnItemsCountLessThanPageSize_serverSideNotNotified() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(CLIENT_FILTER_COMBO_BOX);

        Assert.assertEquals("Expected 5 items before opening", 5,
                getItemCount(CLIENT_FILTER_COMBO_BOX_ITEM_COUNT_SPAN_ID));

        comboBox.openPopup();

        waitForItems(comboBox, items -> items.size() == 5);

        Assert.assertEquals("Expected 5 items before filtering", 5,
                getItemCount(CLIENT_FILTER_COMBO_BOX_ITEM_COUNT_SPAN_ID));

        comboBox.sendKeys("M");

        waitForItems(comboBox, items -> items.size() == 3);

        Assert.assertEquals(
                "Expected no item count change events on client filter change",
                5, getItemCount(CLIENT_FILTER_COMBO_BOX_ITEM_COUNT_SPAN_ID));
    }

    @Test
    public void itemCountChange_clientFilterAppliedOnItemsCountMoreThanPageSize_serverSideNotified() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(IN_MEMORY_COMBO_BOX);

        Assert.assertEquals("Expected 100 items before opening", 100,
                getItemCount(IN_MEMORY_COMBO_BOX_ITEM_COUNT_SPAN_ID));

        comboBox.openPopup();

        waitForItems(comboBox, items -> items.size() == 100);

        Assert.assertEquals("Expected 100 items before filtering", 100,
                getItemCount(IN_MEMORY_COMBO_BOX_ITEM_COUNT_SPAN_ID));

        comboBox.sendKeys("1");

        waitForItems(comboBox, items -> items.size() == 19);

        Assert.assertEquals(
                "Expected no item count change events on client filter change",
                100, getItemCount(IN_MEMORY_COMBO_BOX_ITEM_COUNT_SPAN_ID));
    }

    @Test
    public void itemCountChange_clientFilterAppliedOnBackendData_serverSideNotified() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id(BACKEND_COMBO_BOX);

        Assert.assertEquals("Expected 0 items before opening", 0,
                getItemCount(BACKEND_COMBO_BOX_ITEM_COUNT_SPAN_ID));

        comboBox.openPopup();

        waitForItems(comboBox, items -> items.size() == 30);

        Assert.assertEquals("Expected 30 items before filtering", 30,
                getItemCount(BACKEND_COMBO_BOX_ITEM_COUNT_SPAN_ID));

        comboBox.sendKeys("1");

        waitForItems(comboBox, items -> items.size() == 12);

        Assert.assertEquals(
                "Expected no item count change events on client filter change",
                30, getItemCount(BACKEND_COMBO_BOX_ITEM_COUNT_SPAN_ID));
    }

    private int getItemCount(String id) {
        return Integer.parseInt($("span").id(id).getText());
    }

}
