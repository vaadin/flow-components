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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

@Route("vaadin-date-picker/custom-date-format")
public class CustomDateFormatPage extends VerticalLayout {
    public CustomDateFormatPage() {
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now(ZoneId.systemDefault()));
        Span dateValue = new Span();

        Button format1Button = new Button("dd.MM.yyyy", event -> {
            datePicker.setI18n(new DatePicker.DatePickerI18n()
                    .setDateFormat("dd.MM.yyyy"));
        });
        Button format2Button = new Button("MM.dd.yyyy", event -> {
            datePicker.setI18n(new DatePicker.DatePickerI18n()
                    .setDateFormat("MM.dd.yyyy"));
        });
        Button format3Button = new Button("MM§yyyy§dd", event -> {
            datePicker.setI18n(new DatePicker.DatePickerI18n()
                    .setDateFormat("MM§yyyy§dd"));
        });
        Button format4Button = new Button("MM§yyyy§dd, MM.dd.yyyy", event -> {
            datePicker.setI18n(new DatePicker.DatePickerI18n()
                    .setDateFormats("MM§yyyy§dd", "MM.dd.yyyy"));
        });

        Button removeFormat = new Button("set null", event -> {
            DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
            i18n.setDateFormats("MM§yyyy§dd", "MM.dd.yyyy");
            i18n.setDateFormats(null);

            datePicker.setI18n(i18n);
        });
        Button setLocale = new Button("set local to germany", event -> {
            datePicker.setLocale(Locale.GERMANY);
        });

        datePicker.addChangeListener(event -> {
            if (datePicker.getValue() != null) {
                dateValue.setText(
                        "Value change: " + datePicker.getValue().toString());
            } else {
                dateValue.setText("Invalid date");
            }
        });

        add(datePicker,
                new HorizontalLayout(format1Button, format2Button,
                        format3Button, format4Button, removeFormat, setLocale),
                dateValue);
    }
}
