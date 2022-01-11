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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertFalse;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;

/**
 * Integration tests for {@link NumberField}.
 */
@TestPath("vaadin-text-field/number-field-test")
public class NumberFieldPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertDefaultMinMaxStepNotOverridden() {
        NumberFieldElement numberField = $(NumberFieldElement.class).first();
        Assert.assertNull(numberField.getProperty("min"));
        Assert.assertNull(numberField.getProperty("max"));
        Assert.assertEquals("1", numberField.getPropertyString("step"));
    }

    @Test
    public void assertReadOnly() {
        NumberFieldElement numberField = $(NumberFieldElement.class).first();
        WebElement messageDiv = $("div").id("message");
        numberField.setValue("123.0");

        assertFalse(numberField.hasAttribute("readonly"));

        WebElement readOnlyButton = findElement(By.id("read-only"));
        readOnlyButton.click();

        numberField.setValue("456");
        Assert.assertEquals("123", numberField.getValue());
        Assert.assertEquals("Old value: 'null'. New value: '123.0'.",
                messageDiv.getText());

        numberField.setProperty("readonly", "");
        numberField.setValue("789");
        Assert.assertEquals("123", numberField.getValue());
        Assert.assertEquals("Old value: 'null'. New value: '123.0'.",
                messageDiv.getText());

        readOnlyButton.click();
        numberField.setValue("987");
        Assert.assertEquals("Old value: '123.0'. New value: '987.0'.",
                messageDiv.getText());
    }

    @Test
    public void assertEnabled() {
        NumberFieldElement numberField = $(NumberFieldElement.class).first();
        WebElement messageDiv = $("div").id("message");
        numberField.setValue("123.0");

        assertFalse(numberField.hasAttribute("disabled"));
        WebElement disableEnableButton = findElement(By.id("disabled"));
        disableEnableButton.click();

        numberField.setValue("456");
        Assert.assertEquals("Old value: 'null'. New value: '123.0'.",
                messageDiv.getText());

        numberField.setProperty("disabled", "");
        numberField.setValue("789");
        Assert.assertEquals("Old value: 'null'. New value: '123.0'.",
                messageDiv.getText());

        disableEnableButton.click();
        numberField.setValue("987");
        Assert.assertEquals("Old value: '123.0'. New value: '987.0'.",
                messageDiv.getText());
    }

    @Test
    public void assertRequired() {
        NumberFieldElement numberField = $(NumberFieldElement.class).first();

        assertFalse(numberField.hasAttribute("required"));

        WebElement button = findElement(By.id("required"));
        button.click();
        waitUntil(attributeToBe(numberField, "required", "true"));

        button.click();
        waitUntil(attributeToBe(numberField, "required", ""));
    }

    @Test
    public void assertClearValue() {
        NumberFieldElement field = $(NumberFieldElement.class)
                .id("clear-number-field");

        WebElement input = field.$("input").first();
        input.sendKeys("300");
        blur();

        WebElement clearButton = field.$("*")
                .attributeContains("part", "clear-button").first();
        clearButton.click();

        String value = findElement(By.id("clear-message")).getText();
        Assert.assertEquals("Old value: '300.0'. New value: 'null'.", value);
    }

    @Test
    public void assertStepValue() {
        TestBenchElement field = $("*").id("step-number-field");

        WebElement increaseButton = field.$("*")
                .attributeContains("part", "increase-button").first();
        increaseButton.click();

        String value = findElement(By.id("step-message")).getText();
        Assert.assertEquals("Old value: 'null'. New value: '0.5'.", value);
    }

    @Test
    public void assertInvalidAfterClientChange() {
        final boolean valid = true;
        NumberFieldElement field = $(NumberFieldElement.class)
                .id("step-number-field");
        assertValidStateOfStepNumberField(valid);

        // max is 10
        field.setValue("11");
        assertValidStateOfStepNumberField(!valid);

        // Forcing max to 11 on the client does not make the field valid
        field.setProperty("max", "11");
        getCommandExecutor().waitForVaadin();
        assertValidStateOfStepNumberField(!valid);

        // Forcing the field to be valid does not work
        field.setProperty("invalid", false);
        getCommandExecutor().waitForVaadin();
        assertValidStateOfStepNumberField(!valid);

        // Setting a valid value makes the field return to valid mode
        field.setValue("10");
        getCommandExecutor().waitForVaadin();
        assertValidStateOfStepNumberField(valid);
    }

    private void assertValidStateOfStepNumberField(boolean valid) {
        final WebElement checkIsInvalid = findElement(
                By.id("check-is-invalid"));
        checkIsInvalid.click();

        final String expectedValue = !valid ? "invalid" : "valid";
        Assert.assertEquals(expectedValue,
                findElement(By.id("is-invalid")).getText());
    }

    @Test
    public void assertValueChange() {
        NumberFieldElement field = $(NumberFieldElement.class)
                .id("clear-number-field");
        field.setValue("123.0");
        String message = $("div").id("clear-message").getText();
        Assert.assertEquals("Old value: 'null'. New value: '123.0'.", message);
    }

    @Test
    public void dollarFieldHasDollarPrefix() {
        WebElement dollarField = findElement(By.id("dollar-field"));
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
        WebElement euroField = findElement(By.id("euro-field"));
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
