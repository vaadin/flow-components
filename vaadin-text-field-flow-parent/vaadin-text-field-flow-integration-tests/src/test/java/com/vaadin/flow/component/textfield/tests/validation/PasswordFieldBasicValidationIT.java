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

import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.PasswordFieldBasicValidationPage.REQUIRED_BUTTON;

@TestPath("vaadin-password-field/validation/basic")
public class PasswordFieldBasicValidationIT
        extends AbstractValidationIT<PasswordFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("Value");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void minLength_triggerBlur_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void minLength_changeValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("A");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("AA");
        assertClientValid();
        assertServerValid();

        testField.setValue("AAA");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("AAA");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("AA");
        assertClientValid();
        assertServerValid();

        testField.setValue("A");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_triggerBlur_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);

        // Tab to the show button
        testField.sendKeys(Keys.TAB);
        // Tab out of the field
        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void pattern_changeValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);

        testField.setValue("Word");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("1234");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.sendKeys(Keys.TAB);
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
        testField.sendKeys(Keys.TAB);

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    protected PasswordFieldElement getTestField() {
        return $(PasswordFieldElement.class).first();
    }
}
