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
package com.vaadin.flow.component.textfield.validation;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BigDecimalFieldBasicValidationTest
        extends AbstractBasicValidationTest<BigDecimalField, BigDecimal> {
    @Test
    public void addValidationStatusChangeListener_addAnotherListenerOnInvocation_noExceptions() {
        testField.addValidationStatusChangeListener(event1 -> {
            testField.addValidationStatusChangeListener(event2 -> {
            });
        });

        // Trigger ValidationStatusChangeEvent
        simulateBadInput();
        testField.clear();
    }

    @Test
    public void badInput_validate_emptyErrorMessageDisplayed() {
        testField.getElement().setProperty("_hasInputValue", true);
        simulateBadInput();
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInput_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setBadInputErrorMessage("Value has invalid format"));
        testField.getElement().setProperty("_hasInputValue", true);
        simulateBadInput();
        Assert.assertEquals("Value has invalid format",
                testField.getErrorMessage());
    }

    @Test
    public void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assert.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        testField.setErrorMessage("");
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Override
    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }

    private void simulateBadInput() {
        testField.getElement().setProperty("_hasInputValue", true);

        try {
            Method validateMethod = BigDecimalField.class
                    .getDeclaredMethod("validate");
            validateMethod.setAccessible(true);
            validateMethod.invoke(testField);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
