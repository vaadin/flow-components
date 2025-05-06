/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/selected-items-on-top")
public class MultiSelectComboBoxSelectedItemsOnTopIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement setSelectedOnTop;
    private TestBenchElement unsetSelectedOnTop;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        setSelectedOnTop = $("button").id("set-selected-on-top");
        unsetSelectedOnTop = $("button").id("unset-selected-on-top");
    }

    @Test
    public void setSelectedItemsOnTop_selectItems_itemsGrouped() {
        setSelectedOnTop.click();

        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 2", item1.getText());
        Assert.assertEquals("Item 3", item2.getText());

        Assert.assertTrue(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }

    @Test
    public void selectItems_setSelectedItemsOnTop_itemsGrouped() {
        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        setSelectedOnTop.click();

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 2", item1.getText());
        Assert.assertEquals("Item 3", item2.getText());

        Assert.assertTrue(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }

    @Test
    public void setSelectedItemsOnTop_unset_itemsNotGrouped() {
        setSelectedOnTop.click();

        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        comboBox.closePopup();

        unsetSelectedOnTop.click();

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        TestBenchElement overlay = $("vaadin-multi-select-combo-box-overlay")
                .first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-multi-select-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 1", item1.getText());
        Assert.assertEquals("Item 2", item2.getText());

        Assert.assertFalse(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }
}
