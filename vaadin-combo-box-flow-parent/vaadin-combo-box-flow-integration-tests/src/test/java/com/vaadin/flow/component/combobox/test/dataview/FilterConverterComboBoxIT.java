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

package com.vaadin.flow.component.combobox.test.dataview;

import static com.vaadin.flow.component.combobox.test.dataview.FilterConverterComboBoxPage.DEFINED_COUNT_COMBO_BOX_ID;
import static com.vaadin.flow.component.combobox.test.dataview.FilterConverterComboBoxPage.UNKNOWN_COUNT_COMBO_BOX_ID;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.test.AbstractComboBoxIT;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.Keys;

@TestPath("filter-converter-lazy-data-view-combo-box-page")
public class FilterConverterComboBoxIT extends AbstractComboBoxIT {

    private ComboBoxElement definedCountComboBox;
    private ComboBoxElement unknownCountComboBox;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 1);

        definedCountComboBox = $(ComboBoxElement.class)
                .id(DEFINED_COUNT_COMBO_BOX_ID);

        unknownCountComboBox = $(ComboBoxElement.class)
                .id(UNKNOWN_COUNT_COMBO_BOX_ID);
    }

    @Test
    public void definedCount_setsClientFilterAndScroll_itemsFiltered() {
        // Apply text filter
        definedCountComboBox.sendKeys("50", Keys.ENTER);

        waitForItems(definedCountComboBox,
                items -> items.size() == 5
                        && "Item 50".equals(getItemLabel(items, 0))
                        && "Item 450".equals(getItemLabel(items, 4)));

        // Reset text filter
        definedCountComboBox.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);

        waitForItems(definedCountComboBox, items -> items.size() == 500
                && "Item 0".equals(getItemLabel(items, 0)));

        scrollToItem(definedCountComboBox, 499);

        waitUntilTextInContent("Item 499");
    }

    @Test
    public void unknownCount_setsClientFilterAndScroll_itemsFiltered() {
        // Apply text filter
        unknownCountComboBox.sendKeys("50", Keys.ENTER);

        waitForItems(unknownCountComboBox,
                items -> items.size() == 5
                        && "Item 50".equals(getItemLabel(items, 0))
                        && "Item 450".equals(getItemLabel(items, 4)));

        // Reset text filter
        unknownCountComboBox.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);

        waitForItems(unknownCountComboBox, items -> items.size() == 200
                && "Item 0".equals(getItemLabel(items, 0)));

        scrollToItem(unknownCountComboBox, 199);

        waitUntilTextInContent("Item 199");

        scrollToItem(unknownCountComboBox, 399);

        waitUntilTextInContent("Item 399");
    }
}
