/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBinderPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBinderPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBinderPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.TextFieldValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-text-field/validation/binder")
public class TextFieldValidationBinderIT
        extends AbstractValidationIT<TextFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("Value", Keys.ENTER);

        testField.setValue("Value");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void minLength_changeInputValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("AAA", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("A");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("AA");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("AAA");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeInputValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("A", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("AAA");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("AA");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("A");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_changeInputValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1234", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("Word");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("12");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("1234");
        assertClientValid();
        assertServerValid();
    }

    protected TextFieldElement getTestField() {
        return $(TextFieldElement.class).first();
    }
}
