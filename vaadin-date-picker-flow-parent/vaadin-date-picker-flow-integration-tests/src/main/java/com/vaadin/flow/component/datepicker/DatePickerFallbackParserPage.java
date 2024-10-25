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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/fallback-parser")
public class DatePickerFallbackParserPage extends Div {
    public DatePickerFallbackParserPage() {
        DatePicker datePicker = new DatePicker();
        datePicker.setFallbackParser((s) -> {
            if (s.equals("tomorrow")) {
                return Result.ok(LocalDate.now().plusDays(1));
            } else {
                return Result.error("Invalid date format");
            }
        });

        Div valueChangeLog = new Div();
        valueChangeLog.setId("value-change-log");

        datePicker.addValueChangeListener(event -> {
            String record = String.join(",",
                    String.valueOf(event.getOldValue()),
                    String.valueOf(event.getValue()),
                    String.valueOf(datePicker.getValue()));
            valueChangeLog.add(new Div(record));
        });

        NativeButton clearValueChangeLog = new NativeButton("Clear value change log", event -> {
            valueChangeLog.removeAll();
        });
        clearValueChangeLog.setId("clear-value-change-log");

        add(datePicker, valueChangeLog, clearValueChangeLog);
    }
}
