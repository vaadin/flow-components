/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration tests for {@link TextArea}.
 */
@TestPath("vaadin-text-field/text-area-pattern")
public class TextAreaPatternIT extends AbstractComponentIT {

    private TextAreaElement textArea;
    private TestBenchElement currentValue;
    private TestBenchElement setNumberPattern;
    private TestBenchElement setInvalidPattern;
    private TestBenchElement clearPattern;
    private TestBenchElement checkValidity;
    private TestBenchElement validityOutput;
    private TestBenchElement enablePreventInvalidInput;

    @Before
    public void init() {
        open();
        textArea = $(TextAreaElement.class).first();
        currentValue = $("div").id("current-value");
        setNumberPattern = $("button").id("set-number-pattern");
        setInvalidPattern = $("button").id("set-invalid-pattern");
        clearPattern = $("button").id("clear-pattern");
        enablePreventInvalidInput = $("button")
                .id("enable-prevent-invalid-input");
        // These two are defined by TextFieldTestPageUtil.addInvalidCheck
        checkValidity = $("button").id("check-is-invalid");
        validityOutput = $("div").id("is-invalid");
    }

    private void assertClientSideValid() {
        Assert.assertFalse(textArea.hasAttribute("invalid"));
    }

    private void assertClientSideInvalid() {
        Assert.assertTrue(textArea.hasAttribute("invalid"));
    }

    private void assertServerSideValid() {
        checkValidity.click();
        Assert.assertEquals("valid", validityOutput.getText());
    }

    private void assertServerSideInvalid() {
        checkValidity.click();
        Assert.assertEquals("invalid", validityOutput.getText());
    }

    @Test
    public void validPattern_validInput_isValid() {
        setNumberPattern.click();
        textArea.sendKeys("1234");
        blur();

        assertClientSideValid();
        assertServerSideValid();
    }

    @Test
    public void validPattern_invalidInput_isInvalid() {
        setNumberPattern.click();
        textArea.sendKeys("abcd");
        blur();

        assertClientSideInvalid();
        assertServerSideInvalid();
    }

    @Test
    public void validPattern_preventInvalidInput_invalidInput_inputShouldBePrevented() {
        setNumberPattern.click();
        enablePreventInvalidInput.click();
        textArea.sendKeys("abcd1234");
        blur();

        assertClientSideValid();
        assertServerSideValid();
        Assert.assertEquals("1234", currentValue.getText());
    }

    @Test
    public void invalidPattern_anyInput_isValid() {
        setInvalidPattern.click();
        textArea.sendKeys("abcd");
        blur();

        assertClientSideValid();
        assertServerSideValid();
    }

    @Test
    @Ignore("Missing logic, see https://github.com/vaadin/flow-components/issues/2370")
    public void validPattern_invalidInput_clearPattern_isValid() {
        setNumberPattern.click();
        textArea.sendKeys("abcd");
        blur();
        clearPattern.click();

        assertClientSideValid();
        assertServerSideValid();
    }
}
