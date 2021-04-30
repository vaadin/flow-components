/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-time-picker/date-time-picker-it")
public class DateTimePickerPage extends Div {

    public DateTimePickerPage() {
        // 1
        Div message = createMessageDiv("message");

        DateTimePicker picker = new DateTimePicker();
        picker.setId("date-time-picker");
        picker.addValueChangeListener(event -> updateMessage(message, picker));

        NativeButton focusButton = new NativeButton("focus");
        focusButton.setId("button-focus");
        focusButton.addClickListener(event -> picker.focus());

        add(picker, message, focusButton);

        // 2
        Div message2 = createMessageDiv("message-value-from-server");
        NativeButton button2 = new NativeButton("Set value from server");
        button2.setId("button-value-from-server");

        DateTimePicker picker2 = new DateTimePicker(
                LocalDateTime.of(2017, 3, 1, 12, 10));
        picker2.setId("date-time-picker-value-from-server");
        picker2.addValueChangeListener(
                event -> updateMessage(message2, picker2));
        button2.addClickListener(event -> picker2
                .setValue(LocalDateTime.of(2019, 10, 15, 9, 40)));

        NativeButton clearFromServerButton = new NativeButton("Clear from server",
                e -> picker2.clear());
        clearFromServerButton.setId("clear-from-server");

        add(new Hr(), picker2, message2, button2, clearFromServerButton);

        // 3
        NativeButton button3 = new NativeButton("Set locale");
        button3.setId("button-locale");

        DateTimePicker picker3 = new DateTimePicker(
                LocalDateTime.of(2018, 1, 2, 15, 30),
                Locale.forLanguageTag("fi"));
        picker3.setId("date-time-picker-locale");
        button3.addClickListener(event -> picker3.setLocale(Locale.US));

        add(new Hr(), picker3, button3);
    }

    private void updateMessage(Div message, DateTimePicker dateTimePicker) {
        LocalDateTime selectedDateTime = dateTimePicker.getValue();
        if (selectedDateTime != null) {
            final DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss");
            message.setText("Value: " + selectedDateTime.format(formatter)
                    + "\n" + "Locale: " + dateTimePicker.getLocale());
        } else {
            message.setText("No date is selected");
        }
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

}
