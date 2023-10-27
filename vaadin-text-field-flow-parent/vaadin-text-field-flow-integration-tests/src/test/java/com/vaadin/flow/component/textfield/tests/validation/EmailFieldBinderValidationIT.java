/*
 * Copyright 2000-2023 Vaadin Ltd.
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
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("john@vaadin.com",
                Keys.ENTER);

        testField.setValue("john@vaadin.com");
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
        $("input").id(MIN_LENGTH_INPUT).sendKeys("13", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("aaa@vaadin.com",
                Keys.ENTER);

        // Constraint validation fails:
        testField.setValue("a@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("aa@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("aaa@vaadin.com");
        assertValidationCount(1);
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
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("aa@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("a@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void defaultPattern_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("john@vaadin.com",
                Keys.ENTER);

        testField.setValue("arbitrary string");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        testField.setValue("john@vaadin.com");
        assertValidationCount(1);
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
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setValue("oliver@vaadin.com");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setValue("john@vaadin.com");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Override
    protected EmailFieldElement getTestField() {
        return $(EmailFieldElement.class).first();
    }
}
