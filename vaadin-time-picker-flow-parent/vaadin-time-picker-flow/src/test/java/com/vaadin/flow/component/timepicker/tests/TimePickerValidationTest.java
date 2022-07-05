package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalTime;

public class TimePickerValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "BINDER_VALIDATION_FAILED";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean {
        private LocalTime time;

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(ValidationError.GREATER_THAN_MAX,
                    status.getMessage().orElse(""));
        });

        field.setValue(LocalTime.now().plusHours(2));
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(BINDER_FAIL_MESSAGE,
                    status.getMessage().orElse(""));
        });

        field.setValue(LocalTime.now().minusHours(2));
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
        field.setValue(LocalTime.now());
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
        field.setValue(LocalTime.now());
        field.setValue(null);
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation(status -> {
            Assert.assertFalse(status.isError());
        });

        field.setValue(LocalTime.now());
    }

    private TimePicker getFieldWithValidation(
            BindingValidationStatusHandler handler) {
        return getFieldWithValidation(handler, false);
    }

    private TimePicker getFieldWithValidation(
            BindingValidationStatusHandler handler, boolean isRequired) {
        var field = new TimePicker();
        field.setMax(LocalTime.now().plusHours(1));
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalTime> binding = binder.forField(field)
                .withValidator(
                        value -> value == null
                                || value.isAfter(LocalTime.now().minusHours(1)),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(handler);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("time");

        return field;
    }
}
