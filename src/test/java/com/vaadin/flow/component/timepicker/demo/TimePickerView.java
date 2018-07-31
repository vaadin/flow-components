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
        message.setId("simple-picker-message");
        addCard("Default Time Picker", timePicker, message);
    }

    private void createDisabledTimePicker() {
        Div message = createMessageDiv("disabled-picker-message");

        // begin-source-example
        // source-example-heading: Disabled time picker
        TimePicker timePicker = new TimePicker();
        timePicker.setEnabled(false);
        // end-source-example

        timePicker.addValueChangeListener(event -> {
            message.setText("This event should not have happened");
        });

        timePicker.setId("disabled-picker");
        message.setId("disabled-picker-message");
        addCard("Disabled time picker", timePicker, message);
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
