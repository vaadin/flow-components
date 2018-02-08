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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.demo.ValueChangeModeButtonProvider;
import com.vaadin.flow.demo.ComponentDemoTest;

/**
 * Integration tests for the {@link TextField}.
 */
public class TextFieldIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-text-field";
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement textFieldValueDiv = layout
                .findElement(By.id("text-field-value"));
        WebElement textField = layout
                .findElement(By.id("text-field-with-value-change-listener"));

        updateValues(textFieldValueDiv, textField, true);
        layout.findElement(
                By.id(ValueChangeModeButtonProvider.TOGGLE_BUTTON_ID)).click();
        updateValues(textFieldValueDiv, textField, false);
    }

    private void updateValues(WebElement textFieldValueDiv,
            WebElement textField, boolean toggleBlur) {
        textField.sendKeys("a");
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text field value changed from '' to 'a'",
                textFieldValueDiv.getText());

        textField.sendKeys(Keys.BACK_SPACE);
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Text field value changed from 'a' to ''",
                textFieldValueDiv.getText());
    }

    @Test
    public void textFieldHasPlaceholder() {
        WebElement textField = layout
                .findElement(By.id("text-field-with-value-change-listener"));
        Assert.assertEquals(textField.getAttribute("placeholder"),
                "placeholder text");
    }

    private void waitUntilTextsEqual(String expected, String actual) {
        waitUntil(driver -> expected.equals(actual));
    }

    @Test
    public void dollarFieldHasDollarPrefix() {
        WebElement dollarField = layout.findElement(By.id("dollar-field"));
        WebElement span = dollarField.findElement(By.tagName("span"));

        Assert.assertEquals("$", span.getText());

        int spanX = span.getLocation().getX();
        int middleX = dollarField.getLocation().getX()
                + dollarField.getSize().getWidth() / 2;

        Assert.assertTrue(
                "The dollar sign should be located on the left side of the text field",
                spanX < middleX);
    }

    @Test
    public void euroFieldHasEuroSuffix() {
        WebElement euroField = layout.findElement(By.id("euro-field"));
        WebElement span = euroField.findElement(By.tagName("span"));

        Assert.assertEquals("€", span.getText());

        int spanX = span.getLocation().getX();
        int middleX = euroField.getLocation().getX()
                + euroField.getSize().getWidth() / 2;

        Assert.assertTrue(
                "The euro sign should be located on the right side of the text field",
                spanX > middleX);
    }
}
