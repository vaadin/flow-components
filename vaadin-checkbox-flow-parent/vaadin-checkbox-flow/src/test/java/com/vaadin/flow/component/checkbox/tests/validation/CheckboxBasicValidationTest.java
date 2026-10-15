/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
        Assert.assertEquals("", testField.getErrorMessage());
    }

    @Test
    public void required_setI18nErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setValue(true);
        testField.setValue(false);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_customErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(true);
        testField.setValue(false);
        Assert.assertEquals("Custom error message",
                testField.getErrorMessage());
    }

    @Test
    public void setI18nAndCustomErrorMessage_validate_removeCustomErrorMessage_validate_i18nErrorMessageDisplayed() {
        testField.setRequiredIndicatorVisible(true);
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage("Field is required"));
        testField.setErrorMessage("Custom error message");
        testField.setValue(true);
        testField.setValue(false);
        testField.setErrorMessage("");
        testField.setValue(true);
        testField.setValue(false);
        Assert.assertEquals("Field is required", testField.getErrorMessage());
    }

    @Override
    protected Checkbox createTestField() {
        return new Checkbox();
    }
}
