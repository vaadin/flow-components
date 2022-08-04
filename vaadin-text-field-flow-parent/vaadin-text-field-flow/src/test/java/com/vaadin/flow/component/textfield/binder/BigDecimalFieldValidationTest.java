package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.Ignore;

import java.math.BigDecimal;

public class BigDecimalFieldValidationTest
        extends AbstractTextFieldValidationTest<BigDecimal, BigDecimalField> {

    @Override
    protected void initField() {
        field = new BigDecimalField();
    }

    @Override
    protected void setValidValue() {
        field.setValue(new BigDecimal("5"));
    }

    @Override
    protected void setComponentInvalidValue() {

    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue(new BigDecimal("11"));
    }

    @Override
    protected SerializablePredicate<? super BigDecimal> getValidator() {
        return value -> value == null || value.compareTo(BigDecimal.TEN) < 0;
    }

    @Override
    @Ignore("Component doesn't have validation constraints")
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
    }
}
