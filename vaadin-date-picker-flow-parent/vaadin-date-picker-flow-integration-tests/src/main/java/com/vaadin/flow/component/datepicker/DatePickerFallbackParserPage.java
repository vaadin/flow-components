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

import elemental.json.Json;
import elemental.json.JsonObject;

@Route("vaadin-date-picker/fallback-parser")
public class DatePickerFallbackParserPage extends Div {
    public DatePickerFallbackParserPage() {
        DatePicker datePicker = new DatePicker();
        datePicker.setFallbackParser((s) -> {
            if (s.equals("newyear")) {
                return Result.ok(LocalDate.of(2024, 1, 1));
            } else {
                return Result.error("Invalid date format");
            }
        });

        Div valueChangeLog = new Div();
        valueChangeLog.setId("value-change-log");

        datePicker.addValueChangeListener(event -> {
            JsonObject record = Json.createObject();
            record.put("eventFromClient", event.isFromClient());
            record.put("eventOldValue", formatDate(event.getOldValue()));
            record.put("eventNewValue", formatDate(event.getValue()));
            record.put("componentValue", formatDate(datePicker.getValue()));
            record.put("componentValueProperty",
                    datePicker.getElement().getProperty("value", ""));

            valueChangeLog.add(new Div(record.toString()));
        });

        NativeButton clearValueChangeLog = new NativeButton(
                "Clear value change log", event -> {
                    valueChangeLog.removeAll();
                });
        clearValueChangeLog.setId("clear-value-change-log");

        add(datePicker, valueChangeLog, clearValueChangeLog);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }
}
