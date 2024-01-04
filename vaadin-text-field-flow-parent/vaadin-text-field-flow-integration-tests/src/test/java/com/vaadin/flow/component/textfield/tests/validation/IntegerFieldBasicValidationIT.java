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

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldBasicValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldBasicValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldBasicValidationPage.STEP_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldBasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldBasicValidationPage.CLEAR_VALUE_BUTTON;

@TestPath("vaadin-integer-field/validation/basic")
public class IntegerFieldBasicValidationIT
        extends AbstractValidationIT<IntegerFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("1234");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("1");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("2");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("3");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("3");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("2");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("1");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void step_changeValue_assertValidity() {
        $("input").id(STEP_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("1");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("2");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setValue("3");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setValue("2");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        testField.setValue("2");
        assertServerValid();
        assertClientValid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.sendKeys("--2", Keys.ENTER);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void maxIntegerOverflow_changeValue_assertValidity() {
        testField.setValue("999999999999");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void minIntegerOverflow_changeValue_assertValidity() {
        testField.setValue("-999999999999");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setValue("");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("2");
        testField.setValue("");

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void webComponentCanNotModifyInvalidState() {
        assertWebComponentCanNotModifyInvalidState();

        detachAndReattachField();

        assertWebComponentCanNotModifyInvalidState();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("2");
        testField.setValue("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected IntegerFieldElement getTestField() {
        return $(IntegerFieldElement.class).first();
    }
}
