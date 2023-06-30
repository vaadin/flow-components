package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.function.SerializablePredicate;

public class IntegerFieldValidationTest
        extends AbstractTextFieldValidationTest<Integer, IntegerField> {

    @Tag("test-integer-field")
    private class TestIntegerField extends IntegerField {
        protected boolean isEnforcedFieldValidationEnabled() {
            return true;
        }
    }

    @Override
    protected void initField() {
        field = new TestIntegerField();
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