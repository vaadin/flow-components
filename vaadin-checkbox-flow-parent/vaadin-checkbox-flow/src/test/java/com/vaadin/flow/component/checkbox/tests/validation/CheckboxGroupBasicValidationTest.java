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
package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class CheckboxGroupBasicValidationTest extends
        AbstractBasicValidationTest<CheckboxGroup<String>, Set<String>> {
    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assert.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        testField.setErrorMessage("");
        testField.setValue(Set.of("foo"));
        testField.setValue(Set.of());
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> select = new CheckboxGroup<>();
        select.setItems("foo");
        return select;
    }
}
