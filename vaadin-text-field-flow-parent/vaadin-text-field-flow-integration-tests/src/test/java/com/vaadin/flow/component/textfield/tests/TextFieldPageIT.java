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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

/**
 * Integration tests for {@link TextField}.
 */
@TestPath("vaadin-text-field/text-field-test")
public class TextFieldPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertReadOnly() {
        WebElement webComponent = findElement(By.tagName("vaadin-text-field"));

        Assert.assertNull(webComponent.getAttribute("readonly"));

        WebElement button = findElement(By.id("read-only"));
        button.click();

        waitUntil(
                driver -> "true".equals(getProperty(webComponent, "readonly")));

        button.click();

        waitUntil(driver -> "false"
                .equals(getProperty(webComponent, "readonly")));
    }

    @Test
    public void assertRequired() {
        WebElement webComponent = findElement(By.tagName("vaadin-text-field"));
        Assert.assertNull(webComponent.getAttribute("required"));
        WebElement button = findElement(By.id("required"));
        button.click();
        waitUntil(
                driver -> "true".equals(getProperty(webComponent, "required")));

        button.click();
        waitUntil(driver -> "false"
                .equals(getProperty(webComponent, "required")));
    }

    @Test
    public void assertValueWithoutListener() {
        TextFieldElement field = $(TextFieldElement.class).id("value-change");
        WebElement input = field.$("input").first();
        input.sendKeys("foo");
        blur();
        WebElement button = findElement(By.id("get-value"));
        new Actions(getDriver())
                .moveToElement(button, button.getSize().getWidth() / 2,
                        button.getSize().getHeight() / 2)
                .click().build().perform();

        String value = findElement(By.className("text-field-value")).getText();
        Assert.assertEquals("foo", value);
    }

    @Test
    public void assertClearValue() {
        TextFieldElement field = $(TextFieldElement.class)
                .id("clear-text-field");
        WebElement input = field.$("input").first();
        input.sendKeys("foo");
        blur();

        WebElement clearButton = field.$("div").id("clearButton");
        clearButton.click();

        String value = findElement(By.id("clear-message")).getText();
        Assert.assertEquals("Old value: 'foo'. New value: ''.", value);
    }

    @Test
    public void assertCantMakeInvalidValueValidThroughClientManipulation() {
        ValidationTestHelper.testValidation(getCommandExecutor(), getContext(),
                $(TextFieldElement.class).id("invalid-test-field"));
    }

    @Test
    public void disabledTextFieldNotUpdating() {
        WebElement textField = findElement(By.id("disabled-text-field"));
        WebElement message = findElement(By.id("disabled-text-field-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");", textField);
        textField.sendKeys("abc");
        blur();

        message = findElement(By.id("disabled-text-field-message"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement textFieldValueDiv = findElement(By.id("text-field-value"));
        WebElement textField = findElement(
                By.id("text-field-with-value-change-listener"));
        updateValues(textFieldValueDiv, textField, true);
        $(RadioButtonGroupElement.class).first().selectByText(EAGER.toString());
        updateValues(textFieldValueDiv, textField, false);
    }

    @Test
    public void textFieldHasPlaceholder() {
        WebElement textField = findElement(
                By.id("text-field-with-value-change-listener"));
        Assert.assertEquals(textField.getAttribute("placeholder"),
                "placeholder text");
    }

    @Test
    public void assertFocusShortcut() {
        WebElement shortcutField = findElement(By.id("shortcut-field"));
        Assert.assertNull(
                "TextField should not be focused before the shortcut event is triggered.",
                shortcutField.getAttribute("focused"));

        SendKeysHelper.sendKeys(driver, Keys.ALT, "1");
        Assert.assertTrue(
                "TextField should be focused after the shortcut event is triggered.",
                shortcutField.getAttribute("focused").equals("true")
                        || shortcutField.getAttribute("focused").equals(""));
    }

    @Test
    public void assertHelperText() {
        TextFieldElement textFieldElement = $(TextFieldElement.class)
                .id("helper-text-field");
        Assert.assertEquals("Helper text", textFieldElement.getHelperText());
    }

    @Test
    public void clearHelper() {
        TextFieldElement textFieldElement = $(TextFieldElement.class)
                .id("helper-text-field");
        Assert.assertEquals("Helper text", textFieldElement.getHelperText());

        $(TestBenchElement.class).id("clear-helper-text-button").click();
        Assert.assertEquals("", textFieldElement.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        TextFieldElement textFieldElement = $(TextFieldElement.class)
                .id("helper-component-field");
        Assert.assertEquals("helper-component",
                textFieldElement.getHelperComponent().getAttribute("id"));
    }

    @Test
    public void clearHelperComponent() {
        TextFieldElement textFieldElement = $(TextFieldElement.class)
                .id("helper-component-field");
        Assert.assertEquals("helper-component",
                textFieldElement.getHelperComponent().getAttribute("id"));

        $(TestBenchElement.class).id("clear-helper-component-button").click();
        Assert.assertNull(textFieldElement.getHelperComponent());
    }

    private void updateValues(WebElement textFieldValueDiv,
            WebElement textField, boolean toggleBlur) {
        textField.sendKeys("a");
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text field value changed from '' to 'a'",
                textFieldValueDiv);

        textField.sendKeys(Keys.BACK_SPACE);
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text field value changed from 'a' to ''",
                textFieldValueDiv);
    }

    private void waitUntilTextsEqual(String expected, WebElement valueDiv) {
        waitUntil(driver -> expected.equals(valueDiv.getText()));
    }
}
