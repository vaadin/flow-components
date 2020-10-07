/*
 * Copyright 2000-2018 Vaadin Ltd.
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
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

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
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();

        comboBox.sendKeys("2");

        waitForItems(comboBox, items -> items.size() == 1
                && "Option 2".equals(getItemLabel(items, 0)));

        comboBox.sendKeys(Keys.BACK_SPACE);

        waitForItems(comboBox, items -> items.size() == 4);

        comboBox.sendKeys("3");

        waitForItems(comboBox, items -> items.size() == 1
                && "Option 3".equals(getItemLabel(items, 0)));

        // Second combobox.
        comboBox = $(ComboBoxElement.class).get(1);

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

}
