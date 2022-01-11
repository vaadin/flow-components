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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import static org.junit.Assert.assertFalse;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;

/**
 * Integration tests for {@link BigDecimalField}.
 */
@TestPath("vaadin-text-field/big-decimal-field-test")
public class BigDecimalFieldPageIT extends AbstractComponentIT {

    private BigDecimalFieldElement field;

    @Before
    public void init() {
        open();
        checkLogsForErrors();
        field = $(BigDecimalFieldElement.class).first();
    }

    @Test
    public void shouldHaveInputModeDecimal() {
        Assert.assertEquals("decimal",
                field.$("input").first().getAttribute("inputmode"));
    }

    @Test
    public void shouldHaveSameWidthAsNumberField() {
        // The width-property is set as 8em, but getComputedStyle returns the
        // width in pixels.
        String widthInEm = (String) executeScript(
                "const style = getComputedStyle(arguments[0]);"
                        + "const widthInPx = parseFloat(style.width);"
                        + "const fontSize = parseFloat(style.fontSize);"
                        + "return (widthInPx / fontSize) + 'em';",
                field);
        Assert.assertEquals("8em", widthInEm);
    }

    @Test
    public void enterValue_valueChanged() {
        field.setValue("123");
        assertValueChange(1, null, "123");
    }

    @Test
    public void enterValueWithHighPrecision_precisionPreserved() {
        // This can't be represented as a double
        String preciseNum = "1.9999999991111111111888888883333377777722222";
        field.setValue(preciseNum);
        assertValueChange(1, null, preciseNum);
    }

    @Test
    public void enterDecimalsWithoutChangingNumericalValue_valueChanged() {
        field.setValue("123");
        field.setValue("123.0");
        // These values are not equal BigDecimals, because they have
        // different scale-values.
        assertValueChange(2, "123", "123.0");
    }

    @Test
    public void enterInvalidValue_valueChangedToNull() {
        field.setValue("123");
        field.setValue("1.2.3");
        assertValueChange(2, "123", null);
    }

    @Test
    public void setValueWithScaleFromServer_scalePreserved() {
        clickElementWithJs("set-value-with-scale");
        Assert.assertEquals("1.200", field.getValue());
        assertValueChange(1, null, "1.200");
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
    public void assertRequired() {
        assertFalse(field.hasAttribute("required"));

        WebElement toggleRequired = findElement(By.id("toggle-required"));
        toggleRequired.click();
        waitUntil(attributeToBe(field, "required", "true"));

        toggleRequired.click();
        waitUntil(attributeToBe(field, "required", ""));
    }

    @Test
    public void assertClearValue() {
        field = $(BigDecimalFieldElement.class).id("clear-big-decimal-field");

        WebElement input = field.$("input").first();
        input.sendKeys("300");
        blur();

        TestBenchElement clearButton = field.$(TestBenchElement.class)
                .attributeContains("part", "clear-button").first();
        clearButton.click();

        assertValueChange(2, 300, null);
    }

    @Test
    public void setLocale_parsingAndFormattingUseLocalizedDecimalSeparator() {
        clickElementWithJs("set-french-locale");

        // Parsing value with comma from client to server
        field.setValue("5,5");
        assertValueChange(1, null, "5.5");

        // Formatting server-side value to have comma at client-side
        clickElementWithJs("set-value-with-scale");
        Assert.assertEquals("1,200", field.getValue());
        assertValueChange(2, "5.5", "1.200");
    }

    @Test
    public void setValue_setLocale_currentInputValueReformatted() {
        field.setValue("1.1");

        clickElementWithJs("set-french-locale");

        Assert.assertEquals("1,1", field.getValue());

        // shouldn't fire extra event when formatting the value
        assertValueChange(1, null, "1.1");
    }

    @Test
    public void setLocale_fieldAcceptsLocalizedDecimalSeparatorAsInput() {
        clickElementWithJs("set-french-locale");
        Assert.assertEquals(
                "BigDecimalField with French locale has unexpected pattern for "
                        + "invalid input prevention (the _enabledCharPattern property)",
                "[\\d-+,]", field.getPropertyString("_enabledCharPattern"));
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
