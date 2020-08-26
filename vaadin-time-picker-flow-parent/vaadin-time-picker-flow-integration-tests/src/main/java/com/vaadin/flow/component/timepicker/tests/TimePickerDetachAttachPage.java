/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

/**
 * Test view for attaching / detaching {@link TimePicker}.
 */
@Route("vaadin-time-picker/time-picker-detach-attach")
public class TimePickerDetachAttachPage extends Div {

    /**
     * Constructs a basic layout with a time picker.
     */
    public TimePickerDetachAttachPage() {
        TimePicker timePicker = new TimePicker();
        timePicker.setRequiredIndicatorVisible(true);
        timePicker.setId("time-picker");
        add(timePicker);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (timePicker.getParent().isPresent()) {
                remove(timePicker);
            } else {
                add(timePicker);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);
    }
}
