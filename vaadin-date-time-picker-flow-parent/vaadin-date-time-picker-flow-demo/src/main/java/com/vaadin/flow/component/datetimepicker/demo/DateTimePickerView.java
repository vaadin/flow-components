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
package com.vaadin.flow.component.datetimepicker.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link DateTimePicker} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-date-time-picker")
public class DateTimePickerView extends DemoView {

    @Override
    public void initView() {
        createSimpleDateTimePicker();
        createLocaleChangeDateTimePicker();
        addCard("Additional code used in the demo",
                new Label("These methods are used in the demo."));
    }

    private void createSimpleDateTimePicker() {
        Div message = createMessageDiv("simple-picker-message");

        // begin-source-example
        // source-example-heading: Simple date time picker
        DateTimePicker dateTimePicker = new DateTimePicker();

        dateTimePicker.addValueChangeListener(
                event -> updateMessage(message, dateTimePicker));
        // end-source-example

        dateTimePicker.setId("simple-picker");
        addCard("Simple date time picker", dateTimePicker, message);
    }

    private void createLocaleChangeDateTimePicker() {
        Div message = createMessageDiv("Customize-locale-picker-message");
        // begin-source-example
        // source-example-heading: Date time picker with different locales
        // By default, the dateTimePicker uses the current UI locale
        DateTimePicker dateTimePicker = new DateTimePicker();
        NativeButton locale1 = new NativeButton("Locale: US");
        NativeButton locale2 = new NativeButton("Locale: UK");
        NativeButton locale3 = new NativeButton("Locale: CHINA");

        locale1.addClickListener(e -> {
            dateTimePicker.setLocale(Locale.US);
            updateMessage(message, dateTimePicker);
        });
        locale2.addClickListener(e -> {
            dateTimePicker.setLocale(Locale.UK);
            updateMessage(message, dateTimePicker);
        });
        locale3.addClickListener(e -> {
            dateTimePicker.setLocale(Locale.CHINA);
            updateMessage(message, dateTimePicker);
        });

        dateTimePicker.addValueChangeListener(
                event -> updateMessage(message, dateTimePicker));
        // end-source-example
        locale1.setId("Locale-US");
        locale2.setId("Locale-UK");
        dateTimePicker.setId("locale-change-picker");
        addCard("Date time picker with different locales", dateTimePicker, locale1,
                locale2, locale3, message);
    }

    // begin-source-example
    // source-example-heading: Additional code used in the demo
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
    // end-source-example

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }
}
