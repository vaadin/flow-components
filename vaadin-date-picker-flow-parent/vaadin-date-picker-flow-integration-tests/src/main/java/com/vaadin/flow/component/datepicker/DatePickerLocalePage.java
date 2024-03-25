/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.Locale;

@Route("vaadin-date-picker/date-picker-locale")
public class DatePickerLocalePage extends Div {
    public DatePickerLocalePage() {
        DatePicker datePicker = new DatePicker();
        datePicker.setId("picker");

        Input localeInput = new Input();
        localeInput.setId("locale-input");
        localeInput.setPlaceholder("Enter locale string");

        NativeButton applyLocale = new NativeButton("Apply locale", e -> {
            String localeString = localeInput.getValue();
            String[] localeParts = localeString.split("_");
            Locale locale = null;
            if (localeParts.length == 1) {
                locale = new Locale(localeParts[0]);
            }
            if (localeParts.length == 2) {
                locale = new Locale(localeParts[0], localeParts[1]);
            }
            if (localeParts.length == 3) {
                locale = new Locale(localeParts[0], localeParts[1],
                        localeParts[2]);
            }

            datePicker.setLocale(locale);
        });
        applyLocale.setId("apply-locale");

        NativeButton applyCustomReferenceDate = new NativeButton(
                "Apply custom reference date", e -> {
                    DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
                    i18n.setReferenceDate(LocalDate.of(2000, 1, 1));
                    datePicker.setI18n(i18n);
                });
        applyCustomReferenceDate.setId("apply-custom-reference-date");

        add(datePicker, localeInput, applyLocale, applyCustomReferenceDate);
    }
}
