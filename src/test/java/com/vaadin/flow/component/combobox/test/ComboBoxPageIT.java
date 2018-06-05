/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertFalse;

@TestPath("combo-box-test")
public class ComboBoxPageIT extends AbstractComponentIT {

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

        waitUntil(driver -> "baz".equals(getItem(getItems(combo), 0)));
        assertItem(getItems(combo), 1, "foobar");

        // update item caption generator
        findElement(By.id("update-caption-gen")).click();

        waitUntil(driver -> "3".equals(getItem(getItems(combo), 0)));
        assertItem(getItems(combo), 1, "6");
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
        WebElement combo = findElement(By.id("titles"));

        executeScript("arguments[0].selectedItem = arguments[0].items[0]",
                combo);

        WebElement selectionInfo = findElement(By.id("selected-titles"));
        Assert.assertEquals("MR", selectionInfo.getText());

        executeScript("arguments[0].selectedItem = arguments[0].items[1]",
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

        WebElement combo = findElement(By.id("button-renderer"));
        WebElement textField = findInShadowRoot(combo, By.id("input")).get(0);
        // opens the dropdown
        clickElementWithJs(textField);

        // long trip to the depths of the component to find the button
        WebElement overlay = findElement(
                By.tagName("vaadin-combo-box-overlay"));
        WebElement content = findInShadowRoot(overlay, By.id("content")).get(0);
        WebElement ironList = findInShadowRoot(content, By.id("selector"))
                .get(0);
        WebElement item = ironList
                .findElement(By.tagName("vaadin-combo-box-item"));
        WebElement button = findInShadowRoot(item, By.cssSelector("button"))
                .get(0);
        clickElementWithJs(button);

        Assert.assertEquals("Button clicked: foo", message.getText());
    }

    @Test
    public void setValue_changeDataProvider_valueIsReset() {
        WebElement combo = findElement(By.id("combo"));

        findElement(By.id("update-provider")).click();
        waitUntil(driver -> "baz".equals(getItem(getItems(combo), 0)));

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
        WebElement combo = findElement(By.id("updatable-combo"));
        WebElement message = findElement(By.id("updatable-combo-message"));
        WebElement button = findElement(By.id("updatable-combo-button"));

        Assert.assertEquals("", getSelectedItemLabel(combo));

        button.click();
        Assert.assertEquals("Item 2", getSelectedItemLabel(combo));
        Assert.assertEquals("Value: Item 2 isFromClient: false",
                message.getText());

        executeScript("arguments[0].selectedItem = arguments[0].items[0]",
                combo);
        Assert.assertEquals("Item 1", getSelectedItemLabel(combo));
        Assert.assertEquals("Value: Item 1 isFromClient: true",
                message.getText());

        button.click();
        Assert.assertEquals("Item 2", getSelectedItemLabel(combo));
        Assert.assertEquals("Value: Item 2 isFromClient: false",
                message.getText());
    }

    private List<?> getItems(WebElement combo) {
        List<?> items = (List<?>) getCommandExecutor()
                .executeScript("return arguments[0].items;", combo);
        return items;
    }

    private void assertItem(List<?> items, int index, String caption) {
        Map<?, ?> map = (Map<?, ?>) items.get(index);
        Assert.assertEquals(caption, map.get("label"));
    }

    private Object getItem(List<?> items, int index) {
        Map<?, ?> map = (Map<?, ?>) items.get(index);
        return map.get("label");
    }

    private String getSelectedItemLabel(WebElement combo) {
        return String.valueOf(executeScript(
                "return arguments[0].selectedItem ? arguments[0].selectedItem.label : \"\"",
                combo));
    }
}
