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

import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldBinderValidationPage.BAD_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldBinderValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldBinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.BigDecimalFieldBinderValidationPage.RESET_BEAN_BUTTON;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.BigDecimalFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-big-decimal-field/validation/binder")
public class BigDecimalFieldBinderValidationIT
        extends AbstractValidationIT<BigDecimalFieldElement> {
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
        testField.setValue("1234");
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
    public void required_setValue_resetBean_assertValidity() {
        testField.setValue("1234");
        assertServerValid();
        assertClientValid();

        $("button").id(RESET_BEAN_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
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
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected BigDecimalFieldElement getTestField() {
        return $(BigDecimalFieldElement.class).first();
    }
}
