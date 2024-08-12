/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/date-picker-custom-format")
public class DatePickerCustomFormatPage extends VerticalLayout {
    public static final String PRIMARY_FORMAT_DATE_PICKER = "PRIMARY_FORMAT_DATE_PICKER";
    public static final String PRIMARY_FORMAT_OUTPUT = "PRIMARY_FORMAT_OUTPUT";
    public static final String MULTIPLE_FORMAT_DATE_PICKER = "MULTIPLE_FORMAT_DATE_PICKER";
    public static final String MULTIPLE_FORMAT_OUTPUT = "MULTIPLE_FORMAT_OUTPUT";
    public static final String CHANGE_BETWEEN_FORMATS_DATE_PICKER = "CHANGE_BETWEEN_FORMATS_DATE_PICKER";
    public static final String CHANGE_BETWEEN_FORMATS_BUTTON = "CHANGE_BETWEEN_FORMATS_BUTTON";
    public static final String CHANGE_BETWEEN_FORMATS_OUTPUT = "CHANGE_BETWEEN_FORMATS_OUTPUT";
    public static final String REMOVE_DATE_FORMAT_DATE_PICKER = "REMOVE_DATE_FORMAT_DATE_PICKER";
    public static final String REMOVE_DATE_FORMAT_BUTTON = "REMOVE_DATE_FORMAT_BUTTON";
    public static final String REMOVE_DATE_FORMAT_OUTPUT = "REMOVE_DATE_FORMAT_OUTPUT";
    public static final String SET_LOCALE_AFTER_FORMAT_DATE_PICKER = "SET_LOCALE_AFTER_FORMAT_DATE_PICKER";
    public static final String SET_LOCALE_AFTER_FORMAT_OUTPUT = "SET_LOCALE_AFTER_FORMAT_OUTPUT";
    public static final String SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER = "SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER";
    public static final String SET_DATE_FORMAT_AFTER_LOCALE_OUTPUT = "SET_DATE_FORMAT_AFTER_LOCALE_OUTPUT";
    public static final String SERVER_SIDE_VALUE_CHANGE_DATE_PICKER = "SERVER_SIDE_VALUE_CHANGE_DATE_PICKER";
    public static final String SERVER_SIDE_VALUE_CHANGE_BUTTON = "SERVER_SIDE_VALUE_CHANGE_BUTTON";
    public static final String CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_DATE_PICKER = "CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_DATE_PICKER";
    public static final String CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_OUTPUT = "CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_OUTPUT";
    public static final String OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_DATE_PICKER = "OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_DATE_PICKER";
    public static final String OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_OUTPUT = "OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_OUTPUT";

    public static final LocalDate may13 = LocalDate.of(2018, Month.MAY, 13);

    public DatePickerCustomFormatPage() {
        setupPrimaryFormat();
        setupMultipleFormats();
        setupChangeBetweenFormats();
        setupRemoveDateFormat();
        setupSetLocaleAfterFormat();
        setupSetFormatAfterLocale();
        setupServerSideValueChange();
        setupCustomReferenceDateAndFormatOptions();
        setupOldReferenceDateWithShortFormat();
    }

    public void setupPrimaryFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("yyyy-MM-dd");
        datePicker.setI18n(i18n);

        Span output = createOutputSpan(datePicker);
        datePicker.setId(PRIMARY_FORMAT_DATE_PICKER);
        output.setId(PRIMARY_FORMAT_OUTPUT);
        add(datePicker, output);
    }

    private void setupMultipleFormats() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormats("yyyy-MM-dd", "dd.MM.yyyy", "MM/dd/yyyy");
        datePicker.setI18n(i18n);

        Span output = createOutputSpan(datePicker);
        datePicker.setId(MULTIPLE_FORMAT_DATE_PICKER);
        output.setId(MULTIPLE_FORMAT_OUTPUT);
        add(datePicker, output);
    }

    private void setupChangeBetweenFormats() {
        DatePicker datePicker = new DatePicker(may13);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("dd.yyyy.MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change format", clickEvent -> {
            datePicker.setI18n(new DatePickerI18n().setDateFormat("M/d/yy"));
        });

        Span output = createOutputSpan(datePicker);
        btn.setId(CHANGE_BETWEEN_FORMATS_BUTTON);
        datePicker.setId(CHANGE_BETWEEN_FORMATS_DATE_PICKER);
        output.setId(CHANGE_BETWEEN_FORMATS_OUTPUT);
        add(datePicker, btn, output);
    }

    private void setupRemoveDateFormat() {
        DatePicker datePicker = new DatePicker(may13);
        datePicker.setLocale(Locale.GERMANY);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("dd yyyy MM");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("set format to null",
                clickEvent -> {
                    datePicker
                            .setI18n(new DatePickerI18n().setDateFormat(null));
                });

        Span output = createOutputSpan(datePicker);
        btn.setId(REMOVE_DATE_FORMAT_BUTTON);
        datePicker.setId(REMOVE_DATE_FORMAT_DATE_PICKER);
        output.setId(REMOVE_DATE_FORMAT_OUTPUT);
        add(datePicker, btn, output);
    }

    private void setupSetLocaleAfterFormat() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        datePicker.setLocale(Locale.GERMANY); // should have no effect

        Span output = createOutputSpan(datePicker);
        datePicker.setId(SET_LOCALE_AFTER_FORMAT_DATE_PICKER);
        output.setId(SET_LOCALE_AFTER_FORMAT_OUTPUT);
        add(datePicker, output);
    }

    private void setupSetFormatAfterLocale() {
        DatePicker datePicker = new DatePicker(may13);
        DatePickerI18n i18n = new DatePickerI18n();
        datePicker.setLocale(Locale.GERMANY);

        i18n.setDateFormat("yyyy/MM/dd");
        datePicker.setI18n(i18n);

        Span output = createOutputSpan(datePicker);
        datePicker.setId(SET_DATE_FORMAT_AFTER_LOCALE_DATE_PICKER);
        output.setId(SET_DATE_FORMAT_AFTER_LOCALE_OUTPUT);
        add(datePicker, output);
    }

    private void setupServerSideValueChange() {
        DatePicker datePicker = new DatePicker();
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setDateFormat("d.M.yyyy");
        datePicker.setI18n(i18n);

        NativeButton btn = new NativeButton("change date", clickEvent -> {
            datePicker.setValue(may13);
        });

        btn.setId(SERVER_SIDE_VALUE_CHANGE_BUTTON);
        datePicker.setId(SERVER_SIDE_VALUE_CHANGE_DATE_PICKER);
        add(datePicker, btn);
    }

    private void setupCustomReferenceDateAndFormatOptions() {
        DatePicker datePicker = new DatePicker(may13);

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setReferenceDate(LocalDate.of(1980, 2, 2));
        datePicker.setI18n(i18n);

        NativeButton setShortFormat = new NativeButton("Set short format",
                clickEvent -> {
                    datePicker.setI18n(
                            datePicker.getI18n().setDateFormat("yy-MM-dd"));
                });
        setShortFormat.setId("set-short-format");

        NativeButton setLongFormat = new NativeButton("Set long format",
                clickEvent -> {
                    datePicker.setI18n(
                            datePicker.getI18n().setDateFormat("yyyy-MM-dd"));
                });
        setLongFormat.setId("set-long-format");

        NativeButton setMultipleFormats = new NativeButton(
                "Set multiple formats", clickEvent -> {
                    datePicker.setI18n(datePicker.getI18n()
                            .setDateFormats("yy-MM-dd", "yyyy-MM-dd"));
                });
        setMultipleFormats.setId("set-multiple-formats");

        Span output = createOutputSpan(datePicker);
        datePicker.setId(CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_DATE_PICKER);
        output.setId(CUSTOM_REFERENCE_DATE_AND_FORMAT_OPTIONS_OUTPUT);
        add(datePicker,
                new Div(setShortFormat, setLongFormat, setMultipleFormats),
                output);
    }

    private void setupOldReferenceDateWithShortFormat() {
        LocalDate today = LocalDate.now();

        DatePicker datePicker = new DatePicker(today);
        datePicker.setId(OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_DATE_PICKER);
        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setReferenceDate(today.minusYears(100));
        i18n.setDateFormat("yy-MM-dd");
        datePicker.setI18n(i18n);

        Span output = new Span();
        datePicker.addOpenedChangeListener(event -> {
            LocalDate newValue = datePicker.getValue();
            if (newValue != null) {
                output.setText(newValue.format(DateTimeFormatter.ISO_DATE));
            } else {
                output.setText("");
            }
        });
        output.setId(OLD_REFERENCE_DATE_WITH_SHORT_FORMAT_OUTPUT);

        add(datePicker, output);
    }

    private static Span createOutputSpan(DatePicker datePicker) {
        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            LocalDate newValue = datePicker.getValue();
            if (newValue != null) {
                output.setText(newValue.format(DateTimeFormatter.ISO_DATE));
            } else {
                output.setText("");
            }
        });
        return output;
    }
}
