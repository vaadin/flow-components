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
package com.vaadin.flow.component.combobox.test.validation;

import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.ENABLE_CUSTOM_VALUE_BUTTON;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.combobox.test.validation.ComboBoxBinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-combo-box/validation/binder")
public class ComboBoxBinderValidationIT
        extends AbstractValidationIT<ComboBoxElement> {

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("foo", Keys.ENTER);

        // Binder validation fails
        testField.selectByText("bar");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Binder validation passes
        testField.selectByText("foo");
        assertServerValid();
        assertClientValid();

        // Required fails
        testField.clear();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);

        // Try enter custom value, required fails
        testField.sendKeys("custom", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_customValuesAllowed_changeValue_assertValidity() {
        $("button").id(ENABLE_CUSTOM_VALUE_BUTTON).click();
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("custom", Keys.ENTER);

        // Binder validation fails
        testField.sendKeys("invalid", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Binder validation passes
        testField.clear();
        testField.sendKeys("custom", Keys.TAB);
        assertServerValid();
        assertClientValid();

        // Required fails
        testField.clear();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected ComboBoxElement getTestField() {
        return $(ComboBoxElement.class).first();
    }
}
