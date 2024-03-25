/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalDateTime;

@Route("vaadin-date-time-picker/date-time-picker-step")
public class DateTimePickerStepPage extends Div {

    public DateTimePickerStepPage() {
        setupInitialSteps();
        setupChangeSteps();
    }

    private void setupInitialSteps() {
        DateTimePicker picker = new DateTimePicker();
        picker.setId("initial-steps-date-time-picker");
        picker.setStep(Duration.ofMillis(500));
        picker.setValue(LocalDateTime.of(2021, 9, 13, 15, 20, 30)
                .plus(Duration.ofMillis(123)));

        add(new H1("Initial steps"));
        add(picker);
    }

    private void setupChangeSteps() {
        DateTimePicker picker = new DateTimePicker();
        picker.setId("change-steps-date-time-picker");

        NativeButton valueButton = new NativeButton("Set value");
        valueButton.addClickListener(e -> picker.setValue(LocalDateTime
                .of(2021, 9, 13, 15, 20, 30).plus(Duration.ofMillis(123))));
        valueButton.setId("set-date-time-value");

        NativeButton secondPrecisionButton = new NativeButton(
                "Set seconds precision");
        secondPrecisionButton
                .addClickListener(e -> picker.setStep(Duration.ofSeconds(30)));
        secondPrecisionButton.setId("set-second-precision");

        NativeButton millisecondPrecisionButton = new NativeButton(
                "Set milliseconds precision");
        millisecondPrecisionButton
                .addClickListener(e -> picker.setStep(Duration.ofMillis(500)));
        millisecondPrecisionButton.setId("set-millisecond-precision");

        add(new H1("Change steps"));
        add(picker);
        add(new Div(valueButton, secondPrecisionButton,
                millisecondPrecisionButton));
    }
}
