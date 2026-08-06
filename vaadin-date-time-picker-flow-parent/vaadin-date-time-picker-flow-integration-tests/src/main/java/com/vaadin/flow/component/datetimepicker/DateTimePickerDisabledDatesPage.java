/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

/**
 * Test page for a date-time picker that combines the two disabled-dates APIs on
 * its date part: a fixed list of disabled dates and a per-date provider.
 * January 2023 is shown, with January 10th and 20th disabled through the fixed
 * list, and the 15th of every month disabled through the provider.
 */
@Route("vaadin-date-time-picker/date-time-picker-disabled-dates")
public class DateTimePickerDisabledDatesPage extends Div {

    public static final int PROVIDER_DISABLED_DAY = 15;
    public static final List<LocalDate> FIXED_DISABLED_DATES = List
            .of(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 20));

    public DateTimePickerDisabledDatesPage() {
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setId("date-time-picker");
        // Show January 2023 when the date overlay opens.
        dateTimePicker.setValue(LocalDateTime.of(2023, 1, 5, 12, 0));

        // Fixed list of already-booked dates.
        dateTimePicker.setDisabledDates(FIXED_DISABLED_DATES);
        // Provider: the 15th of every month.
        dateTimePicker.setDisabledDatesProvider(
                date -> date.getDayOfMonth() == PROVIDER_DISABLED_DAY);

        add(new Paragraph(
                "Disabled dates in the date part: January 10 and 20, 2023 (fixed list); the 15th of every month (provider)."),
                dateTimePicker);
    }
}
