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
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.demo.ComponentDemoTest;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

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

        $(RadioButtonGroupElement.class).context(layout).first()
                .selectByText(EAGER.toString());
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

    @Test
    public void assertFocusShortcut(){
        PasswordFieldElement shortcutField = $(PasswordFieldElement.class).id("shortcut-field");
        Assert.assertNull("TextField should not be focused before the shortcut event is triggered.",
                shortcutField.getAttribute("focused"));

        sendKeys(Keys.ALT, "1");
        Assert.assertTrue("TextField should be focused after the shortcut event is triggered.",
                shortcutField.getAttribute("focused").equals("true")
                        || shortcutField.getAttribute("focused").equals(""));
    }

    private void sendKeys(CharSequence... keys) {
        new Actions(driver).sendKeys(keys).build().perform();
        /* if keys are not reset, alt will remain down and start flip-flopping */
        resetKeys();
    }

    private void resetKeys() {
        new Actions(driver).sendKeys(Keys.NULL).build().perform();
    }
}
