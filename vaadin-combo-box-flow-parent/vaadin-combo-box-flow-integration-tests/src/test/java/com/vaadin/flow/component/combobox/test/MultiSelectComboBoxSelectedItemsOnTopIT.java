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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

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
    private TestBenchElement setComponentRenderer;
    private TestBenchElement useCustomValueSetListener;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        setSelectedOnTop = $("button").id("set-selected-on-top");
        unsetSelectedOnTop = $("button").id("unset-selected-on-top");
        setComponentRenderer = $("button").id("set-component-renderer");
        useCustomValueSetListener = $("button")
                .id("use-custom-value-set-listener");
    }

    @Test
    public void setSelectedItemsOnTop_selectItems_itemsGrouped() {
        setSelectedOnTop.click();

        comboBox.selectByText("Item 2");
        comboBox.selectByText("Item 3");

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        ElementQuery<TestBenchElement> items = getItems();

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

        ElementQuery<TestBenchElement> items = getItems();

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

        ElementQuery<TestBenchElement> items = getItems();

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("Item 1", item1.getText());
        Assert.assertEquals("Item 2", item2.getText());

        Assert.assertFalse(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));
    }

    @Test
    public void componentRendererAndCustomValues_selectedItemsOnTop_addAndSelectItems_deselectFirst_itemsCorrect() {
        setComponentRenderer.click();
        useCustomValueSetListener.click();
        setSelectedOnTop.click();

        addCustomItem("foo");
        addCustomItem("bar");

        comboBox.deselectByText("foo");

        comboBox.closePopup();
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        ElementQuery<TestBenchElement> items = getItems();

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertEquals("bar", item1.getText());
        Assert.assertEquals("foo", item2.getText());

        Assert.assertTrue(item1.hasAttribute("selected"));
        Assert.assertFalse(item2.hasAttribute("selected"));
    }

    private ElementQuery<TestBenchElement> getItems() {
        return comboBox.$("vaadin-multi-select-combo-box-item");
    }

    private void addCustomItem(String item) {
        comboBox.sendKeys(item);
        comboBox.waitForLoadingFinished();
        comboBox.sendKeys(Keys.ENTER);
        comboBox.waitForLoadingFinished();
    }
}
