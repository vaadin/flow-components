/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.datepicker.validation;

import java.time.LocalDate;

import org.junit.Test;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<DatePicker, LocalDate> {
    @Test
    public void addValidationStatusChangeListener_addAnotherListenerOnInvocation_noExceptions() {
        testField.addValidationStatusChangeListener(event1 -> {
            testField.addValidationStatusChangeListener(event2 -> {
            });
        });

        // Trigger ValidationStatusChangeEvent
        testField.getElement().setProperty("_hasInputValue", true);
        testField.clear();
    }

    protected DatePicker createTestField() {
        return new DatePicker();
    }
}
