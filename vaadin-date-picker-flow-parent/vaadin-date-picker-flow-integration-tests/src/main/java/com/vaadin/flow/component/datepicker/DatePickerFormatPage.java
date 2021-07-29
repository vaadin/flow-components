/*
 * Copyright 2000-2021 Vaadin Ltd.
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

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

@Route("vaadin-date-picker/date-picker-format")
public class DatePickerFormatPage extends VerticalLayout {
    public static final String WITH_PRIMARY_FORMAT = "WITH_PRIMARY_FORMAT";
    public static final String WITH_MULTIPLE_FORMAT = "WITH_MULTIPLE_FORMAT";
    public static final String WITH_FALLBACK_FORMAT = "WITH_FALLBACK_FORMAT";
    public static final String CHANGE_FORMAT_BUTTON = "CHANGE_FORMAT_BUTTON";
    public static final String WITH_FORMAT_AND_SET_LOCALE = "WITH_FORMAT_AND_SET_LOCALE";
    public static final String SET_FORMAT_TO_NULL_BUTTON = "SET_FORMAT_TO_NULL_BUTTON";
    public static final String WITH_NULL_FORMAT = "WITH_NULL_FORMAT";
    public static final String CHANGE_TO_FORMAT_BTN = "CHANGE_TO_FORMAT_BTN";
    public static final String WITH_CHANGE_BETWEEN_FORMATS = "WITH_CHANGE_BETWEEN_FORMATS";

    public static final LocalDate may13 = LocalDate.of(2018, Month.MAY, 13);


    public DatePickerFormatPage() {
        withPrimaryFormat();
        withMultipleFormats();
        withChangeBetweenFormats();
        withFallBackFormats();
        withFormatAndSetLocale();
        withNullFormat();
    }

    public void withPrimaryFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("yyyy-MM-dd");
        datePicker.setI18n(i18n);

        datePicker.setId(WITH_PRIMARY_FORMAT);
        add(datePicker);
    }

    private void withMultipleFormats() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy.MM.dd", "yyyy-MM-dd");
        datePicker.setI18n(i18n);

        datePicker.setId(WITH_MULTIPLE_FORMAT);
        add(datePicker);
    }


    private void withChangeBetweenFormats() {
        DatePicker datePicker = new DatePicker(may13);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("dd.yyyy.MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change format", clickEvent -> {
            datePicker.setI18n(new DatePickerI18n().setDateFormat("M/d/yy"));
        });

        btn.setId(CHANGE_TO_FORMAT_BTN);
        datePicker.setId(WITH_CHANGE_BETWEEN_FORMATS);
        add(datePicker, btn);
    }

    private void withFallBackFormats() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy-MM-dd");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change format", clickEvent -> {
            i18n.setDateFormats("dd§MM§yyyy", "yyyy-MM-dd");
            datePicker.setI18n(i18n);
        });

        btn.setId(CHANGE_FORMAT_BUTTON);
        datePicker.setId(WITH_FALLBACK_FORMAT);
        add(datePicker, btn);
    }

    private void withFormatAndSetLocale() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        datePicker.setLocale(Locale.GERMANY); // should have no effect

        datePicker.setId(WITH_FORMAT_AND_SET_LOCALE);
        add(datePicker);
    }

    private void withNullFormat() {
        DatePicker datePicker = new DatePicker(may13);
        datePicker.setLocale(Locale.GERMANY);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("dd yyyy MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("set format to null", clickEvent -> {
            datePicker.setI18n(new DatePickerI18n().setDateFormat(null));
        });

        btn.setId(SET_FORMAT_TO_NULL_BUTTON);
        datePicker.setId(WITH_NULL_FORMAT);
        add(datePicker, btn);
    }

}
