
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
 *
 */

package com.vaadin.flow.component.datepicker;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/german-picker-format")
public class DatePickerView extends VerticalLayout {

    private DatePicker datePicker;

    public DatePickerView() {
        datePicker = new DatePicker();
        datePicker.setId("german-picker");
        Locale locale = Locale.GERMAN;
        setLocale(locale);
        LocalDate value = LocalDate.of(22, 2, 14);
        datePicker.setValue(value);
        add(datePicker);
    }

    private void setLocale(Locale locale) {
        String pattern = "dd.MM.yy, EEEE";
        datePicker.setLocale(locale);
        DatePickerI18n datePickerI18n = new DatePickerI18n();
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        datePickerI18n.setMonthNames(Arrays.asList(symbols.getMonths()));
        datePickerI18n.setFirstDayOfWeek(1);
        datePickerI18n.setWeekdays(Arrays.stream(symbols.getWeekdays())
                .filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        datePickerI18n.setWeekdaysShort(Arrays
                .stream(symbols.getShortWeekdays()).filter(s -> !s.isEmpty())
                .collect(Collectors.toList()));
        datePickerI18n.setDateFormat(pattern);
        datePicker.setI18n(datePickerI18n);
    }

}
