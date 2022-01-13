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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/refresh-data-provider")
public class RefreshDataProviderIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void emptyComboBox_addItemsAndRefreshAll_addedItemsIncluded() {
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        testItems(
                "Unexpected items in the combobox, there shouldn't be any item",
                comboBox);

        findElement(By.id("update")).click();

        waitUntil(e -> comboBox.getOptions().size() == 2);

        testItems(
                "Expected to contain added items after refreshing data provider",
                comboBox, "foo", "bar");
    }

    @Test
    public void refreshItem_onlyOneItemUpdated() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("refresh-item-combo-box");
        testItems("Unexpected initial items", comboBox, "foo", "bar");

        findElement(By.id("refresh-item")).click();

        testItems("Expected only the second item to be updated", comboBox,
                "foo", "bar updated");
    }

    private void testItems(String message, ComboBoxElement comboBox,
            String... expectedItems) {
        comboBox.openPopup();
        List<String> items = comboBox.getOptions();
        Assert.assertArrayEquals(message, expectedItems,
                items.toArray(new String[items.size()]));
        comboBox.closePopup();
    }

}
