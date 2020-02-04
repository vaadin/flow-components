/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

@Route("time-picker-it")
public class TimePickerPage extends Div {

    public TimePickerPage() {
        createDefaultTimePicker();
        createDisabledTimePicker();
        createTimePickerWithStepSetting();
        createTimePickerWithMinAndMaxSetting();
    }

    private void createDefaultTimePicker() {
        Div message = createMessageDiv("simple-picker-message");
        TimePicker timePicker = new TimePicker();
        timePicker.setId("simple-picker");
        timePicker.setLabel("Default TimePicker");

        timePicker.addValueChangeListener(
                event -> updateMessage(message, timePicker));

        add(timePicker, message);
    }

    private void createDisabledTimePicker() {
        Div message = createMessageDiv("disabled-picker-message");
        TimePicker timePicker = new TimePicker();
        timePicker.setEnabled(false);
        timePicker.setLabel("Disabled TimePicker");

        timePicker.addValueChangeListener(event -> message
                .setText("This event should not have happened"));

        timePicker.setId("disabled-picker");
        add(timePicker, message);
    }

    private void createTimePickerWithStepSetting() {
        TimePicker timePicker = new TimePicker();
        timePicker.setId("step-setting-picker");
        timePicker.setLabel("TimePicker with step");

        NativeSelect stepSelector = new NativeSelect();
        stepSelector.setWidth("70px");
        stepSelector.setOptions(
                Arrays.asList("0.5s", "10s", "1m", "15m", "30m", "1h"));
        stepSelector.setId("step-picker");

        stepSelector.addValueChangeListener(event -> {
            if (event.getValue() != null && !event.getValue().isEmpty()) {
                Duration step = Duration
                        .parse("PT" + event.getValue().toUpperCase());
                timePicker.setStep(step);
            }
        });

        add(stepSelector, timePicker);
    }

    private void createTimePickerWithMinAndMaxSetting() {
        Div message = createMessageDiv("time-picker-min-max-message");
        TimePicker timePicker = new TimePicker();
        timePicker.setLabel("TimePicker Min & Max");

        timePicker.setMin("05:00");
        timePicker.setMax("18:00");

        timePicker.addValueChangeListener(
                event -> updateMessage(message, timePicker));

        timePicker.setId("time-picker-min-max");
        add(timePicker, message);
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private void updateMessage(Div message, TimePicker timePicker) {
        LocalTime selectedTime = timePicker.getValue();
        if (selectedTime != null) {
            message.setText("Hour: " + selectedTime.getHour() + "\nMinute: "
                    + selectedTime.getMinute());
        } else {
            message.setText("No time is selected");
        }
    }

}
