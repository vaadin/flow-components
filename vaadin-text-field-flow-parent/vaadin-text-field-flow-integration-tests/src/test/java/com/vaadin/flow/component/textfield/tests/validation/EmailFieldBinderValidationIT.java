/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBinderValidationPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBinderValidationPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBinderValidationPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldBinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-email-field/validation/binder")
public class EmailFieldBinderValidationIT
        extends AbstractValidationIT<EmailFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("john@vaadin.com",
                Keys.ENTER);

        testField.setValue("john@vaadin.com");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void minLength_changeValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("13", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("aaa@vaadin.com",
                Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("a@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("aa@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("aaa@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("13", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("a@vaadin.com",
                Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("aaa@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("aa@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("a@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void defaultPattern_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("john@vaadin.com",
                Keys.ENTER);

        testField.setValue("arbitrary string");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        testField.setValue("john@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_changeValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^[^\\d]+@vaadin.com$",
                Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("john@vaadin.com",
                Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("2222@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("oliver@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("john@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    protected EmailFieldElement getTestField() {
        return $(EmailFieldElement.class).first();
    }
}
