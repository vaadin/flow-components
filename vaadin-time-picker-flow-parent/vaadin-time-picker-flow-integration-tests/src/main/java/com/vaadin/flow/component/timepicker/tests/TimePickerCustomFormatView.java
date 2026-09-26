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
package com.vaadin.flow.component.timepicker.tests;

import java.time.LocalTime;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.TimePicker.TimePickerI18n;
import com.vaadin.flow.router.Route;

@Route("vaadin-time-picker/custom-format")
public class TimePickerCustomFormatView extends Div {
    public static final String SERVER_VALUE = "server-value";
    public static final String CLEAR_FORMATS_BUTTON = "clear-formats-button";

    public TimePickerCustomFormatView() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLocale(Locale.US);
        timePicker.setI18n(new TimePickerI18n().setTimeFormats("HH.mm", "Hmm"));
        timePicker.setValue(LocalTime.of(13, 5));

        Span serverValue = new Span(timePicker.getValue().toString());
        serverValue.setId(SERVER_VALUE);
        timePicker.addValueChangeListener(event -> serverValue.setText(
                event.getValue() == null ? "" : event.getValue().toString()));

        NativeButton clearFormatsButton = new NativeButton("Clear formats",
                event -> timePicker.setI18n(new TimePickerI18n()));
        clearFormatsButton.setId(CLEAR_FORMATS_BUTTON);

        add(timePicker, serverValue, clearFormatsButton);
    }
}
