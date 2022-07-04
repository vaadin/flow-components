package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.shared.ValidationError;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.function.SerializablePredicate;

public class NumberFieldValidationTest
        extends AbstractTextFieldValidationTest<Double> {

    private NumberField field;

    protected String fieldConstraintErrorMessage = ValidationError.GREATER_THAN_MAX;

    @Override
    protected HasValue<?, Double> getField() {
        if (field == null) {
            field = new NumberField();
            field.setMax(10);
        }

        return field;
    }

    @Override
    protected void setValidValue() {
        getField().setValue(5d);
    }

    @Override
    protected void setComponentInvalidValue() {
        getField().setValue(15d);
    }

    @Override
    protected void setBinderInvalidValue() {
        getField().setValue(1d);
    }

    @Override
    protected void setEmptyValue() {
        getField().setValue(getField().getEmptyValue());
    }

    @Override
    protected SerializablePredicate<? super Double> getValidator() {
        return value -> value == null || value > 2d;
    }
}
