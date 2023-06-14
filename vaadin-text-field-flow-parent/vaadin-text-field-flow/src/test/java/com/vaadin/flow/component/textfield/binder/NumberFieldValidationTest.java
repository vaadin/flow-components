package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.function.SerializablePredicate;

public class NumberFieldValidationTest
        extends AbstractTextFieldValidationTest<Double, NumberField> {

    @Override
    protected void initField() {
        field = new NumberField();
        field.setMax(10);
    }

    @Override
    protected void setValidValue() {
        field.setValue(5d);
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue(15d);
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue(1d);
    }

    @Override
    protected SerializablePredicate<? super Double> getValidator() {
        return value -> value == null || value > 2d;
    }
}