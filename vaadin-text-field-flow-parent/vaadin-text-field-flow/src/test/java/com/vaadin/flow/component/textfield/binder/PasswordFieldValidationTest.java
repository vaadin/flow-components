package com.vaadin.flow.component.textfield.binder;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Objects;

public class PasswordFieldValidationTest
        extends AbstractTextFieldValidationTest<String, PasswordField> {

    @Tag("test-password-field")
    private class TestPasswordField extends PasswordField {
        protected boolean isFeatureFlagEnabled(Feature feature) {
            if (feature.getId() == FeatureFlags.ENFORCE_FIELD_VALIDATION
                    .getId()) {
                return true;
            }

            return super.isFeatureFlagEnabled(feature);
        }
    }

    @Override
    protected void initField() {
        field = new TestPasswordField();
        field.setMaxLength(10);
    }

    @Override
    protected void setValidValue() {
        field.setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("AAAAAAAAAAAAAA");
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
