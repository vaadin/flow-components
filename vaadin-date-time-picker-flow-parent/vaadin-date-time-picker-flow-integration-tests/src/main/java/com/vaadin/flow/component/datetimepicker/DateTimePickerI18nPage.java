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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-time-picker/i18n")
public class DateTimePickerI18nPage extends Div {

    public DateTimePickerI18nPage() {
        DateTimePicker dateTimePicker = new DateTimePicker();

        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n();
        i18n.setDateLabel("date");
        i18n.setTimeLabel("time");

        NativeButton setI18n = new NativeButton("Set i18n",
                event -> dateTimePicker.setI18n(i18n));
        setI18n.setId("set-i18n");

        NativeButton setDateAriaLabel = new NativeButton("Set date aria label",
                event -> dateTimePicker.setDateAriaLabel("Custom date"));
        setDateAriaLabel.setId("set-date-aria-label");

        NativeButton removeDateAriaLabel = new NativeButton(
                "Remove date aria label",
                event -> dateTimePicker.setDateAriaLabel(null));
        removeDateAriaLabel.setId("remove-date-aria-label");

        NativeButton setTimeAriaLabel = new NativeButton("Set time aria label",
                event -> dateTimePicker.setTimeAriaLabel("Custom time"));
        setTimeAriaLabel.setId("set-time-aria-label");

        NativeButton removeTimeAriaLabel = new NativeButton(
                "Remove time aria label",
                event -> dateTimePicker.setTimeAriaLabel(null));
        removeTimeAriaLabel.setId("remove-time-aria-label");

        add(dateTimePicker, setI18n, setDateAriaLabel, removeDateAriaLabel,
                setTimeAriaLabel, removeTimeAriaLabel);
    }

}
