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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.stream.IntStream;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

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

        WebElement clearButton = field.$("*")
                .attributeContains("part", "clear-button").first();
        clearButton.click();

        String value = findElement(By.id("clear-message")).getText();
        Assert.assertEquals("Old value: 'foo'. New value: ''.", value);
    }

    @Test
    public void assertFocusShortcut() {
        TextAreaElement shortcutField = $(TextAreaElement.class)
                .id("shortcut-field");
        Assert.assertNull(
                "TextArea should not be focused before the shortcut event is triggered.",
                shortcutField.getAttribute("focused"));

        SendKeysHelper.sendKeys(driver, Keys.ALT, "1");
        Assert.assertTrue(
                "TextArea should be focused after the shortcut event is triggered.",
                shortcutField.getAttribute("focused").equals("true")
                        || shortcutField.getAttribute("focused").equals(""));
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
    public void textAreaHasPlaceholder() {
        WebElement textField = findElement(
                By.id("text-area-with-value-change-listener"));
        Assert.assertEquals(textField.getAttribute("placeholder"),
                "placeholder text");
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
    public void assertCantMakeInvalidValueValidThroughClientManipulation() {
        ValidationTestHelper.testValidation(getCommandExecutor(), getContext(),
                $(TextAreaElement.class).id("invalid-test-field"));
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
                textAreaElement.getHelperComponent().getAttribute("id"));
    }

    @Test
    public void clearHelperComponent() {
        TextAreaElement textAreaElement = $(TextAreaElement.class)
                .id("helper-component-field");
        Assert.assertEquals("helper-component",
                textAreaElement.getHelperComponent().getAttribute("id"));

        $(TestBenchElement.class).id("clear-helper-component-button").click();
        Assert.assertNull(textAreaElement.getHelperComponent());
    }

}
