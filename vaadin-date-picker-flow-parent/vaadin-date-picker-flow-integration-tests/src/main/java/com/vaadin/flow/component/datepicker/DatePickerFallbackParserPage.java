/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
            } else if (s.equals("exception")) {
                throw new RuntimeException("Exception in fallback parser");
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

        NativeButton setValue = new NativeButton("Set value", event -> {
            datePicker.setValue(LocalDate.now());
        });

        add(datePicker, valueChangeLog, clearValueChangeLog, setValue);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }
}
