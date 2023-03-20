/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
