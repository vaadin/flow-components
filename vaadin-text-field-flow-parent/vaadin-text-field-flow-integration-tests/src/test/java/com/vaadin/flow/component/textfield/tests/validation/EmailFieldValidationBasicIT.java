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

import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldValidationBasicPage.MIN_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldValidationBasicPage.MAX_LENGTH_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldValidationBasicPage.PATTERN_INPUT;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldValidationBasicPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.EmailFieldValidationBasicPage.ATTACH_FIELD_BUTTON;

@TestPath("vaadin-email-field/validation/basic")
public class EmailFieldValidationBasicIT
        extends AbstractValidationIT<EmailFieldElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        $("button").id(REQUIRED_BUTTON).click();

        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys("john@vaadin.com");
        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys(Keys.ENTER);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        testField = getTestField();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setValue("john@vaadin.com");
        assertServerValid();
        assertClientValid();

        testField.setValue("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void minLength_triggerInputBlur_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("13", Keys.ENTER);

        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void minLength_changeInputValue_assertValidity() {
        $("input").id(MIN_LENGTH_INPUT).sendKeys("13", Keys.ENTER);

        testField.setValue("a@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("aa@vaadin.com");
        assertClientValid();
        assertServerValid();

        testField.setValue("aaa@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void maxLength_changeInputValue_assertValidity() {
        $("input").id(MAX_LENGTH_INPUT).sendKeys("13", Keys.ENTER);

        testField.setValue("aaa@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("aa@vaadin.com");
        assertClientValid();
        assertServerValid();

        testField.setValue("a@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void defaultPattern_triggerInputBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void defaultPattern_changeInputValue_assertValidity() {
        testField.setValue("arbitrary string");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("john@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void pattern_changeInputValue_assertValidity() {
        $("input").id(PATTERN_INPUT).sendKeys("^[^\\d]+@vaadin.com$",
                Keys.ENTER);

        testField.setValue("2222@vaadin.com");
        assertClientInvalid();
        assertServerInvalid();

        testField.setValue("john@vaadin.com");
        assertClientValid();
        assertServerValid();
    }

    protected EmailFieldElement getTestField() {
        return $(EmailFieldElement.class).first();
    }
}
