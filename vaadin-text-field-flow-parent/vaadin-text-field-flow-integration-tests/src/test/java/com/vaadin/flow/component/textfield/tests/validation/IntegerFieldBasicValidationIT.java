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

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBasicPage.STEP_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBasicPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValidationBasicPage.CLEAR_VALUE_BUTTON;

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
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("1234");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_triggerBlur_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2", Keys.ENTER);

        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("1");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("2");
        assertClientValid();
        assertServerValid();

        testField.setValue("3");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_triggerBlur_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2", Keys.ENTER);

        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("3");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("2");
        assertClientValid();
        assertServerValid();

        testField.setValue("1");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void step_triggerBlur_assertValidity() {
        $("input").id(STEP_INPUT).sendKeys("2", Keys.ENTER);

        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void step_changeValue_assertValidity() {
        $("input").id(STEP_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("1");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("2");
        assertClientValid();
        assertServerValid();

        testField.setValue("3");
        assertClientInvalid();
        assertServerInvalid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        testField.sendKeys("--2", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();

        testField.setValue("2");
        assertServerValid();
        assertClientValid();

        testField.sendKeys("--2", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.sendKeys("--2", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void integerOverflow_setValueExceedingMaxInteger_assertValidity() {
        testField.sendKeys("999999999999", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void integerOverflow_setValueExceedingMinInteger_assertValidity() {
        testField.sendKeys("-999999999999", Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.sendKeys(Keys.TAB);

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
        testField.sendKeys(Keys.TAB);

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    protected IntegerFieldElement getTestField() {
        return $(IntegerFieldElement.class).first();
    }
}
