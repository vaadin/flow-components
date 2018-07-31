/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker.demo;

import java.time.LocalTime;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link TimePicker} demo.
 */
@Route("vaadin-time-picker")
public class TimePickerView extends DemoView {
    private Div message;

    @Override
    public void initView() {
        createDefaultTimePicker();
        createDisabledTimePicker();
        createTimePickerWithStepSetting();
    }

    private void createDefaultTimePicker() {
        Div message = createMessageDiv("simple-picker-message");
        // begin-source-example
        // source-example-heading: Default Time Picker
        TimePicker timePicker = new TimePicker();

        timePicker.addValueChangeListener(
                event -> UpdateMessage(message, timePicker));
        // end-source-example

        timePicker.setId("simple-picker");
        addCard("Default Time Picker", timePicker, message);
    }

    private void createTimePickerWithStepSetting() {
        Div message = createMessageDiv("step-setting-picker-message");
        Label label = new Label(
                "The first two steps will not show the dropdown.\n Different step setting will affect the displayed time pattern.");
        // begin-source-example
        // source-example-heading: Time Picker With Step Setting
        TimePicker timePicker = new TimePicker();

        NativeButton button1 = new NativeButton("Time Pattern: hh:mm:ss.fff", 
                event-> { 
                    timePicker.setStep(0.5);
                    message.setText("Current Step:" + timePicker.getStep());
                });
        NativeButton button2 = new NativeButton("Time Pattern: hh:mm:ss",
                event -> {
                    timePicker.setStep(6.0);
                    message.setText("Current Step:" + timePicker.getStep());
                });
        NativeButton button3 = new NativeButton("Time Pattern: hh:mm",
                event -> {
                    timePicker.setStep(900.0);
                    message.setText("Current Step:" + timePicker.getStep());
                });
        // end-source-example
        timePicker.setId("step-setting-picker");
        button1.setId("step-0.5");
        button2.setId("step-6.0");
        button3.setId("step-900.0");
        addCard("Time Picker With Step Setting", label, timePicker, button1,
                button2, button3, message);
    }

    private void createDisabledTimePicker() {
        Div message = createMessageDiv("disabled-picker-message");

        // begin-source-example
        // source-example-heading: Disabled Time Picker
        TimePicker timePicker = new TimePicker();
        timePicker.setEnabled(false);
        // end-source-example

        timePicker.addValueChangeListener(event -> {
            message.setText("This event should not have happened");
        });

        timePicker.setId("disabled-picker");
        addCard("Disabled Time Picker", timePicker, message);
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private void UpdateMessage(Div message, TimePicker timePicker) {
        LocalTime selectedTime = timePicker.getValue();
        if (selectedTime != null) {
            message.setText("Hour: " + selectedTime.getHour() + "\nMinute: "
                    + selectedTime.getMinute());
        } else {
            message.setText("No time is selected");
        }
    }
}
