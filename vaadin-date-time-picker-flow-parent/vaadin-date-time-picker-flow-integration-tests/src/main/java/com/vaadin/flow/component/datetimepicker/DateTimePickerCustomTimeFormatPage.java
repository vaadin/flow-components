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

import java.time.LocalDateTime;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker.TimePickerI18n;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-time-picker/custom-time-format")
public class DateTimePickerCustomTimeFormatPage extends Div {

    public DateTimePickerCustomTimeFormatPage() {
        DateTimePicker picker = new DateTimePicker();
        picker.setLocale(Locale.US);
        picker.setTimePickerI18n(
                new TimePickerI18n().setTimeFormats("HH:mm", "Hmm"));
        picker.setValue(LocalDateTime.of(2026, 9, 26, 13, 5));

        Span serverValue = new Span(picker.getValue().toString());
        serverValue.setId("server-value");
        picker.addValueChangeListener(event -> serverValue.setText(
                event.getValue() == null ? "" : event.getValue().toString()));

        add(picker, serverValue);
    }
}
