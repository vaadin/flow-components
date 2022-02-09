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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;

import static org.junit.Assert.assertFalse;

@TestPath("vaadin-combo-box/combo-box-test")
public class ComboBoxPageIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void dataProvider_itemCaptionGenerator() {
        WebElement combo = findElement(By.id("combo"));

        List<?> items = getItems(combo);
        Assert.assertEquals(2, items.size());
        assertItem(items, 0, "foo");
        assertItem(items, 1, "bar");

        // update data provider
        findElement(By.id("update-provider")).click();

        waitUntil(driver -> "baz".equals(getItemLabel(getItems(combo), 0)));
        assertItem(getItems(combo), 1, "foobar");

        // update item caption generator
        findElement(By.id("update-caption-gen")).click();

        waitUntil(driver -> "3".equals(getItemLabel(getItems(combo), 0)));
        assertItem(getItems(combo), 1, "6");
    }

    @Test
    public void sizeRequestCount_setListDataProvider_sizeRequestedImmediately() {
        ComboBoxElement combo = $(ComboBoxElement.class)
                .id("combobox-list-size-request-count");
        WebElement span = findElement(By.id("list-size-request-count-span"));

        // check the data provider has been invoked immediately
        // First size request - check whether to set client side filter
        // Second size request - get real size of the data
        Assert.assertEquals("2", span.getText());

        combo.openPopup();
        // Third size request - fetch data
        Assert.assertEquals("3", span.getText());

        // update data provider
        findElement(By.id("size-request-count-update-provider")).click();

        // check that the data provider has been changed
        waitUntil(
                driver -> "new item".equals(getItemLabel(getItems(combo), 0)));
    }

    @Test
    public void createExternalDisableTest() {
        WebElement combo = findElement(By.id("client-test"));
        WebElement message = findElement(By.id("get-value"));
        waitUntil(driver -> "Nothing clicked yet...".equals(message.getText()));

        findElement(By.id("set-value")).click();
        Assert.assertEquals("bar", message.getText());

        findElement(By.id("disable-combo-box")).click();
        assertFalse("the combobox should be disabled", combo.isEnabled());
        Assert.assertEquals("bar", message.getText());
    }

    @Test
    public void selectedValue() {
        ComboBoxElement combo = $(ComboBoxElement.class).id("titles");
        combo.openPopup();

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                combo);

        WebElement selectionInfo = findElement(By.id("selected-titles"));
        Assert.assertEquals("MR", selectionInfo.getText());

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[1]",
                combo);
        Assert.assertEquals("MRS", selectionInfo.getText());
    }

    @Test
    public void presetValue() {
        WebElement combo = findElement(By.id("titles-with-preset-value"));
        String value = getSelectedItemLabel(combo);
        Assert.assertEquals("MRS", value);
    }

    @Test
    public void buttonsInsideComboBox_clickOnButton_messageIsUpdated() {
        WebElement message = findElement(By.id("button-renderer-message"));
        Assert.assertEquals("Nothing clicked yet...", message.getText());

        ComboBoxElement combo = $(ComboBoxElement.class).id("button-renderer");
        WebElement input = combo.$("input").first();
        // opens the dropdown
        clickElementWithJs(input);

        WebElement item = getItemElements().get(0);
        WebElement button = item.findElement(By.cssSelector("button"));
        clickElementWithJs(button);

        Assert.assertEquals("Button clicked: foo", message.getText());
    }

    @Test
    public void setValue_changeDataProvider_valueIsReset() {
        WebElement combo = findElement(By.id("combo"));

        findElement(By.id("update-provider")).click();
        waitUntil(driver -> "baz".equals(getItemLabel(getItems(combo), 0)));

        findElement(By.id("update-value")).click();
        String value = getSelectedItemLabel(combo);
        Assert.assertEquals("baz", value);

        findElement(By.id("update-provider")).click();
        Assert.assertNull(
                executeScript("return arguments[0].selectedItem", combo));
    }

    @Test
    public void setValueProgrammatically() {
        WebElement combo = findElement(By.id("external-selected-item"));
        Assert.assertEquals("foo", getSelectedItemLabel(combo));

        findElement(By.id("toggle-selected-item")).click();
        Assert.assertEquals("bar", getSelectedItemLabel(combo));

        findElement(By.id("toggle-selected-item")).click();
        Assert.assertEquals("foo", getSelectedItemLabel(combo));
    }

    @Test
    public void changeValue_IsFromClientIsSetAccordingly() {
        ComboBoxElement combo = $(ComboBoxElement.class).id("updatable-combo");
        combo.openPopup();
        WebElement message = findElement(By.id("updatable-combo-message"));
        WebElement button = findElement(By.id("updatable-combo-button"));

        Assert.assertEquals("", getSelectedItemLabel(combo));

        button.click();
        Assert.assertEquals("Item 2", getSelectedItemLabel(combo));
        Assert.assertEquals("Value: Item 2 isFromClient: false",
                message.getText());

        executeScript(
                "arguments[0].selectedItem = arguments[0].filteredItems[0]",
                combo);
        Assert.assertEquals("Item 1", getSelectedItemLabel(combo));
        Assert.assertEquals("Value: Item 1 isFromClient: true",
                message.getText());

        button.click();
        Assert.assertEquals("Item 2", getSelectedItemLabel(combo));
        Assert.assertEquals("Value: Item 2 isFromClient: false",
                message.getText());
    }

    @Test
    public void changeValue_oldSelectedItemKeyIsReset() {
        ComboBoxElement combo = $(ComboBoxElement.class)
                .id("update-on-change-combo");
        combo.openPopup();

        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-combo-box-item");

        TestBenchElement item1 = items.get(0);
        TestBenchElement item2 = items.get(1);

        Assert.assertFalse(item1.hasAttribute("selected"));
        Assert.assertTrue(item2.hasAttribute("selected"));

        item1.click();

        combo.openPopup();

        Assert.assertTrue(item1.hasAttribute("selected"));
        Assert.assertFalse(item2.hasAttribute("selected"));
    }

    @Test
    public void setValue_setLabelGenerator_selectedItemLabelUpdated() {
        ComboBoxElement combo = $(ComboBoxElement.class)
                .id("label-generator-after-value");
        Assert.assertEquals("foo", combo.getSelectedText());
    }

}
