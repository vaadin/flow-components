package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.shared.ValidationError;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class TextFieldValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAIL";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean {
        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(ValidationError.MAX_LENGTH_EXCEEDED,
                    status.getMessage().orElse(""));
        });

        field.setValue("AAAAAAAAAA");
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(BINDER_FAIL_MESSAGE,
                    status.getMessage().orElse(""));
        });

        field.setValue("A");
    }

    @Test
    public void elementRequiredFromBinder_emptyField_binderValidationFail() {
        var field = getFieldWithValidation(status -> {
            if (status.getField().isEmpty()) {
                Assert.assertTrue(status.isError());
                Assert.assertEquals(REQUIRED_MESSAGE,
                        status.getMessage().orElse(""));
            }
        }, true);
        field.setValue("ASDF");
        field.setValue("");
    }

    @Test
    public void elementRequiredFromComponent_emptyField_binderValidationOK() {
        var field = getFieldWithValidation(status -> {
            if (status.getField().isEmpty()) {
                Assert.assertFalse(status.isError());
            }
        });
        field.setRequiredIndicatorVisible(true);
        field.setValue("ASDF");
        field.setValue("");
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation(status -> {
            Assert.assertFalse(status.isError());
        });

        field.setValue("ASDF");
    }

    private TextField getFieldWithValidation(
            BindingValidationStatusHandler handler) {
        return getFieldWithValidation(handler, false);
    }

    private TextField getFieldWithValidation(
            BindingValidationStatusHandler handler, boolean isRequired) {
        var field = new TextField();
        field.setMaxLength(8);
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(value -> Objects.equals(value, "")
                        || value.length() > 2, BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(handler);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("string");

        return field;
    }
}
