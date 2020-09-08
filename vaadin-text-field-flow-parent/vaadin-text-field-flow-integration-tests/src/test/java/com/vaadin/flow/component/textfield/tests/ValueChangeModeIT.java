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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.tests.AbstractComponentIT;
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
    public void testValueChangeModesForTextField() throws InterruptedException {
        testValueChangeModes(textField, "textfield");
    }

    @Test
    public void testValueChangeModesForTextArea() throws InterruptedException {
        testValueChangeModes(textArea, "textarea");
    }

    @Test
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
            assertMessageUpdated(
                    "By default the value change events should be sent when clicking enter (ValueChangeMode should be ON_CHANGE)");
        }

        field.sendKeys("1");
        assertMessageNotUpdated(
                "By default the value change events should not be sent on every key stroke (ValueChangeMode should be ON_CHANGE)");
        blur();
        assertMessageUpdated("The value change events should be sent on blur");

        clickButton(componentName + "-on-blur");

        field.sendKeys("1");
        assertMessageNotUpdated(
                "The value change events should not be sent on every key stroke when using ValueChangeMode.ON_BLUR");

        field.sendKeys(Keys.ENTER);
        assertMessageNotUpdated(
                "The value change events should not be sent with enter key when using ValueChangeMode.ON_BLUR");

        blur();
        assertMessageUpdated(
                "The value change events should be sent on blur when using ValueChangeMode.ON_BLUR");

        clickButton(componentName + "-eager");

        field.sendKeys("1");
        assertMessageUpdated(
                "The value change events should be sent on every key stroke when using ValueChangeMode.EAGER");
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
        clickButton(componentName + "-lazy");
        field.sendKeys("1");
        assertMessageNotUpdated(
                "The value change event should not be sent on first key stroke when using ValueChangeMode.LAZY");

        for (int i = 0; i < 2; i++) {
            field.sendKeys("1");
            Thread.sleep(600);
            assertMessageNotUpdated(
                    "The value change event should not be sent until timeout elapsed since last keystroke when using ValueChangeMode.LAZY");
        }

        waitUntilMessageUpdated(400,
                "The value change event should be sent when timeout elapsed since last keystroke when using ValueChangeMode.LAZY");

        clickButton(componentName + "-timeout");
        field.sendKeys("1");
        assertMessageUpdated(
                "The value change event should be sent on first key stroke when using ValueChangeMode.TIMEOUT");

        field.sendKeys("1");
        Thread.sleep(600);
        assertMessageNotUpdated(
                "The value change event should not be sent until timeout elapsed since last event when using ValueChangeMode.TIMEOUT");

        waitUntilMessageUpdated(400,
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

    private void waitUntilMessageUpdated(long timeout, String failMessage) {
        new WebDriverWait(getDriver(), timeout).withMessage(failMessage)
                .until(webDriver -> isMessageUpdated());
    }
}
