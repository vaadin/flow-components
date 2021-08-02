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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route("vaadin-date-picker/date-picker-format")
public class DatePickerFormatPage extends VerticalLayout {
    public static final String PRIMARY_FORMAT = "PRIMARY_FORMAT";
    public static final String MULTIPLE_FORMAT = "MULTIPLE_FORMAT";
    public static final String FORMAT_AND_SET_LOCALE = "FORMAT_AND_SET_LOCALE";
    public static final String SET_FORMAT_TO_NULL_BUTTON = "SET_FORMAT_TO_NULL_BUTTON";
    public static final String NULL_FORMAT = "NULL_FORMAT";
    public static final String CHANGE_TO_FORMAT_BTN = "CHANGE_TO_FORMAT_BTN";
    public static final String CHANGE_BETWEEN_FORMATS = "CHANGE_BETWEEN_FORMATS";
    public static final String CHANGE_BETWEEN_FORMATS_OUTPUT = "CHANGE_BETWEEN_FORMATS_OUTPUT";
    public static final String CLIENT_CHANGE = "CLIENT_CHANGE";
    public static final String SERVER_CHANGE = "SERVER_CHANGE";
    public static final String CHANGE_DATE_BTN = "CHANGE_DATE_BTN";
    public static final String INVALID_CLIENT_DATE = "INVALID_CLIENT_DATE";
    public static final String CHANGE_FROM_SET_LOCALE = "CHANGE_FROM_SET_LOCALE";
    public static final String CLIENT_CHANGE_OUTPUT = "CLIENT_CHANGE_OUTPUT";
    public static final String PARSING_SINGLE_FORMAT = "PARSING_SINGLE_FORMAT";
    public static final String PARSING_SINGLE_FORMAT_OUTPUT = "PARSING_SINGLE_FORMAT_OUTPUT";
    public static final String FORMAT_AND_SET_LOCALE_OUTPUT = "FORMAT_AND_SET_LOCALE_OUTPUT";
    public static final String NULL_FORMAT_OUTPUT = "NULL_FORMAT_OUTPUT";

    public static final LocalDate may13 = LocalDate.of(2018, Month.MAY, 13);

    public DatePickerFormatPage() {
        setupPrimaryFormat();
        setupMultipleFormats();
        setupChangeBetweenFormats();
        setupNullFormat();
        setupFormatAndSetLocale();
        setupSetFormatAfterSetLocale();
        setupParsingWithSingleFormat();
        setupClientSideChange();
        setupServerSideChange();
        setupInvalidClientSideDate();
    }

    public void setupPrimaryFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("yyyy-MM-dd");
        datePicker.setI18n(i18n);

        datePicker.setId(PRIMARY_FORMAT);
        add(datePicker);
    }

    private void setupMultipleFormats() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy.MM.dd", "yyyy-MM-dd");
        datePicker.setI18n(i18n);

        datePicker.setId(MULTIPLE_FORMAT);
        add(datePicker);
    }

    private void setupChangeBetweenFormats() {
        DatePicker datePicker = new DatePicker(may13);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("dd.yyyy.MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change format", clickEvent -> {
            datePicker.setI18n(new DatePickerI18n().setDateFormat("M/d/yy"));
        });

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        btn.setId(CHANGE_TO_FORMAT_BTN);
        datePicker.setId(CHANGE_BETWEEN_FORMATS);
        output.setId(CHANGE_BETWEEN_FORMATS_OUTPUT);
        add(datePicker, btn, output);
    }

    private void setupNullFormat() {
        DatePicker datePicker = new DatePicker(may13);
        datePicker.setLocale(Locale.GERMANY);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("dd yyyy MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("set format to null", clickEvent -> {
            datePicker.setI18n(new DatePickerI18n().setDateFormat(null));
        });

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        btn.setId(SET_FORMAT_TO_NULL_BUTTON);
        datePicker.setId(NULL_FORMAT);
        output.setId(NULL_FORMAT_OUTPUT);
        add(datePicker, btn, output);
    }

    private void setupFormatAndSetLocale() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        datePicker.setLocale(Locale.GERMANY); // should have no effect

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        datePicker.setId(FORMAT_AND_SET_LOCALE);
        output.setId(FORMAT_AND_SET_LOCALE_OUTPUT);
        add(datePicker, output);
    }

    private void setupSetFormatAfterSetLocale() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        datePicker.setLocale(Locale.GERMANY);

        i18n.setDateFormats("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        datePicker.setId(CHANGE_FROM_SET_LOCALE);
        add(datePicker);
    }

    private void setupParsingWithSingleFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        datePicker.setId(PARSING_SINGLE_FORMAT);
        output.setId(PARSING_SINGLE_FORMAT_OUTPUT);
        add(datePicker, output);
    }

    private void setupClientSideChange() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd", "dd.MM.yyyy", "dd-MM-yyyy");
        datePicker.setI18n(i18n);

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        datePicker.setId(CLIENT_CHANGE);
        output.setId(CLIENT_CHANGE_OUTPUT);
        add(datePicker, output);
    }

    private void setupServerSideChange() {
        DatePicker datePicker = new DatePicker();
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("d.M.yyyy", "dd-MM-yyyy");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change date", clickEvent -> {
            datePicker.setValue(may13);
        });

        btn.setId(CHANGE_DATE_BTN);
        datePicker.setId(SERVER_CHANGE);
        add(datePicker, btn);
    }

    private void setupInvalidClientSideDate() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd", "dd.MM.yyyy", "dd-MM-yyyy");
        datePicker.setI18n(i18n);

        datePicker.setId(INVALID_CLIENT_DATE);
        add(datePicker);
    }
}
