/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.select.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.select.Select;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

class BasicValidationTest
        extends AbstractBasicValidationTest<Select<String>, String> {
    @Test
    void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue("foo");
        testField.setValue(null);
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Select.SelectI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue("foo");
        testField.setValue(null);
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Select.SelectI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue("foo");
        testField.setValue(null);
        Assertions.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Select.SelectI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue("foo");
        testField.setValue(null);
        testField.setErrorMessage("");
        testField.setValue("foo");
        testField.setValue(null);
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Override
    protected Select<String> createTestField() {
        Select<String> select = new Select<>();
        select.setItems("foo");
        return select;
    }
}
