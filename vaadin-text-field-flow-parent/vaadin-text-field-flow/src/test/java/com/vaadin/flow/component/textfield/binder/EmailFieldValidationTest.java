package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Objects;

public class EmailFieldValidationTest
        extends AbstractTextFieldValidationTest<String> {

    private EmailField field;

    @Override
    protected HasValue<?, String> getField() {
        if (field == null) {
            field = new EmailField();
            // To disable pattern validation
            field.setPattern(null);
            field.setMaxLength(20);
        }
        return field;
    }

    @Override
    protected void setValidValue() {
        getField().setValue("contact@example.com");
    }

    @Override
    protected void setComponentInvalidValue() {
        getField().setValue("reallylongemail@example.com");
    }

    @Override
    protected void setBinderInvalidValue() {
        getField().setValue("contact@another.com");
    }

    @Override
    protected void setEmptyValue() {
        getField().setValue("");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "")
                || value.contains("@example.com");
    }
}
