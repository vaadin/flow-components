/*
 * Copyright 2000-2022 Vaadin Ltd.
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

/**
 * Test view for attaching / detaching {@link DateTimePicker}.
 */
@Route("vaadin-date-time-picker/date-time-picker-detach-attach")
public class DateTimePickerDetachAttachPage extends Div {

    /**
     * Constructs a basic layout with a date time picker.
     */
    public DateTimePickerDetachAttachPage() {
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setRequiredIndicatorVisible(true);
        dateTimePicker.setId("date-time-picker");
        add(dateTimePicker);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (dateTimePicker.getParent().isPresent()) {
                remove(dateTimePicker);
            } else {
                add(dateTimePicker);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);
    }
}
