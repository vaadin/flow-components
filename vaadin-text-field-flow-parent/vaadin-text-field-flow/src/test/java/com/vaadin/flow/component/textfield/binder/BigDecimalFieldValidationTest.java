package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.function.SerializablePredicate;

import java.math.BigDecimal;

public class BigDecimalFieldValidationTest
        extends AbstractTextFieldValidationTest<BigDecimal> {

    private BigDecimalField field;

    @Override
    protected HasValue<?, BigDecimal> getField() {
        if (field == null) {
            field = new BigDecimalField();
        }
        return field;
    }

    @Override
    protected void setValidValue() {
        getField().setValue(new BigDecimal("5"));
    }

    @Override
    protected void setComponentInvalidValue() {

    }

    @Override
    protected void setBinderInvalidValue() {
        getField().setValue(new BigDecimal("11"));
    }

    @Override
    protected void setEmptyValue() {
        getField().setValue(getField().getEmptyValue());
    }

    @Override
    protected SerializablePredicate<? super BigDecimal> getValidator() {
        return value -> value == null || value.compareTo(BigDecimal.TEN) < 0;
    }

    @Override
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        // IGNORE TEST
    }
}
