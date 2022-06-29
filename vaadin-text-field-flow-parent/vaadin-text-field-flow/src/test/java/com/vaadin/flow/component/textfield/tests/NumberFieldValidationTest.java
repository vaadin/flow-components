package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.shared.ValidationError;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class NumberFieldValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "YEAR_LESS_THAN_MIN";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean {
        private Double number;

        public Double getNumber() {
            return number;
        }

        public void setNumber(Double number) {
            this.number = number;
        }
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(ValidationError.GREATER_THAN_MAX,
                    status.getMessage().orElse(""));
        });

        field.setValue(20d);
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(BINDER_FAIL_MESSAGE,
                    status.getMessage().orElse(""));
        });

        field.setValue(1d);
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
        field.setValue(5d);
        field.setValue(null);
    }

    @Test
    public void elementRequiredFromComponent_emptyField_binderValidationOK() {
        var field = getFieldWithValidation(status -> {
            if (status.getField().isEmpty()) {
                Assert.assertFalse(status.isError());
            }
        });
        field.setRequiredIndicatorVisible(true);
        field.setValue(5d);
        field.setValue(null);
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation(status -> {
            Assert.assertFalse(status.isError());
        });

        field.setValue(5d);
    }

    private NumberField getFieldWithValidation(
            BindingValidationStatusHandler handler) {
        return getFieldWithValidation(handler, false);
    }

    private NumberField getFieldWithValidation(
            BindingValidationStatusHandler handler, boolean isRequired) {
        var field = new NumberField();
        field.setMax(10);
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(value -> value == null || value > 2,
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(handler);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("number");

        return field;
    }
}
