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
package com.vaadin.flow.component.combobox.validation;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class ComboBoxBasicValidationTest
        extends AbstractBasicValidationTest<ComboBox<String>, String> {
    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue("foo");
        testField.setValue(null);
        Assert.assertEquals("", getErrorMessageProperty());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new ComboBox.ComboBoxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue("foo");
        testField.setValue(null);
        Assert.assertEquals("Field is required", getErrorMessageProperty());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new ComboBox.ComboBoxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue("foo");
        testField.setValue(null);
        Assert.assertEquals("Custom error message", getErrorMessageProperty());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new ComboBox.ComboBoxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue("foo");
        testField.setValue(null);
        testField.setErrorMessage("");
        Assert.assertEquals("Field is required", getErrorMessageProperty());
    }

    @Override
    protected ComboBox<String> createTestField() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("foo");
        return comboBox;
    }

    private String getErrorMessageProperty() {
        return testField.getElement().getProperty("errorMessage");
    }
}
