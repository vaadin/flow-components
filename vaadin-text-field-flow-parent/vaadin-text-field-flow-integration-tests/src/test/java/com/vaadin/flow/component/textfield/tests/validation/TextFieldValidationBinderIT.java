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
package com.vaadin.flow.component.textfield.tests.validation;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-text-field/validation/binder")
public class TextFieldValidationBinderIT
        extends AbstractValidationBinderIT<TextFieldElement> {
    @Test
    public void minLength_changeInputValue_assertValidity() {
        $("input").id(TextFieldValidationBinderPage.MIN_LENGTH_INPUT)
                .sendKeys("2", Keys.ENTER);
        $("input").id(AbstractValidationBinderPage.EXPECTED_VALUE_INPUT)
                .sendKeys("AAA", Keys.ENTER);

        // Constraint validation fails:
        field.setValue("A");
        assertClientValid(false);
        assertServerValid(false);

        // Binder validation fails:
        field.setValue("AA");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(
                AbstractValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.setValue("AAA");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void maxLength_changeInputValue_assertValidity() {
        $("input").id(TextFieldValidationBinderPage.MAX_LENGTH_INPUT)
                .sendKeys("2", Keys.ENTER);
        $("input").id(AbstractValidationBinderPage.EXPECTED_VALUE_INPUT)
                .sendKeys("A", Keys.ENTER);

        // Constraint validation fails:
        field.setValue("AAA");
        assertClientValid(false);
        assertServerValid(false);

        // Binder validation fails:
        field.setValue("AA");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(
                AbstractValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.setValue("A");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void pattern_changeInputValue_assertValidity() {
        $("input").id(TextFieldValidationBinderPage.PATTERN_INPUT)
                .sendKeys("^\\d+$", Keys.ENTER);
        $("input").id(AbstractValidationBinderPage.EXPECTED_VALUE_INPUT)
                .sendKeys("1234", Keys.ENTER);

        // Constraint validation fails:
        field.setValue("Word");
        assertClientValid(false);
        assertServerValid(false);

        // Binder validation fails:
        field.setValue("12");
        assertClientValid(false);
        assertServerValid(false);
        assertErrorMessage(
                AbstractValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        field.setValue("1234");
        assertClientValid(true);
        assertServerValid(true);
    }

    protected TextFieldElement getField() {
        return $(TextFieldElement.class).first();
    }
}
