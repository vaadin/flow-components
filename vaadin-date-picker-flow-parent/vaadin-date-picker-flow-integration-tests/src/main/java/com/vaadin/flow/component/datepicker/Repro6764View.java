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
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/6764
 *
 * A DatePicker with a locale whose conventional date pattern is zero-padded
 * (e.g. de-DE: dd.MM.yyyy) renders the value unpadded (1.3.2024), because the
 * connector derives the pattern from
 * {@code Date.prototype.toLocaleDateString(locale)}, which uses the numeric
 * (unpadded) skeleton rather than the locale's short/medium pattern.
 */
@Route("repro-6764")
public class Repro6764View extends Div {

    private static final LocalDate VALUE = LocalDate.of(2024, 3, 1);

    private static final List<Locale> LOCALES = Arrays.asList(Locale.GERMANY,
            Locale.GERMAN, new Locale("de", "AT"), new Locale("de", "CH"),
            Locale.FRANCE, new Locale("pl", "PL"), Locale.UK, Locale.US,
            new Locale("es", "ES"), new Locale("fi", "FI"));

    public Repro6764View() {
        add(new Span("Value set from server: " + VALUE
                + " — each row shows what the DatePicker input displays vs. what "
                + "java.time formats for the same locale."));

        for (Locale locale : LOCALES) {
            add(row(locale));
        }

        // Control: same German locale, but with an explicit date format.
        Div control = new Div();
        DatePicker withCustomFormat = new DatePicker();
        withCustomFormat.setId("picker-custom-format");
        withCustomFormat.setLocale(Locale.GERMANY);
        withCustomFormat.setI18n(
                new DatePicker.DatePickerI18n().setDateFormat("dd.MM.yyyy"));
        withCustomFormat.setValue(VALUE);
        control.add(new Span("control: de-DE + setDateFormat(\"dd.MM.yyyy\") "),
                withCustomFormat);
        add(control);

        // Lets the value be re-applied without restarting the server, to check
        // that the format is not a one-off of the initial rendering.
        NativeButton resetValues = new NativeButton("Re-set all values", e -> {
            getChildren().flatMap(c -> c.getChildren())
                    .filter(DatePicker.class::isInstance)
                    .map(DatePicker.class::cast).forEach(picker -> {
                        picker.setValue(null);
                        picker.setValue(VALUE);
                    });
        });
        resetValues.setId("reset-values");
        add(resetValues);
    }

    private Div row(Locale locale) {
        DatePicker picker = new DatePicker();
        picker.setId("picker-" + locale.toLanguageTag());
        picker.setLocale(locale);
        picker.setValue(VALUE);

        String javaShort = VALUE.format(DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT).withLocale(locale));
        String javaMedium = VALUE.format(DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale));

        Span expected = new Span(
                "java SHORT: " + javaShort + " | java MEDIUM: " + javaMedium);
        expected.setId("java-" + locale.toLanguageTag());

        Div row = new Div();
        row.getStyle().set("display", "flex").set("gap", "1em")
                .set("align-items", "baseline");
        row.add(new Span(locale.toLanguageTag()), picker, expected);
        return row;
    }
}
