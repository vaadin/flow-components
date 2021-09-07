/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Route("vaadin-time-picker/time-picker-value-change")
public class TimePickerValueChangePage extends Div {

    Div changeLog;

    public TimePickerValueChangePage() {
        createTimePicker();
        createChangeLog();
    }

    private void createTimePicker() {
        TimePicker timePicker = new TimePicker();
        timePicker.setId("time-picker");
        timePicker.setLabel("TimePicker");
        timePicker.addValueChangeListener(this::logValueChangedEvent);

        NativeButton setCurrentTimeButton = new NativeButton("Set current time",
                event -> {
                    timePicker.setValue(LocalTime.now());
                });
        setCurrentTimeButton.setId("set-current-time");

        NativeButton setSecondsPrecisionButton = new NativeButton(
                "Set seconds precision", event -> {
                    timePicker.setStep(Duration.of(30, ChronoUnit.SECONDS));
                });
        setSecondsPrecisionButton.setId("set-seconds-precision");

        NativeButton setMillisPrecisionButton = new NativeButton(
                "Set millisecond precision", event -> {
                    timePicker.setStep(Duration.of(500, ChronoUnit.MILLIS));
                });
        setMillisPrecisionButton.setId("set-millis-precision");

        add(timePicker);
        add(new Div(setCurrentTimeButton, setSecondsPrecisionButton,
                setMillisPrecisionButton));
    }

    private void createChangeLog() {
        changeLog = new Div();
        changeLog.setId("change-log");
        changeLog.getStyle().set("whiteSpace", "pre");
        add(changeLog);
    }

    private void logValueChangedEvent(
            AbstractField.ComponentValueChangeEvent<TimePicker, LocalTime> event) {
        String source = event.isFromClient() ? "client" : "server";
        String value = event.getValue().format(DateTimeFormatter.ISO_TIME);

        String logEntry = String.format("source: %s; value: %s%n", source,
                value);

        changeLog.setText(changeLog.getText() + logEntry);
    }
}
