/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests.validation;

import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.BAD_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.MAX_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.MIN_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.RESET_BEAN_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.STEP_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.STEP_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldBinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-number-field/validation/binder")
public class NumberFieldBinderValidationIT
        extends AbstractValidationIT<NumberFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1234", Keys.ENTER);

        testField.setValue("1234");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);

        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_setValue_resetBean_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1234", Keys.ENTER);

        testField.setValue("1234");
        assertServerValid();
        assertClientValid();

        $("button").id(RESET_BEAN_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2.2", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("1.8");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setValue("2");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("2.2");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        // Binder validation fails:
        testField.setValue("");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1.8", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("2.2");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setValue("2");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("1.8");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        // Binder validation fails:
        testField.setValue("");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void step_changeValue_assertValidity() {
        $("input").id(STEP_INPUT).sendKeys("1.5", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("3", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("1");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(STEP_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setValue("1.5");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("3");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        // Binder validation fails:
        testField.setValue("");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2", Keys.ENTER);

        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.setValue("2");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("2");
        assertServerValid();
        assertClientValid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected NumberFieldElement getTestField() {
        return $(NumberFieldElement.class).first();
    }
}
