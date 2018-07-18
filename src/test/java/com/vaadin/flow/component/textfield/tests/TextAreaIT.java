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
package com.vaadin.flow.component.textfield.tests;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.demo.ValueChangeModeButtonProvider;
import com.vaadin.flow.demo.ComponentDemoTest;

/**
 * Integration tests for the {@link TextArea}.
 */
public class TextAreaIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-text-area";
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement textFieldValueDiv = layout
                .findElement(By.id("text-area-value"));
        WebElement textArea = layout
                .findElement(By.id("text-area-with-value-change-listener"));

        updateValues(textFieldValueDiv, textArea, true);
        layout.findElement(
                By.id(ValueChangeModeButtonProvider.TOGGLE_BUTTON_ID)).click();
        updateValues(textFieldValueDiv, textArea, false);
    }

    private void updateValues(WebElement textFieldValueDiv, WebElement textArea,
            boolean toggleBlur) {
        textArea.sendKeys("a");
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text area value changed from '' to 'a'",
                textFieldValueDiv.getText());

        textArea.sendKeys(Keys.BACK_SPACE);
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text area value changed from 'a' to ''",
                textFieldValueDiv.getText());
    }

    @Test
    public void maxHeight() {
        WebElement textArea = layout
                .findElement(By.id("text-area-with-max-height"));

        IntStream.range(0, 20).forEach(i -> textArea.sendKeys("foobarbaz\n"));

        Assert.assertTrue(textArea.getSize().getHeight() <= 125);
    }

    @Test
    public void minHeight() throws InterruptedException {
        WebElement textArea = layout
                .findElement(By.id("text-area-with-min-height"));

        IntStream.range(0, 20).forEach(i -> textArea.sendKeys("foobarbaz\n"));

        Assert.assertTrue(textArea.getSize().getHeight() >= 125);

        IntStream.range(0, 20 * "foobarbaz\n".length())
                .forEach(i -> textArea.sendKeys(Keys.BACK_SPACE));

        Assert.assertEquals(125, textArea.getSize().getHeight());
    }

    @Test
    public void textAreaHasPlaceholder() {
        WebElement textField = layout
                .findElement(By.id("text-area-with-value-change-listener"));
        Assert.assertEquals(textField.getAttribute("placeholder"),
                "placeholder text");
    }

    @Test
    public void disabledTextAreaNotUpdating() {
        WebElement textArea = layout.findElement(By.id("disabled-text-area"));
        WebElement message = layout
                .findElement(By.id("disabled-text-area-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");", textArea);
        textArea.sendKeys("abc");
        blur();
        
        message = layout.findElement(By.id("disabled-text-area-message"));
        Assert.assertEquals("", message.getText());
    }

    private void waitUntilTextsEqual(String expected, String actual) {
        waitUntil(driver -> expected.equals(actual));
    }

    @Test
    public void assertVariants() {
        verifyThemeVariantsBeingToggled();
    }
}
