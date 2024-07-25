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

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.PATTERN_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.MIN_LENGTH_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.MAX_LENGTH_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

@TestPath("vaadin-password-field/validation/binder")
public class PasswordFieldBinderValidationIT
        extends AbstractValidationIT<PasswordFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("Value", Keys.ENTER);

        testField.setValue("Value");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void minLength_changeValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("AAA", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("A");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_LENGTH_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setValue("AA");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("AAA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("2", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("A", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("AAA");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_LENGTH_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setValue("AA");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("A");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_changeValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("1234", Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("Word");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(PATTERN_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setValue("12");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("1234");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Override
    protected PasswordFieldElement getTestField() {
        return $(PasswordFieldElement.class).first();
    }
}
