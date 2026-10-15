/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.dom.Element;
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
        fakeClientPropertyChange(testField, "value", "foo");
        testField.clear();
    }

    @Test
    public void badInput_validate_emptyErrorMessageDisplayed() {
        fakeClientPropertyChange(testField, "value", "foo");
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void badInput_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setI18n(new BigDecimalField.BigDecimalFieldI18n()
                .setBadInputErrorMessage("Value has invalid format"));
        fakeClientPropertyChange(testField, "value", "foo");
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

    private void fakeClientPropertyChange(Component component, String property,
            String value) {
        Element element = component.getElement();
        element.getStateProvider().setProperty(element.getNode(), property,
                value, false);
    }
}
