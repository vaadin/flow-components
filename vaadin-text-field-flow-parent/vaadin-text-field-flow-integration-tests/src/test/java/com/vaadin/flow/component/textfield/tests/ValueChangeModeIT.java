/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.tests.AbstractComponentIT;

import static org.junit.Assert.assertTrue;

import com.vaadin.flow.testutil.TestPath;

/**
 * Integration tests for changing the ValueChangeMode of TextField, TextArea and
 * PasswordField.
 */
@TestPath("vaadin-text-field/value-change-mode-test")
public class ValueChangeModeIT extends AbstractComponentIT {

    private WebElement message;
    private String lastMessageText = "";

    private WebElement textField;
    private WebElement textArea;
    private WebElement passwordField;
    private WebElement emailField;
    private WebElement numberField;
    private WebElement integerField;
    private WebElement bigDecimalField;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("message"));
        message = findElement(By.id("message"));
        textField = findElement(By.tagName("vaadin-text-field"));
        textArea = findElement(By.tagName("vaadin-text-area"));
        passwordField = findElement(By.tagName("vaadin-password-field"));
        emailField = findElement(By.tagName("vaadin-email-field"));
        numberField = findElement(By.tagName("vaadin-number-field"));
        integerField = findElement(By.tagName("vaadin-integer-field"));
        bigDecimalField = findElement(By.tagName("vaadin-big-decimal-field"));
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForTextField() throws InterruptedException {
        testValueChangeModes(textField, "textfield");
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForTextArea() throws InterruptedException {
        testValueChangeModes(textArea, "textarea");
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForPasswordField()
            throws InterruptedException {
        testValueChangeModes(passwordField, "passwordfield");
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForEmailField()
            throws InterruptedException {
        testValueChangeModes(emailField, "emailfield");
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForNumberField()
            throws InterruptedException {
        testValueChangeModes(numberField, "numberfield");
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForIntegerField()
            throws InterruptedException {
        testValueChangeModes(integerField, "integerfield");
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void testValueChangeModesForBigDecimalField()
            throws InterruptedException {
        testValueChangeModes(bigDecimalField, "bigdecimalfield");
    }

    private void testValueChangeModes(WebElement field, String componentName)
            throws InterruptedException {

        field.sendKeys("1");
        assertMessageNotUpdated(
                "By default the value change events should not be sent on every key stroke (ValueChangeMode should be ON_CHANGE)");

        if (field != textArea) {
            // Clicking enter on TextArea makes a line-break instead of
            // "committing" the change and firing a change-event.
            field.sendKeys(Keys.ENTER);
            waitUntilMessageUpdated();
        }

        field.sendKeys("1");
        assertMessageNotUpdated(
                "By default the value change events should not be sent on every key stroke (ValueChangeMode should be ON_CHANGE)");
        blur();
        waitUntilMessageUpdated();

        clickButton(componentName + "-on-blur");

        field.sendKeys("1");
        assertMessageNotUpdated(
                "The value change events should not be sent on every key stroke when using ValueChangeMode.ON_BLUR");

        field.sendKeys(Keys.ENTER);
        assertMessageNotUpdated(
                "The value change events should not be sent with enter key when using ValueChangeMode.ON_BLUR");

        blur();
        waitUntilMessageUpdated();

        clickButton(componentName + "-eager");
        field.sendKeys("1");

        waitUntilMessageUpdated();

        blur();
        assertMessageNotUpdated(
                "The value change event should not be sent again on blur, because it was already sent eagerly when typing");

        WebElement changeTimeoutField = findElement(
                By.id(componentName + "-set-change-timeout"));
        changeTimeoutField.sendKeys("1000");
        blur();

        testValueChangeTimeout(field, componentName);
    }

    private void testValueChangeTimeout(WebElement field, String componentName)
            throws InterruptedException {
        long last = System.currentTimeMillis();

        clickButton(componentName + "-lazy");
        field.sendKeys("1");
        assertMessageNotUpdated(
                "The value change event should not be sent on first key stroke when using ValueChangeMode.LAZY");
        waitUntilMessageUpdated();
        assertTrue(-last + (last = System.currentTimeMillis()) > 1000);

        for (int i = 0; i < 2; i++) {
            field.sendKeys("1");
            assertMessageNotUpdated(
                    "The value change event should not be sent until timeout elapsed since last keystroke when using ValueChangeMode.LAZY");
        }

        waitUntilMessageUpdated();
        assertTrue(
                "The value change event should be sent when timeout elapsed since last keystroke when using ValueChangeMode.LAZY",
                -last + (last = System.currentTimeMillis()) > 1000);

        clickButton(componentName + "-timeout");
        field.sendKeys("1");
        waitUntilMessageUpdated();

        field.sendKeys("1");
        assertMessageNotUpdated(
                "The value change event should be sent when timeout elapsed since last event when using ValueChangeMode.TIMEOUT");
    }

    private void clickButton(String buttonId) {
        findElement(By.id(buttonId)).click();
    }

    private void assertMessageUpdated(String failMessage) {
        Assert.assertTrue(failMessage, isMessageUpdated());
    }

    private void assertMessageNotUpdated(String failMessage) {
        Assert.assertFalse(failMessage, isMessageUpdated());
    }

    private boolean isMessageUpdated() {
        String messageText = message.getText();
        boolean isUpdated = !message.getText().equals(lastMessageText);
        lastMessageText = messageText;
        return isUpdated;
    }

    private void waitUntilMessageUpdated() {
        waitUntilMessageUpdated(2000,
                "It took more than 2000ms to change the message, probably CI performance problems");
    }

    private void waitUntilMessageUpdated(long timeout, String failMessage) {
        new WebDriverWait(getDriver(), timeout).withMessage(failMessage)
                .until(webDriver -> isMessageUpdated());
    }
}
