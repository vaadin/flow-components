package com.vaadin.flow.component.textfield.binder;

import java.util.Objects;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

public class TextFieldValidationTest
        extends AbstractTextFieldValidationTest<String, TextField> {

    @Tag("test-text-field")
    private class TestTextField extends TextField {
        protected boolean isEnforcedFieldValidationEnabled() {
            return true;
        }
    }

    @Override
    protected void initField() {
        field = new TestTextField();
        field.setMaxLength(8);
    }

    @Override
    protected void setValidValue() {
        field.setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("AAAAAAAAAA");
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue("A");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "") || value.length() > 2;
    }
}
