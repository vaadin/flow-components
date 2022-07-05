package com.vaadin.flow.component.combobox;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatusHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

public class ComboBoxValidationTest {
    private static final String BINDER_FAIL_MESSAGE = "YEAR_LESS_THAN_MIN";
    private static final String REQUIRED_MESSAGE = "REQUIRED";

    public static class Bean {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void elementWithConstraints_binderValidationNotMet_binderValidationFails() {
        var field = getFieldWithValidation(status -> {
            Assert.assertTrue(status.isError());
            Assert.assertEquals(BINDER_FAIL_MESSAGE,
                    status.getMessage().orElse(""));
        });

        field.setValue("bar");
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
        field.setValue("foo");
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
        field.setValue("foo");
        field.setValue(null);
    }

    @Test
    public void elementWithConstraints_validValue_validationOk() {
        var field = getFieldWithValidation(status -> {
            Assert.assertFalse(status.isError());
        });

        field.setValue("foo");
    }

    private ComboBox<String> getFieldWithValidation(
            BindingValidationStatusHandler handler) {
        return getFieldWithValidation(handler, false);
    }

    private ComboBox<String> getFieldWithValidation(
            BindingValidationStatusHandler handler, boolean isRequired) {
        var field = new ComboBox<String>();
        field.setItems(Arrays.asList("foo", "bar", "baz"));
        var binder = new Binder<>(Bean.class);
        var binding = binder.forField(field)
                .withValidator(
                        value -> value == null || Objects.equals(value, "foo"),
                        BINDER_FAIL_MESSAGE)
                .withValidationStatusHandler(handler);

        if (isRequired) {
            binding.asRequired(REQUIRED_MESSAGE);
        }

        binding.bind("value");

        return field;
    }
}
