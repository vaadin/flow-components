/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-time-picker/helper-text-component")
public class DateTimePickerHelpersPage extends Div {

    public DateTimePickerHelpersPage() {
        helperText();
        helperComponent();
    }

    private void helperText() {
        DateTimePicker dateTimePickerHelperText = new DateTimePicker();
        dateTimePickerHelperText.setHelperText("Helper text");
        dateTimePickerHelperText.setId("dtp-helper-text");

        NativeButton clearHelper = new NativeButton("Remove helper text", e -> {
            dateTimePickerHelperText.setHelperText(null);
        });
        clearHelper.setId("button-clear-helper-text");

        add(dateTimePickerHelperText, clearHelper);
    }

    private void helperComponent() {
        DateTimePicker dateTimePickerHelperComponent = new DateTimePicker(
                "Arrival time");
        dateTimePickerHelperComponent.setId("dtp-helper-component");
        Span span = new Span("Select your arrival time");
        span.setId("helper-component");
        dateTimePickerHelperComponent.setHelperComponent(span);

        NativeButton clearHelperComponent = new NativeButton(
                "Remove helper component", e -> {
                    dateTimePickerHelperComponent.setHelperComponent(null);
                });
        clearHelperComponent.setId("button-clear-helper-component");

        add(dateTimePickerHelperComponent, clearHelperComponent);
    }
}
