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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

/**
 * Integration tests for {@link PasswordField}.
 */
@TestPath("vaadin-text-field/password-field-test")
public class PasswordFieldPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertReadOnly() {
        WebElement webComponent = findElement(
                By.tagName("vaadin-password-field"));
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
        WebElement webComponent = findElement(
                By.tagName("vaadin-password-field"));

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
    public void assertClearValue() {
        PasswordFieldElement field = $(PasswordFieldElement.class)
                .id("clear-password-field");
        field.setValue("foo");

        WebElement clearButton = field.$("*")
                .attributeContains("part", "clear-button").first();
        clearButton.click();

        String value = findElement(By.id("clear-message")).getText();
        Assert.assertEquals("Old value: 'foo'. New value: ''.", value);
    }

    @Test
    public void disabledPasswordFieldNotUpdating() {
        WebElement passwordField = findElement(
                By.id("disabled-password-field"));
        WebElement message = findElement(
                By.id("disabled-password-field-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");",
                passwordField);
        passwordField.sendKeys("abc");
        blur();

        message = findElement(By.id("disabled-password-field-message"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void assertFocusShortcut() {
        PasswordFieldElement shortcutField = $(PasswordFieldElement.class)
                .id("shortcut-field");
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
    public void passwordFieldHasPlaceholder() {
        WebElement passwordField = findElement(
                By.id("password-field-with-value-change-listener"));
        Assert.assertEquals(passwordField.getAttribute("placeholder"),
                "placeholder text");
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement passwordFieldValueDiv = findElement(
                By.id("password-field-value"));
        WebElement passwordField = findElement(
                By.id("password-field-with-value-change-listener"));

        updateValues(passwordFieldValueDiv, passwordField, true);

        $(RadioButtonGroupElement.class).first().selectByText(EAGER.toString());
        updateValues(passwordFieldValueDiv, passwordField, false);
    }

    @Test
    public void assertCantMakeInvalidValueValidThroughClientManipulation() {
        ValidationTestHelper.testValidation(getCommandExecutor(), getContext(),
                $(PasswordFieldElement.class).id("invalid-test-field"));
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

    private void waitUntilTextsEqual(String expected, WebElement valueDiv) {
        waitUntil(driver -> expected.equals(valueDiv.getText()));
    }
}
