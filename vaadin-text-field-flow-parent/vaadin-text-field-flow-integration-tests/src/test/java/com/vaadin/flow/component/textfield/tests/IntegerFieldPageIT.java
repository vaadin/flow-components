/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for {@link IntegerField}.
 */
@TestPath("vaadin-text-field/integer-field-test")
public class IntegerFieldPageIT extends AbstractComponentIT {

    private IntegerFieldElement field;

    @Before
    public void init() {
        open();
        field = $(IntegerFieldElement.class).first();
    }

    @Test
    public void assertDefaultMinMaxStepNotOverridden() {
        Assert.assertNull(field.getProperty("min"));
        Assert.assertNull(field.getProperty("max"));
        Assert.assertEquals(null, field.getPropertyString("step"));
    }

    @Test
    public void assertValueChange() {
        field.setValue("123");
        assertValueChange(1, null, 123);
    }

    @Test
    public void assertReadOnly() {
        field.setValue("123");

        assertFalse(field.hasAttribute("readonly"));

        WebElement toggleReadOnly = findElement(By.id("toggle-read-only"));
        toggleReadOnly.click();

        field.setValue("456");
        Assert.assertEquals("123", field.getValue());

        assertValueChange(1, null, 123);

        field.setProperty("readonly", "");
        field.setValue("789");
        Assert.assertEquals("123", field.getValue());
        assertValueChange(1, null, 123);

        toggleReadOnly.click();
        field.setValue("987");
        assertValueChange(2, 123, 987);
    }

    @Test
    public void assertEnabled() {
        field.setValue("123");

        assertFalse(field.hasAttribute("disabled"));
        WebElement toggleEnabled = findElement(By.id("toggle-enabled"));
        toggleEnabled.click();

        field.setValue("456");
        assertValueChange(1, null, 123);

        field.setProperty("disabled", "");
        field.setValue("789");
        assertValueChange(1, null, 123);

        toggleEnabled.click();
        field.setValue("987");
        assertValueChange(2, 123, 987);
    }

    @Test
    public void assertClearValue() {
        field = $(IntegerFieldElement.class).id("clear-integer-field");

        WebElement input = field.$("input").first();
        input.sendKeys("300");
        blur();

        field.clickClearButton();

        assertValueChange(2, 300, null);
    }

    @Test
    public void assertStepValue() {
        field = $(IntegerFieldElement.class).id("step-integer-field");

        TestBenchElement increaseButton = field.$(TestBenchElement.class)
                .withAttributeContainingWord("part", "increase-button").first();

        increaseButton.click();
        assertValueChange(1, null, 4);

        increaseButton.click();
        assertValueChange(2, 4, 7);
    }

    @Test
    public void changeMaxInBrowser_oldMaxUsedInValidationAtServer() {
        field = $(IntegerFieldElement.class).id("step-integer-field");
        assertValidState(true);

        // max is 10
        field.setValue("11");
        assertValidState(false);

        // Forcing max to 11 on the client does not make the field valid
        field.setProperty("max", "11");
        getCommandExecutor().waitForVaadin();
        assertValidState(false);

        // Forcing the field to be valid does not work
        field.setProperty("invalid", false);
        getCommandExecutor().waitForVaadin();
        assertValidState(false);
    }

    @Test
    public void changeMinInBrowser_oldMinUsedInValidationAtServer() {
        field = $(IntegerFieldElement.class).id("step-integer-field");
        assertValidState(true);

        // min is 4
        field.setValue("3");
        assertValidState(false);

        // Forcing min to 0 on the client does not make the field valid
        executeScript("arguments[0].min = 0", field);
        getCommandExecutor().waitForVaadin();
        assertValidState(false);

        // Forcing the field to be valid does not work
        field.setProperty("invalid", false);
        getCommandExecutor().waitForVaadin();
        assertValidState(false);
    }

    private void assertValidState(boolean valid) {
        findElement(By.id("check-is-invalid")).click();

        final String expectedValue = !valid ? "invalid" : "valid";
        Assert.assertEquals(expectedValue,
                findElement(By.id("is-invalid")).getText());
    }

    @Test
    public void integerOverflow_noException_valueSetToNull() {
        // max int
        field.setValue("2147483647");
        assertValueChange(1, null, "2147483647");

        // increase by one to overflow
        field.sendKeys(Keys.BACK_SPACE + "8" + Keys.ENTER);
        assertValueChange(2, "2147483647", null);

        // min int
        field.setValue("-2147483648");
        assertValueChange(3, null, "-2147483648");

        // decrease by one to overflow
        field.sendKeys(Keys.BACK_SPACE + "9" + Keys.ENTER);
        assertValueChange(4, "-2147483648", null);
    }

    // Always checking the count of fired events to make sure it doesn't fire
    // duplicates or extra value-changes in any scenario
    private void assertValueChange(int expectedCount, Object expectedOldValue,
            Object expectedValue) {
        List<TestBenchElement> messages = $("div").id("messages").$("p").all();
        Assert.assertEquals("Unexpected amount of value-change events fired",
                expectedCount, messages.size());
        Assert.assertEquals(
                String.format("Old value: '%s'. New value: '%s'.",
                        expectedOldValue, expectedValue),
                messages.get(messages.size() - 1).getText());
    }
}
