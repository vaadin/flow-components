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
    public static final String PRIMARY_FORMAT_DATE_PICKER = "PRIMARY_FORMAT_DATE_PICKER";
    public static final String MULTIPLE_FORMAT_DATE_PICKER = "MULTIPLE_FORMAT_DATE_PICKER";
    public static final String FORMAT_AND_SET_LOCALE = "FORMAT_AND_SET_LOCALE";
    public static final String REMOVE_DATE_FORMAT_BUTTON = "REMOVE_DATE_FORMAT_BUTTON";
    public static final String REMOVE_DATE_FORMAT = "REMOVE_DATE_FORMAT";
    public static final String CHANGE_BETWEEN_FORMATS_BUTTON = "CHANGE_BETWEEN_FORMATS_BUTTON";
    public static final String CHANGE_BETWEEN_FORMATS = "CHANGE_BETWEEN_FORMATS";
    public static final String CHANGE_BETWEEN_FORMATS_OUTPUT = "CHANGE_BETWEEN_FORMATS_OUTPUT";
    public static final String FALLBACK_PARSERS = "FALLBACK_PARSERS";
    public static final String SERVER_CHANGE = "SERVER_CHANGE";
    public static final String CHANGE_DATE_BUTTON = "CHANGE_DATE_BUTTON";
    public static final String INVALID_CLIENT_DATE = "INVALID_CLIENT_DATE";
    public static final String DATE_FORMAT_AFTER_SET_LOCALE = "DATE_FORMAT_AFTER_SET_LOCALE";
    public static final String FALLBACK_PARSERS_OUTPUT = "FALLBACK_PARSERS_OUTPUT";
    public static final String PARSING_SINGLE_FORMAT = "PARSING_SINGLE_FORMAT";
    public static final String PARSING_SINGLE_FORMAT_OUTPUT = "PARSING_SINGLE_FORMAT_OUTPUT";
    public static final String FORMAT_AND_SET_LOCALE_OUTPUT = "FORMAT_AND_SET_LOCALE_OUTPUT";
    public static final String REMOVE_DATE_FORMAT_OUTPUT = "NULL_FORMAT_OUTPUT";
    public static final String INVALID_CLIENT_DATE_OUTPUT = "INVALID_CLIENT_DATE_OUTPUT";

    public static final LocalDate may13 = LocalDate.of(2018, Month.MAY, 13);

    public DatePickerFormatPage() {
        setupPrimaryFormat();
        setupMultipleFormats();
        setupChangeBetweenFormats();
        setupRemoveDateFormat();
        setupFormatAndSetLocale();
        setupSetFormatAfterSetLocale();
        setupParsingWithSingleFormat();
        setupFallbackDateFormatParsers();
        setupServerSideDatePickerValueChange();
        setupInvalidClientSideDate();
    }

    public void setupPrimaryFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("yyyy-MM-dd");
        datePicker.setI18n(i18n);

        datePicker.setId(PRIMARY_FORMAT_DATE_PICKER);
        add(datePicker);
    }

    private void setupMultipleFormats() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy.MM.dd", "yyyy-MM-dd");
        datePicker.setI18n(i18n);

        datePicker.setId(MULTIPLE_FORMAT_DATE_PICKER);
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
            output.setText(
                    datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        btn.setId(CHANGE_BETWEEN_FORMATS_BUTTON);
        datePicker.setId(CHANGE_BETWEEN_FORMATS);
        output.setId(CHANGE_BETWEEN_FORMATS_OUTPUT);
        add(datePicker, btn, output);
    }

    private void setupRemoveDateFormat() {
        DatePicker datePicker = new DatePicker(may13);
        datePicker.setLocale(Locale.GERMANY);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("dd yyyy MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("set format to null",
                clickEvent -> {
                    datePicker
                            .setI18n(new DatePickerI18n().setDateFormat(null));
                });

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(
                    datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        btn.setId(REMOVE_DATE_FORMAT_BUTTON);
        datePicker.setId(REMOVE_DATE_FORMAT);
        output.setId(REMOVE_DATE_FORMAT_OUTPUT);
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
            output.setText(
                    datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
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

        datePicker.setId(DATE_FORMAT_AFTER_SET_LOCALE);
        add(datePicker);
    }

    private void setupParsingWithSingleFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(
                    datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        datePicker.setId(PARSING_SINGLE_FORMAT);
        output.setId(PARSING_SINGLE_FORMAT_OUTPUT);
        add(datePicker, output);
    }

    private void setupFallbackDateFormatParsers() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd", "dd.MM.yyyy", "dd-MM-yyyy");
        datePicker.setI18n(i18n);

        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            output.setText(
                    datePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        });

        datePicker.setId(FALLBACK_PARSERS);
        output.setId(FALLBACK_PARSERS_OUTPUT);
        add(datePicker, output);
    }

    private void setupServerSideDatePickerValueChange() {
        DatePicker datePicker = new DatePicker();
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("d.M.yyyy", "dd-MM-yyyy");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change date", clickEvent -> {
            datePicker.setValue(may13);
        });

        btn.setId(CHANGE_DATE_BUTTON);
        datePicker.setId(SERVER_CHANGE);
        add(datePicker, btn);
    }

    private void setupInvalidClientSideDate() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy/MM/dd", "dd.MM.yyyy", "dd-MM-yyyy");
        datePicker.setI18n(i18n);

        Span output = new Span();
        datePicker.setId(INVALID_CLIENT_DATE);
        datePicker.addValueChangeListener(event -> {
            if (datePicker.getValue() != null) {
                output.setText(datePicker.getValue()
                        .format(DateTimeFormatter.ISO_DATE));
            }
        });

        output.setId(INVALID_CLIENT_DATE_OUTPUT);
        add(datePicker, output);
    }
}
