package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class DateTimePickerValidationTest {

    private static final String BINDER_FAIL_MESSAGE = "YEAR_LESS_THAN_MIN";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean {
        private LocalDateTime date;

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }
    }

    @Test
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(ValidationError.GREATER_THAN_MAX,
                    status.getMessage().orElse(""));
        });

        field.setValue(LocalDateTime.now().plusDays(2));
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(BINDER_FAIL_MESSAGE,
                    status.getMessage().orElse(""));
        });

        field.setValue(LocalDateTime.now().minusYears(1));
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
        field.setValue(LocalDateTime.now());
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
        field.setValue(LocalDateTime.now());
        field.setValue(null);
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation(status -> {
            Assert.assertFalse(status.isError());
        });

        field.setValue(LocalDateTime.now());
    }

    private DateTimePicker getFieldWithValidation(
            BindingValidationStatusHandler handler) {
        return getFieldWithValidation(handler, false);
    }

    private DateTimePicker getFieldWithValidation(
            BindingValidationStatusHandler handler, boolean isRequired) {
        var field = new DateTimePicker();
        field.setMax(LocalDateTime.now().plusDays(1));
        var binder = new Binder<>(Bean.class);
        Binder.BindingBuilder<Bean, LocalDateTime> binding = binder
                .forField(field)
                .withValidator(date -> date == null
                        || date.getYear() >= LocalDateTime.now().getYear(),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(handler);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("date");

        return field;
    }
}
