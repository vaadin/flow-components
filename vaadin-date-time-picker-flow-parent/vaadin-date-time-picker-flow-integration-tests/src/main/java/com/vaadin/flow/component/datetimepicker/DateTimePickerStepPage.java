/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
