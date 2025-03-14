/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.MAX_LENGTH_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.MIN_LENGTH_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.PATTERN_ERROR_MESSAGE;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.TextAreaBasicValidationPage.REQUIRED_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-text-area/validation/basic")
public class TextAreaBasicValidationIT
        extends AbstractValidationIT<TextAreaElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("Value");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");

        testField.setValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void minLength_changeValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("A");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_LENGTH_ERROR_MESSAGE);

        testField.setValue("AA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.setValue("AAA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void maxLength_changeValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("2", Keys.ENTER);

        testField.setValue("AAA");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_LENGTH_ERROR_MESSAGE);

        testField.setValue("AA");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.setValue("A");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void pattern_changeValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^\\d+$", Keys.ENTER);

        testField.setValue("Word");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(PATTERN_ERROR_MESSAGE);

        testField.setValue("1234");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("Value");
        testField.setValue("");

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
<<<<<<< Updated upstream
    public void detach_attachAndInvalidate_preservesInvalidState() {
        detachField();
        attachAndInvalidateField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_hide_attach_showAndInvalidate_preservesInvalidState() {
        detachField();
        hideField();
        attachField();
        showAndInvalidateField();

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
=======
>>>>>>> Stashed changes
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setValue("Value");
        testField.setValue("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected TextAreaElement getTestField() {
        return $(TextAreaElement.class).first();
    }
}
