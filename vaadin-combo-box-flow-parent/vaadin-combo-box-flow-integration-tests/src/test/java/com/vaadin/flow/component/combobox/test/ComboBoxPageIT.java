/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

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

        TestBenchElement combo = $("*").id("button-renderer");
        WebElement textField = combo.$("*").id("input");
        // opens the dropdown
        clickElementWithJs(textField);

        // long trip to the depths of the component to find the button
        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        TestBenchElement content = overlay.$("*").id("content");
        TestBenchElement ironList = content.$("*").id("selector");
        TestBenchElement item = ironList.$("vaadin-combo-box-item").first();
        TestBenchElement button = item.$("button").first();
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
    public void selectValueFromClient_open_selectValueFromServer_onlyOneItemMarkedAsSelected() {
        // Select item 1 on client, this also opens / renders the overlay
        ComboBoxElement combo = $(ComboBoxElement.class).id("updatable-combo");
        combo.selectByText("Item 1");
        // Select item 2 from server
        WebElement button = findElement(By.id("updatable-combo-button"));
        button.click();

        // Get overlay items and verify only item 2 is selected
        combo.openPopup();
        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        List<TestBenchElement> items = overlay.$("div").id("content")
                .$("vaadin-combo-box-item").all();

        items.forEach(
                item -> Assert.assertEquals("Item 2".equals(item.getText()),
                        item.hasAttribute("selected")));
    }

    @Test
    public void setValue_setLabelGenerator_selectedItemLabelUpdated() {
        ComboBoxElement combo = $(ComboBoxElement.class)
                .id("label-generator-after-value");
        Assert.assertEquals("foo", combo.getSelectedText());
    }

}
