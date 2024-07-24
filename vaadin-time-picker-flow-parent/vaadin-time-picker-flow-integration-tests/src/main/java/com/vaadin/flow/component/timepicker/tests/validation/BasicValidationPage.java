package com.vaadin.flow.component.timepicker.tests.validation;

import java.time.LocalTime;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-time-picker/validation/basic")
public class BasicValidationPage extends AbstractValidationPage<TimePicker> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String BAD_INPUT_ERROR_MESSAGE = "Time has incorrect format";
    public static final String MIN_ERROR_MESSAGE = "Time is too small";
    public static final String MAX_ERROR_MESSAGE = "Time is too big";

    public BasicValidationPage() {
        super();

        testField.setI18n(new TimePicker.TimePickerI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE)
                .setBadInputErrorMessage(BAD_INPUT_ERROR_MESSAGE)
                .setMinErrorMessage(MIN_ERROR_MESSAGE)
                .setMaxErrorMessage(MAX_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
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
    }

    @Override
    protected TimePicker createTestField() {
        return new TimePicker() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
