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

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.demo.ValueChangeModeButtonProvider;
import com.vaadin.flow.demo.ComponentDemoTest;

/**
 * Integration tests for the {@link PasswordField}.
 */
public class PasswordFieldIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-password-field";
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement passwordFieldValueDiv = layout
                .findElement(By.id("password-field-value"));
        WebElement passwordField = layout.findElement(
                By.id("password-field-with-value-change-listener"));

        updateValues(passwordFieldValueDiv, passwordField, true);
        layout.findElement(
                By.id(ValueChangeModeButtonProvider.TOGGLE_BUTTON_ID)).click();
        updateValues(passwordFieldValueDiv, passwordField, false);
    }

    private void updateValues(WebElement passwordFieldValueDiv,
            WebElement passwordField, boolean toggleBlur) {
        passwordField.sendKeys("a");
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Password field value changed from '' to 'a'",
                passwordFieldValueDiv);

        passwordField.sendKeys(Keys.BACK_SPACE);
        if (toggleBlur) {
            blur();
        }
        waitUntilTextsEqual("Password field value changed from 'a' to ''",
                passwordFieldValueDiv);
    }

    @Test
    public void passwordFieldHasPlaceholder() {
        WebElement passwordField = layout.findElement(
                By.id("password-field-with-value-change-listener"));
        Assert.assertEquals(passwordField.getAttribute("placeholder"),
                "placeholder text");
    }

    @Test
    public void disabledPasswordFieldNotUpdating() {
        WebElement passwordField = layout.findElement(By.id("disabled-password-field"));
        WebElement message = layout
                .findElement(By.id("disabled-password-field-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");", passwordField);
        passwordField.sendKeys("abc");
        blur();

        message = layout.findElement(By.id("disabled-password-field-message"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void assertVariants() {
        verifyThemeVariantsBeingToggled();
    }

    private void waitUntilTextsEqual(String expected, WebElement valueDiv) {
        waitUntil(driver -> expected.equals(valueDiv.getText()));
    }
}
