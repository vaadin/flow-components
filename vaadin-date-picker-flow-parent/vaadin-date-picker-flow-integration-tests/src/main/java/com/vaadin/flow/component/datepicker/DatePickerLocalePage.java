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
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

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
