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
