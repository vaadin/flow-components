package com.vaadin.flow.component.textfield.binder;

import java.util.Objects;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.function.SerializablePredicate;

public class EmailFieldValidationTest
        extends AbstractTextFieldValidationTest<String, EmailField> {

    @Tag("test-email-field")
    private class TestEmailField extends EmailField {
        protected boolean isEnforcedFieldValidationEnabled() {
            return true;
        }
    }

    @Override
    protected void initField() {
        field = new TestEmailField();
        // To disable pattern validation
        field.setPattern(null);
        field.setMaxLength(20);
    }

    @Override
    protected void setValidValue() {
        field.setValue("contact@example.com");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("reallylongemail@example.com");
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue("contact@another.com");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "")
                || value.contains("@example.com");
    }
}