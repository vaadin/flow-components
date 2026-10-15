/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
