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
package com.vaadin.flow.component.timepicker.tests;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-time-picker/time-picker-it")
public class TimePickerPage extends Div {

    public TimePickerPage() {
        createDefaultTimePicker();
        createAutoOpenDisabledTimePicker();
        createDisabledTimePicker();
        createTimePickerWithStepSetting();
        createTimePickerWithMinAndMaxSetting();
        createTimePickerFromRenderer();
        createHelperText();
        createHelperComponent();
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

    private void createAutoOpenDisabledTimePicker() {
        Div message = createMessageDiv("autoopendisabled-picker-message");
        TimePicker timePicker = new TimePicker();
        timePicker.setId("autoopendisabled-picker");
        timePicker.setLabel("Auto open disabled TimePicker");
        timePicker.setAutoOpen(false);

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

        timePicker.setMin(LocalTime.of(5, 0));
        timePicker.setMax(LocalTime.of(18, 0));

        timePicker.addValueChangeListener(
                event -> updateMessage(message, timePicker));

        timePicker.setId("time-picker-min-max");
        add(timePicker, message);
    }

    private void createTimePickerFromRenderer() {
        ComponentRenderer<TimePicker, TimePickerPage> renderer = new ComponentRenderer<>(
                () -> {
                    TimePicker timePicker = new TimePicker();
                    timePicker.setValue(LocalTime.now());
                    return timePicker;
                });
        renderer.render(getElement(), null);
    }

    private void createHelperText() {
        TimePicker timePickerHelperText = new TimePicker();
        timePickerHelperText.setId("time-picker-helper-text");
        timePickerHelperText.setHelperText("Helper text");
        NativeButton clearHelper = new NativeButton("Clear helper text", e -> {
            timePickerHelperText.setHelperText(null);
        });
        clearHelper.setId("button-clear-helper-text");

        add(timePickerHelperText, clearHelper);
    }

    private void createHelperComponent() {
        TimePicker timePickerHelperComponent = new TimePicker();
        timePickerHelperComponent.setId("time-picker-helper-component");

        Span span = new Span("Helper component");
        span.setId("helper-component");
        timePickerHelperComponent.setHelperComponent(span);

        NativeButton clearComponent = new NativeButton("Clear component helper",
                e -> {
                    timePickerHelperComponent.setHelperComponent(null);
                });
        clearComponent.setId("button-clear-helper-component");

        add(timePickerHelperComponent, clearComponent);
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
