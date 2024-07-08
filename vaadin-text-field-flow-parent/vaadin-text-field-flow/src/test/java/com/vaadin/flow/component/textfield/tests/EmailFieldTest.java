/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.textfield.EmailField;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link EmailField}.
 */
public class EmailFieldTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        EmailField emailField = new EmailField();
        assertEquals("Value should be an empty string", "",
                emailField.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        emailField.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        EmailField emailField = new EmailField();
        assertEquals(emailField.getEmptyValue(),
                emailField.getElement().getProperty("value"));
    }

}
