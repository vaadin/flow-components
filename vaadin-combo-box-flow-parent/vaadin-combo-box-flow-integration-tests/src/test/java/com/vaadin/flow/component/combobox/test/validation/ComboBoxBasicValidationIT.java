/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.validation;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBasicValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBasicValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBasicValidationPage.ENABLE_CUSTOM_VALUE_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBasicValidationPage.REQUIRED_BUTTON;

@TestPath("vaadin-combo-box/validation/basic")
public class ComboBoxBasicValidationIT
        extends AbstractValidationIT<ComboBoxElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("foo");
        assertServerValid();
        assertClientValid();

        testField.clear();
        assertServerInvalid();
        assertClientInvalid();

        // Try enter custom value
        testField.sendKeys("custom", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_customValuesAllowed_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();
        $("button").id(ENABLE_CUSTOM_VALUE_BUTTON).click();

        testField.sendKeys("custom", Keys.TAB);
        assertServerValid();
        assertClientValid();

        testField.clear();
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("foo");
        testField.clear();

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("foo");
        testField.clear();

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected ComboBoxElement getTestField() {
        return $(ComboBoxElement.class).first();
    }

    protected void detachAndReattachField() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        // Verify element has been removed
        waitUntil(ExpectedConditions.stalenessOf(testField));

        $("button").id(ATTACH_FIELD_BUTTON).click();
        // Retrieve new element instance
        testField = getTestField();
    }
}
