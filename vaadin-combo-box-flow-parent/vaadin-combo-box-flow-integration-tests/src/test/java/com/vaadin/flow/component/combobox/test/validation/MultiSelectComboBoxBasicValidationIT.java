/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.validation;

import static com.vaadin.flow.component.combobox.test.validation.MultiSelectComboBoxBasicValidationPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.MultiSelectComboBoxBasicValidationPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.MultiSelectComboBoxBasicValidationPage.ENABLE_CUSTOM_VALUE_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.MultiSelectComboBoxBasicValidationPage.REQUIRED_BUTTON;

import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-multi-select-combo-box/validation/basic")
public class MultiSelectComboBoxBasicValidationIT
        extends AbstractValidationIT<MultiSelectComboBoxElement> {

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

        testField.deselectAll();
        assertServerInvalid();
        assertClientInvalid();

        // Try enter custom value
        testField.sendKeys("custom", Keys.ENTER);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_customValuesAllowed_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();
        $("button").id(ENABLE_CUSTOM_VALUE_BUTTON).click();

        testField.sendKeys("custom", Keys.ENTER);
        assertServerValid();
        assertClientValid();

        testField.deselectAll();
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("foo");
        testField.deselectAll();

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("foo");
        testField.deselectAll();

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected MultiSelectComboBoxElement getTestField() {
        return $(MultiSelectComboBoxElement.class).first();
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
