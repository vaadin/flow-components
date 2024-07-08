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

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.STEP_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-integer-field/validation/binder")
public class IntegerFieldBinderValidationIT
        extends AbstractValidationIT<IntegerFieldElement> {
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
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1234", Keys.ENTER);

        testField.setValue("1234");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("3", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("1");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("2");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("3");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("3");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("2");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("1");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void step_changeValue_assertValidity() {
        $("input").id(STEP_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("4", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("1");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("2");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("4");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2", Keys.ENTER);

        testField.sendKeys("--2", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        testField.setValue("2");
        assertServerValid();
        assertClientValid();

        testField.sendKeys("--2", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.sendKeys("--2", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void integerOverflow_setValueExceedingMaxInteger_assertValidity() {
        testField.sendKeys("999999999999", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    @Test
    public void integerOverflow_setValueExceedingMinInteger_assertValidity() {
        testField.sendKeys("-999999999999", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    protected IntegerFieldElement getTestField() {
        return $(IntegerFieldElement.class).first();
    }
}
