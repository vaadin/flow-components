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

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/filter-debounce")
public class FilterDebounceIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;
    private TestBenchElement input;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
        input = $("input").id("external-input");
    }

    /**
     * vaadin/vaadin-combo-box-flow#296 - Filtering problem using slow
     * DataProvider
     */
    @Test
    public void filterDebounce_doesNotHang() {
        combo.setFilter("a");
        combo.selectByText("aaa");
        tabOutAndBackFromCombo();
        Assert.assertTrue(combo.isPopupOpen());
        combo.getCommandExecutor().waitForVaadin();
        tabOutAndBackFromCombo();
        waitForExpectedItems();
        tabOutAndBackFromCombo();
        waitForExpectedItems();
    }

    private void tabOutAndBackFromCombo() {
        combo.sendKeys("\t");
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));
        combo.sendKeys("a");
    }

    @SuppressWarnings("unchecked")
    private void waitForExpectedItems() {
        waitForItems(combo,
                items -> items.size() == 1
                        && ((Map<Object, Object>) items.get(0)).get("label")
                                .equals("aaa"));
    }
}
