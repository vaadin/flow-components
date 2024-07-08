/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.textfield.PasswordField;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link PasswordField}.
 */
public class PasswordFieldTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        PasswordField passwordField = new PasswordField();
        assertEquals("Value should be an empty string", "",
                passwordField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        passwordField.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        PasswordField passwordField = new PasswordField();
        assertEquals(passwordField.getEmptyValue(),
                passwordField.getElement().getProperty("value"));
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        PasswordField passwordField = new PasswordField();

        assertClearButtonPropertyValueEquals(passwordField, true);
        assertClearButtonPropertyValueEquals(passwordField, false);
    }

    public void assertClearButtonPropertyValueEquals(
            PasswordField passwordField, Boolean value) {
        passwordField.setClearButtonVisible(value);
        assertEquals(value, passwordField.isClearButtonVisible());
        assertEquals(passwordField.isClearButtonVisible(), passwordField
                .getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    public void autoselectPropertyValue() {
        PasswordField passwordField = new PasswordField();

        assertAutoselectPropertyValueEquals(passwordField, true);
        assertAutoselectPropertyValueEquals(passwordField, false);
    }

    public void assertAutoselectPropertyValueEquals(PasswordField passwordField,
            Boolean value) {
        passwordField.setAutoselect(value);
        assertEquals(value, passwordField.isAutoselect());
        assertEquals(passwordField.isAutoselect(),
                passwordField.getElement().getProperty("autoselect", value));
    }
}
