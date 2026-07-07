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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.List;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

/**
 * Test page for a date picker that combines the two disabled-dates APIs: a
 * fixed list of disabled dates and a per-date provider. January 2023 is shown,
 * with January 10th and 20th disabled through the fixed list, and the 15th of
 * every month disabled through the provider.
 */
@Route("vaadin-date-picker/date-picker-disabled-dates")
public class DatePickerDisabledDatesPage extends Div {

    public static final int PROVIDER_DISABLED_DAY = 15;
    public static final List<LocalDate> FIXED_DISABLED_DATES = List
            .of(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 20));

    public DatePickerDisabledDatesPage() {
        DatePicker datePicker = new DatePicker();
        datePicker.setId("date-picker");
        datePicker.setI18n(new DatePickerI18n()
                .setDateDisabledErrorMessage("Date is not available"));
        // Show January 2023 when the overlay opens.
        datePicker.setValue(LocalDate.of(2023, 1, 5));

        // Fixed list of already-booked dates.
        datePicker.setDisabledDates(FIXED_DISABLED_DATES);
        // Provider: the 15th of every month.
        datePicker.setDisabledDatesProvider(date -> {
            System.out.println("Checking " + date);
            return date.getDayOfMonth() == PROVIDER_DISABLED_DAY;
        });

        add(new Paragraph(
                "Disabled: January 10 and 20, 2023 (fixed list); the 15th of every month (provider)."),
                datePicker);
    }
}
