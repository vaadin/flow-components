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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/i18n")
public class DatePickerI18nPage extends Div {

    public DatePickerI18nPage() {
        DatePicker datePicker = new DatePicker();
        // Set initial value to January to test month name
        datePicker.setValue(LocalDate.of(2021, 1, 15));

        NativeButton setI18n = new NativeButton("Set i18n", e -> {
            DatePickerI18n i18n = new DatePickerI18n();
            i18n.setToday("Custom today");
            i18n.setCancel("Custom cancel");
            i18n.setMonthNames(List.of("Custom January", "Custom February",
                    "Custom March", "Custom April", "Custom May", "Custom June",
                    "Custom July", "Custom August", "Custom September",
                    "Custom October", "Custom November", "Custom December"));
            datePicker.setI18n(i18n);
        });
        setI18n.setId("set-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty i18n",
                e -> datePicker.setI18n(new DatePickerI18n()));
        setEmptyI18n.setId("set-empty-i18n");

        add(setI18n, setEmptyI18n, datePicker);
    }
}
