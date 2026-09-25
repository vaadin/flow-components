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
package com.vaadin.flow.component.datetimepicker;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("repro-3309")
public class Repro3309View extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();
        Map<String, List<String>> params = event.getLocation()
                .getQueryParameters().getParameters();

        Locale uiLocale = UI.getCurrent().getLocale();
        add(new Div("UI locale: " + uiLocale + " -> tag " + tag(uiLocale)
                + ", JVM default: " + Locale.getDefault()));

        // Reporter's case: no locale set, uses UI locale
        addRow("default", null);

        if (params.containsKey("lang")) {
            String lang = params.get("lang").get(0);
            String country = params.getOrDefault("country", List.of("")).get(0);
            addRow("param", new Locale(lang, country));
            return;
        }

        // Control
        addRow("uk", Locale.UK);
        // Candidates whose language/country produce a non-BCP47 tag
        addRow("underscore", new Locale("en_GB"));
        addRow("hyphen", new Locale("en-GB"));
        addRow("single-letter", new Locale("e"));
        addRow("country-underscore", new Locale("en", "GB_x"));
        addRow("digits", new Locale("en1"));
        addRow("iw", new Locale("iw", "IL"));

        NativeButton setUiLocale = new NativeButton("UI.setLocale(en_GB)",
                e -> UI.getCurrent().setLocale(new Locale("en_GB")));
        setUiLocale.setId("set-ui-locale");
        add(setUiLocale);
    }

    private void addRow(String id, Locale locale) {
        DateTimePicker picker = new DateTimePicker();
        picker.setId(id);
        if (locale != null) {
            picker.setLocale(locale);
        }
        String label = locale == null ? "(none)"
                : locale + " -> tag " + tag(locale);
        add(new Div(new Span(id + ": " + label + " "), picker));
    }

    // Same tag building as TimePicker.executeLocaleUpdate
    private static String tag(Locale locale) {
        StringBuilder sb = new StringBuilder(locale.getLanguage());
        if (!locale.getCountry().isEmpty()) {
            sb.append("-").append(locale.getCountry());
        }
        return sb.toString();
    }
}
