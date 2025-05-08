/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/date-picker-test")
public class DatePickerPage extends Div {

    public DatePickerPage() {
        createSimpleDatePicker();
        createMinAndMaxDatePicker();
        createDisabledDatePicker();
        createStartAndEndDatePickers();
        createDatePickerWithOpenedChangeListener();
    }

    private void createSimpleDatePicker() {
        Div message = createMessageDiv("simple-picker-message");

        DatePicker datePicker = new DatePicker();

        datePicker.addValueChangeListener(
                event -> updateMessage(message, datePicker));

        datePicker.setId("simple-picker");

        NativeButton open = new NativeButton("Open");
        open.setId("open-simple-picker");
        open.addClickListener(event -> datePicker.open());

        addCard("Simple date picker", datePicker, open, message);
    }

    private void createMinAndMaxDatePicker() {
        Div message = createMessageDiv("min-and-max-picker-message");

        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Select a day within this month");
        datePicker.setPlaceholder("Date within this month");

        LocalDate now = LocalDate.now();

        datePicker.setMin(now.withDayOfMonth(1));
        datePicker.setMax(now.withDayOfMonth(now.lengthOfMonth()));

        datePicker.addValueChangeListener(
                event -> updateMessage(message, datePicker));

        datePicker.setId("min-and-max-picker");
        addCard("Date picker with min and max", datePicker, message);
    }

    private void createDisabledDatePicker() {
        Div message = createMessageDiv("disabled-picker-message");

        DatePicker datePicker = new DatePicker();
        datePicker.setEnabled(false);

        datePicker.addValueChangeListener(event -> message
                .setText("This event should not have happened"));

        datePicker.setId("disabled-picker");
        addCard("Disabled date picker", datePicker, message);
    }

    private void createStartAndEndDatePickers() {
        Div message = createMessageDiv("start-and-end-message");

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setLabel("Start");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setLabel("End");

        startDatePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (selectedDate != null) {
                endDatePicker.setMin(selectedDate.plusDays(1));
                if (endDate == null) {
                    endDatePicker.setOpened(true);
                    message.setText("Select the ending date");
                } else {
                    message.setText(
                            "Selected period:\nFrom " + selectedDate.toString()
                                    + " to " + endDate.toString());
                }
            } else {
                endDatePicker.setMin(null);
                message.setText("Select the starting date");
            }
        });

        endDatePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate startDate = startDatePicker.getValue();
            if (selectedDate != null) {
                startDatePicker.setMax(selectedDate.minusDays(1));
                if (startDate != null) {
                    message.setText(
                            "Selected period:\nFrom " + startDate.toString()
                                    + " to " + selectedDate.toString());
                } else {
                    message.setText("Select the starting date");
                }
            } else {
                startDatePicker.setMax(null);
                if (startDate != null) {
                    message.setText("Select the ending date");
                } else {
                    message.setText("No date is selected");
                }
            }
        });

        startDatePicker.setId("start-picker");
        endDatePicker.setId("end-picker");
        addCard("Two linked date pickers", startDatePicker, endDatePicker,
                message);

    }

    private void createDatePickerWithOpenedChangeListener() {
        Div message = createMessageDiv("picker-with-opened-change-message");

        DatePicker datePicker = new DatePicker();
        datePicker.setId("picker-with-opened-change");

        datePicker.addOpenedChangeListener(event -> {
            var text = event.isOpened() ? "date picker was opened"
                    : "date picker was closed";
            message.setText(text);
        });

        addCard("DatePicker with a opened change listener", datePicker,
                message);
    }

    /**
     * Additional code used in the demo
     */
    private void updateMessage(Div message, DatePicker datePicker) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            message.setText("Day: " + selectedDate.getDayOfMonth() + "\nMonth: "
                    + selectedDate.getMonthValue() + "\nYear: "
                    + selectedDate.getYear() + "\nLocale: "
                    + datePicker.getLocale());
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

    private void addCard(String title, Component... components) {
        addCard(title, null, components);
    }

    private void addCard(String title, String description,
            Component... components) {
        if (description != null) {
            title = title + ": " + description;
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
