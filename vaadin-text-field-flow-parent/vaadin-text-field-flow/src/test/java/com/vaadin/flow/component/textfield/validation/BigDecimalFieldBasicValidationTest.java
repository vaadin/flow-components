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
package com.vaadin.flow.component.textfield.validation;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

class BigDecimalFieldBasicValidationTest
        extends AbstractBasicValidationTest<BigDecimalField, BigDecimal> {
    @Test
    void addValidationStatusChangeListener_addAnotherListenerOnInvocation_noExceptions() {
        testField.addValidationStatusChangeListener(event1 -> {
            testField.addValidationStatusChangeListener(event2 -> {
            });
        });

        // Trigger ValidationStatusChangeEvent
        fakeClientPropertyChange(testField, "value", "foo");
        testField.clear();
    }

    @Test
    void badInput_validate_emptyErrorMessageDisplayed() {
        fakeClientPropertyChange(testField, "value", "foo");
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void badInput_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setBadInputErrorMessage("Value has invalid format"));
        fakeClientPropertyChange(testField, "value", "foo");
        Assertions.assertEquals("Value has invalid format",
                testField.getErrorMessage());
    }

    @Test
    void required_validate_emptyErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assertions.assertEquals("", testField.getErrorMessage());
    }

    @Test
    void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assertions.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        testField.setErrorMessage("");
        testField.setValue(new BigDecimal(1));
        testField.setValue(null);
        Assertions.assertEquals("Field is required",
                testField.getErrorMessage());
    }

    @Override
    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }

    private void fakeClientPropertyChange(Component component, String property,
            String value) {
        Element element = component.getElement();
        element.getStateProvider().setProperty(element.getNode(), property,
                value, false);
    }
}
