/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.function.SerializablePredicate;

public class IntegerFieldBinderValidationTest
        extends AbstractBinderValidationTest<Integer, IntegerField> {

    @Override
    protected void initField() {
        field = new IntegerField();
        field.setMax(10);
    }

    @Override
    protected void setValidValue() {
        field.setValue(5);
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue(15);
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue(1);
    }

    @Override
    protected SerializablePredicate<? super Integer> getValidator() {
        return value -> value == null || value > 2;
    }
}
