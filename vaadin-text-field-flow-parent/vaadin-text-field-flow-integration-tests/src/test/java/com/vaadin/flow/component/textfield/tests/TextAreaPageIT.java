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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link TextArea}.
 */
@TestPath("vaadin-text-field/text-area-test")
public class TextAreaPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertClearValue() {
        TextAreaElement field = $(TextAreaElement.class).id("clear-text-area");

        TestBenchElement input = field.$("textarea").first();
        input.sendKeys("foo");
        blur();

        field.clickClearButton();

        String value = findElement(By.id("clear-message")).getText();
        Assert.assertEquals("Old value: 'foo'. New value: ''.", value);
    }

    @Test
    public void assertFocusShortcut() {
        TextAreaElement shortcutField = $(TextAreaElement.class)
                .id("shortcut-field");
        Assert.assertFalse(
                "TextArea should not be focused before the shortcut event is triggered.",
                shortcutField.hasAttribute("focused"));

        SendKeysHelper.sendKeys(driver, Keys.ALT, "1");
        Assert.assertTrue(
                "TextArea should be focused after the shortcut event is triggered.",
                shortcutField.hasAttribute("focused"));
    }

    @Test
    public void disabledTextAreaNotUpdating() {
        WebElement textArea = findElement(By.id("disabled-text-area"));
        WebElement message = findElement(By.id("disabled-text-area-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");", textArea);
        textArea.sendKeys("abc");
        blur();

        message = findElement(By.id("disabled-text-area-message"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement textFieldValueDiv = findElement(By.id("text-area-value"));
        WebElement textArea = findElement(
                By.id("text-area-with-value-change-listener"));

        updateValues(textFieldValueDiv, textArea, true);
        $(RadioButtonGroupElement.class).first().selectByText(EAGER.toString());
        updateValues(textFieldValueDiv, textArea, false);
    }

    private void updateValues(WebElement textFieldValueDiv, WebElement textArea,
            boolean toggleBlur) {
        textArea.sendKeys("a");
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text area value changed from '' to 'a'",
                textFieldValueDiv);

        textArea.sendKeys(Keys.BACK_SPACE);
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text area value changed from 'a' to ''",
                textFieldValueDiv);
    }

    private void waitUntilTextsEqual(String expected, WebElement valueDiv) {
        waitUntil(driver -> expected.equals(valueDiv.getText()));
    }

    @Test
    public void maxHeight() {
        WebElement textArea = findElement(By.id("text-area-with-max-height"));

        IntStream.range(0, 20).forEach(i -> textArea.sendKeys("foobarbaz\n"));

        Assert.assertTrue(textArea.getSize().getHeight() <= 125);
    }

    @Test
    public void minHeight() throws InterruptedException {
        WebElement textArea = findElement(By.id("text-area-with-min-height"));

        IntStream.range(0, 20).forEach(i -> textArea.sendKeys("foobarbaz\n"));

        Assert.assertTrue(textArea.getSize().getHeight() >= 125);

        IntStream.range(0, 20 * "foobarbaz\n".length())
                .forEach(i -> textArea.sendKeys(Keys.BACK_SPACE));

        Assert.assertEquals(125, textArea.getSize().getHeight());
    }

    @Test
    public void assertHelperText() {
        TextAreaElement textAreaElement = $(TextAreaElement.class)
                .id("helper-text-field");
        Assert.assertEquals("Helper text", textAreaElement.getHelperText());
    }

    @Test
    public void clearHelper() {
        TextAreaElement textAreaElement = $(TextAreaElement.class)
                .id("helper-text-field");
        Assert.assertEquals("Helper text", textAreaElement.getHelperText());

        $(TestBenchElement.class).id("clear-helper-text-button").click();
        Assert.assertEquals("", textAreaElement.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        TextAreaElement textAreaElement = $(TextAreaElement.class)
                .id("helper-component-field");
        Assert.assertEquals("helper-component",
                textAreaElement.getHelperComponent().getDomAttribute("id"));
    }

    @Test
    public void clearHelperComponent() {
        TextAreaElement textAreaElement = $(TextAreaElement.class)
                .id("helper-component-field");
        Assert.assertEquals("helper-component",
                textAreaElement.getHelperComponent().getDomAttribute("id"));

        $(TestBenchElement.class).id("clear-helper-component-button").click();
        Assert.assertNull(textAreaElement.getHelperComponent());
    }

    @Test
    public void scrollToEnd() {
        TextAreaElement textArea = $(TextAreaElement.class)
                .id("text-area-with-max-height");
        textArea.setValue("LONGTEXT".repeat(30));

        TestBenchElement inputContainer = textArea.$("vaadin-input-container")
                .first();
        inputContainer.setProperty("scrollTop", 0);

        $("button").id("scroll-to-end").click();

        int scrollTop = inputContainer.getPropertyInteger("scrollTop");
        int scrollHeight = inputContainer.getPropertyInteger("scrollHeight");
        int clientHeight = inputContainer.getPropertyInteger("clientHeight");
        Assert.assertEquals(scrollHeight - clientHeight, scrollTop);
    }

    @Test
    public void scrollToStart() {
        TextAreaElement textArea = $(TextAreaElement.class)
                .id("text-area-with-max-height");
        textArea.setValue("LONGTEXT".repeat(30));

        TestBenchElement inputContainer = textArea.$("vaadin-input-container")
                .first();
        inputContainer.setProperty("scrollTop", 100);

        $("button").id("scroll-to-start").click();

        int scrollTop = inputContainer.getPropertyInteger("scrollTop");
        Assert.assertEquals(0, scrollTop);
    }
}
