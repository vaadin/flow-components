package com.vaadin.flow.component.timepicker.tests.validation;

import java.time.LocalTime;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-time-picker/validation/binder")
public class BinderValidationPage extends AbstractValidationPage<TimePicker> {
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";
    public static final String RESET_BEAN_BUTTON = "reset-bean-button";

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String BAD_INPUT_ERROR_MESSAGE = "Time has incorrect format";
    public static final String MIN_ERROR_MESSAGE = "Time is too small";
    public static final String MAX_ERROR_MESSAGE = "Time is too big";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "Time does not match the expected value";

    public static class Bean {
        private LocalTime property;

        public LocalTime getProperty() {
            return property;
        }

        public void setProperty(LocalTime property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    private LocalTime expectedValue;

    public BinderValidationPage() {
        super();

        testField.setI18n(new TimePicker.TimePickerI18n()
                .setBadInputErrorMessage(BAD_INPUT_ERROR_MESSAGE)
                .setMinErrorMessage(MIN_ERROR_MESSAGE)
                .setMaxErrorMessage(MAX_ERROR_MESSAGE));

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected time", event -> {
            LocalTime value = LocalTime.parse(event.getValue());
            expectedValue = value;
        }));

        add(createInput(MIN_INPUT, "Set min time", event -> {
            LocalTime value = LocalTime.parse(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max time", event -> {
            LocalTime value = LocalTime.parse(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));

        add(createButton(RESET_BEAN_BUTTON, "Reset bean", event -> {
            binder.setBean(new Bean());
        }));
    }

    @Override
    protected TimePicker createTestField() {
        return new TimePicker();
    }
}
