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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;

import com.vaadin.tests.AbstractComponentIT;

import static org.junit.Assert.assertTrue;

import com.vaadin.flow.testutil.TestPath;

import java.time.Duration;

/**
 * Integration tests for changing the ValueChangeMode of TextField, TextArea and
 * PasswordField.
 */
@TestPath("vaadin-text-field/value-change-mode-test")
public class ValueChangeModeIT extends AbstractComponentIT {

    private WebElement message;
    private String lastMessageText = "";

    private TextFieldElement textField;
    private TextAreaElement textArea;
    private PasswordFieldElement passwordField;
    private EmailFieldElement emailField;
    private NumberFieldElement numberField;
    private IntegerFieldElement integerField;
    private BigDecimalFieldElement bigDecimalField;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("message"));
        message = findElement(By.id("message"));
        textField = $(TextFieldElement.class).first();
        textArea = $(TextAreaElement.class).first();
        passwordField = $(PasswordFieldElement.class).first();
        emailField = $(EmailFieldElement.class).first();
        numberField = $(NumberFieldElement.class).first();
        integerField = $(IntegerFieldElement.class).first();
        bigDecimalField = $(BigDecimalFieldElement.class).first();
    }

    @Test
    public void testValueChangeModesForTextField() throws InterruptedException {
        testValueChangeModes(textField.$("input").first(), "textfield");
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
    public void testValueChangeModesForEmailField()
            throws InterruptedException {
        testValueChangeModes(emailField, "emailfield");
    }

    @Test
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
    public void testValueChangeModesForBigDecimalField()
            throws InterruptedException {
        testValueChangeModes(bigDecimalField.$("input").first(),
                "bigdecimalfield");
    }

    private void testValueChangeModes(TestBenchElement field,
            String componentName) throws InterruptedException {
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
        waitUntilMessageUpdated(2,
                "It took more than 2s to change the message, probably CI performance problems");
    }

    private void waitUntilMessageUpdated(long timeoutInSeconds,
            String failMessage) {
        new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutInSeconds))
                .withMessage(failMessage)
                .until(webDriver -> isMessageUpdated());
    }
}
