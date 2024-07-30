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
package com.vaadin.flow.component.checkbox.tests.validation;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class CheckboxBasicValidationTest
        extends AbstractBasicValidationTest<Checkbox, Boolean> {
    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(true);
        testField.setValue(false);
        Assert.assertEquals("", getErrorMessageProperty());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(true);
        testField.setValue(false);
        Assert.assertEquals("Field is required", getErrorMessageProperty());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(true);
        testField.setValue(false);
        Assert.assertEquals("Custom error message", getErrorMessageProperty());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(true);
        testField.setValue(false);
        testField.setErrorMessage("");
        Assert.assertEquals("Field is required", getErrorMessageProperty());
    }

    @Override
    protected Checkbox createTestField() {
        return new Checkbox();
    }

    private String getErrorMessageProperty() {
        return testField.getElement().getProperty("errorMessage");
    }
}
