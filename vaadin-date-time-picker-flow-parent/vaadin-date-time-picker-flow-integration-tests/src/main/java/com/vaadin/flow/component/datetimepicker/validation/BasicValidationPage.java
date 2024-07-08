/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker.validation;

import java.time.LocalDateTime;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-date-time-picker/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<DateTimePicker> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public BasicValidationPage() {
        super();

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));

        add(createInput(MIN_INPUT, "Set min date time", event -> {
            LocalDateTime value = LocalDateTime.parse(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date time", event -> {
            LocalDateTime value = LocalDateTime.parse(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));
    }

    protected DateTimePicker createTestField() {
        return new DateTimePicker();
    }
}
