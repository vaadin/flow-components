/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import org.junit.Test;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class IntegerFieldBasicValidationTest
        extends AbstractBasicValidationTest<IntegerField, Integer> {
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

    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
