/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.time.LocalDateTime;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/date-time-picker")
public class GridDateTimePickerPage extends Div {

    public GridDateTimePickerPage() {
        var grid = new Grid<Meeting>();
        grid.setItems(
                new Meeting("Meeting 1", LocalDateTime.of(2000, 6, 13, 12, 0)));

        grid.addColumn(Meeting::getName).setHeader("Name");

        grid.addComponentColumn(meeting -> {
            DateTimePicker dateTime = new DateTimePicker();
            DatePickerI18n i18n = new DatePickerI18n();
            i18n.setDateFormat("dd.MM.yyyy");
            dateTime.setDatePickerI18n(i18n);
            dateTime.setValue(meeting.getDate());
            return dateTime;
        }).setHeader("Date and time");

        add(grid);
    }

    private class Meeting {
        private String name;
        private LocalDateTime date;

        public Meeting(String name, LocalDateTime date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getDate() {
            return date;
        }
    }

}
