/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.testbench.TestBenchElement;
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
        TestBenchElement field = $("*").id("clear-password-field");

        WebElement input = field.$("input").first();
        input.sendKeys("foo");
        blur();

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
