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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/set-items-later")
public class SetItemsLaterIT extends AbstractComboBoxIT {
    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void clickButton_comboBoxShouldContainsItems() {
        open();
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();

        waitForItems(comboBox, items -> items.size() == 0);

        WebElement button = findElement(By.id("set-items-button"));
        button.click();

        comboBox.openPopup();

        waitForItems(comboBox,
                items -> items != null && items.size() == 2
                        && "foo".equals(getItemLabel(items, 0))
                        && "bar".equals(getItemLabel(items, 1)));

    }

    @Test
    public void dontSetItemsOrDataProvider_openComboBox_loadingStateResolved() {
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        comboBox.openPopup();
        assertLoadingStateResolved(comboBox);
        assertLoadedItemsCount("ComboBox should not have items", 0, comboBox);
    }

    @Test
    public void openEmptyComboBox_setItems_open_containsItems() {
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();
        comboBox.openPopup();
        comboBox.closePopup();
        WebElement button = findElement(By.id("set-items-button"));
        button.click();
        comboBox.openPopup();

        waitUntil(e -> comboBox.getOptions().size() == 2);

        assertLoadedItemsCount("ComboBox should have loaded 2 items", 2,
                comboBox);
        assertRendered("foo");
        assertRendered("bar");
    }

}
