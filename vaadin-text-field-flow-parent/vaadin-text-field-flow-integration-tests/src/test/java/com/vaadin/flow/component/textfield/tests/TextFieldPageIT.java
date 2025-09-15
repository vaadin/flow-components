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
package com.vaadin.flow.component.textfield.tests;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

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

        Assert.assertNull(webComponent.getDomAttribute("readonly"));

        WebElement button = findElement(By.id("read-only"));
        button.click();

        waitUntil(
                driver -> "true".equals(getProperty(webComponent, "readonly")));

        button.click();

        waitUntil(driver -> "false"
                .equals(getProperty(webComponent, "readonly")));
    }

    @Test
    public void labelMatches() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withLabel("Text field label").all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class).withLabel("").all();
        Assert.assertEquals(4, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class).withLabel("nonexistent")
                .all();
        Assert.assertEquals(0, textFieldElements.size());
    }

    @Test
    public void labelContains() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withLabelContaining("should be").all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withLabelContaining("nonexistent").all();
        Assert.assertEquals(0, textFieldElements.size());
    }

    @Test
    public void labelBiPredicate() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withLabel("Helper", String::startsWith).all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withLabel("visible", String::endsWith).all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withLabel("text", TextFieldPageIT::containsIgnoreCase).all();
        Assert.assertEquals(3, textFieldElements.size());
    }

    @Test
    public void placeholderMatches() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withPlaceholder("placeholder text").all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class).withPlaceholder("").all();
        Assert.assertEquals(7, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withPlaceholder("nonexistent").all();
        Assert.assertEquals(0, textFieldElements.size());
    }

    @Test
    public void placeholderContains() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withPlaceholderContaining("holder").all();
        Assert.assertEquals(3, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withPlaceholderContaining("nonexistent").all();
        Assert.assertEquals(0, textFieldElements.size());
    }

    @Test
    public void placeholderBiPredicate() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withPlaceholder("Placeholder", String::startsWith).all();
        Assert.assertEquals(1, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withPlaceholder("text", String::endsWith).all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class).withPlaceholder(
                "placeholder", TextFieldPageIT::containsIgnoreCase).all();
        Assert.assertEquals(3, textFieldElements.size());
    }

    @Test
    public void labelAndPlaceholderMatches() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withLabel("Text field label")
                .withPlaceholder("placeholder text").all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withLabel("Press ALT + 1 to focus").withPlaceholder("").all();
        Assert.assertEquals(1, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class).withLabel("")
                .withPlaceholder("Placeholder caption").all();
        Assert.assertEquals(1, textFieldElements.size());
    }

    @Test
    public void labelAndPlaceholderContains() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withLabelContaining("Text").withPlaceholderContaining("text")
                .all();
        Assert.assertEquals(2, textFieldElements.size());
    }

    @Test
    public void captionMatches() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withCaption("Text field label").all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withCaption("Placeholder caption").all();
        Assert.assertEquals(1, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class).withCaption("nonexistent")
                .all();
        Assert.assertEquals(0, textFieldElements.size());
    }

    @Test
    public void captionContains() {
        List<TextFieldElement> textFieldElements = $(TextFieldElement.class)
                .withCaptionContaining("should be visible").all();
        Assert.assertEquals(2, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withCaptionContaining("holder").all();
        Assert.assertEquals(1, textFieldElements.size());

        textFieldElements = $(TextFieldElement.class)
                .withCaptionContaining("nonexistent").all();
        Assert.assertEquals(0, textFieldElements.size());
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

        field.clickClearButton();

        String value = findElement(By.id("clear-message")).getText();
        Assert.assertEquals("Old value: 'foo'. New value: ''.", value);
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
    public void assertFocusShortcut() {
        TextFieldElement shortcutField = $(TextFieldElement.class)
                .id("shortcut-field");
        Assert.assertFalse(
                "TextField should not be focused before the shortcut event is triggered.",
                shortcutField.hasAttribute("focused"));

        SendKeysHelper.sendKeys(driver, Keys.ALT, "1");
        Assert.assertTrue(
                "TextField should be focused after the shortcut event is triggered.",
                shortcutField.hasAttribute("focused"));
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
                textFieldElement.getHelperComponent().getDomProperty("id"));
    }

    @Test
    public void clearHelperComponent() {
        TextFieldElement textFieldElement = $(TextFieldElement.class)
                .id("helper-component-field");
        Assert.assertEquals("helper-component",
                textFieldElement.getHelperComponent().getDomProperty("id"));

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

    private static boolean containsIgnoreCase(String a, String b) {
        return a.toUpperCase().contains(b.toUpperCase());
    }
}
