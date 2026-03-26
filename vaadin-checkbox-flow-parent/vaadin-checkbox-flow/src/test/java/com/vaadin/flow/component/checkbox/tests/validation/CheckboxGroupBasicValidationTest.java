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
package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

class CheckboxGroupBasicValidationTest extends
        AbstractBasicValidationTest<CheckboxGroup<String>, Set<String>> {
    @Test
    void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assertions.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        testField.setErrorMessage("");
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> select = new CheckboxGroup<>();
        select.setItems("foo");
        return select;
    }
}
