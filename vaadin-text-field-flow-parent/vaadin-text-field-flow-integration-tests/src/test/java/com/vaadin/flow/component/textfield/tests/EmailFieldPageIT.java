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

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;

/**
 * Integration tests for {@link EmailField}.
 */
@TestPath("vaadin-text-field/email-field-test")
public class EmailFieldPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertReadOnly() {
        EmailFieldElement emailField = $(EmailFieldElement.class).first();
        WebElement messageDiv = $("div").id("message");
        emailField.setValue("mail@domain.com");

        assertFalse(emailField.hasAttribute("readonly"));

        WebElement readOnlyButton = findElement(By.id("read-only"));
        readOnlyButton.click();

        emailField.setValue("another@domain.com");
        Assert.assertEquals("mail@domain.com", emailField.getValue());
        Assert.assertEquals("Old value: ''. New value: 'mail@domain.com'.",
                messageDiv.getText());

        emailField.setProperty("readonly", "");
        emailField.setValue("another@domain.com");
        Assert.assertEquals("mail@domain.com", emailField.getValue());
        Assert.assertEquals("Old value: ''. New value: 'mail@domain.com'.",
                messageDiv.getText());

        readOnlyButton.click();
        emailField.setValue("yetanother@domain.com");
        Assert.assertEquals(
                "Old value: 'mail@domain.com'. New value: 'yetanother@domain.com'.",
                messageDiv.getText());
    }

    @Test
    public void assertEnabled() {
        EmailFieldElement emailField = $(EmailFieldElement.class).first();
        WebElement messageDiv = $("div").id("message");
        emailField.setValue("mail@domain.com");

        assertFalse(emailField.hasAttribute("disabled"));
        WebElement disableEnableButton = findElement(By.id("disabled"));
        disableEnableButton.click();

        emailField.setValue("another@domain.com");
        Assert.assertEquals("Old value: ''. New value: 'mail@domain.com'.",
                messageDiv.getText());

        emailField.setProperty("disabled", "");
        emailField.setValue("another@domain.com");
        Assert.assertEquals("Old value: ''. New value: 'mail@domain.com'.",
                messageDiv.getText());

        disableEnableButton.click();
        emailField.setValue("yetanother@domain.com");
        Assert.assertEquals(
                "Old value: 'mail@domain.com'. New value: 'yetanother@domain.com'.",
                messageDiv.getText());
    }

    @Test
    public void assertRequired() {
        EmailFieldElement emailField = $(EmailFieldElement.class).first();

        assertFalse(emailField.hasAttribute("required"));

        WebElement button = findElement(By.id("required"));
        button.click();
        waitUntil(attributeToBe(emailField, "required", "true"));

        button.click();
        waitUntil(attributeToBe(emailField, "required", ""));
    }

    @Test
    public void assertClearValue() {
        EmailFieldElement field = $(EmailFieldElement.class)
                .id("clear-email-field");

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
    public void assertInvalidValue() {
        EmailFieldElement field = $(EmailFieldElement.class)
                .id("clear-email-field");

        field.sendKeys("username");
        blur();
        assertTrue(field.hasAttribute("invalid"));

        field.sendKeys("username@domain.com");
        blur();
        assertFalse(field.hasAttribute("invalid"));
    }

    @Test
    public void assertValueChange() {
        EmailFieldElement field = $(EmailFieldElement.class)
                .id("clear-email-field");
        field.setValue("account@domain.com");
        String message = $("div").id("clear-message").getText();
        Assert.assertEquals("Old value: ''. New value: 'account@domain.com'.",
                message);
    }
}
