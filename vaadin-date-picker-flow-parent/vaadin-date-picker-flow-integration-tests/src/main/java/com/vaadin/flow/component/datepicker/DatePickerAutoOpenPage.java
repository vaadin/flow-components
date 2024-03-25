/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.function.Consumer;

@Route
public class DatePickerAutoOpenPage extends Div {
    public DatePickerAutoOpenPage() {
        final DatePicker datePicker = new DatePicker();
        final Div autoOpenStatus = new Div();
        autoOpenStatus.setId("auto-open-status");
        final Runnable updateDiv = () -> autoOpenStatus
                .setText(String.valueOf(datePicker.isAutoOpen()));
        final Consumer<Boolean> updateEnabledStatus = status -> {
            datePicker.setAutoOpen(status);
            updateDiv.run();
        };
        final NativeButton enableButton = new NativeButton("Enable auto open",
                e -> updateEnabledStatus.accept(true));
        enableButton.setId("enable-button");
        final NativeButton disableButton = new NativeButton("Disable auto open",
                e -> updateEnabledStatus.accept(false));
        disableButton.setId("disable-button");
        final NativeButton updateStatusButton = new NativeButton(
                "Update status", e -> updateDiv.run());
        updateStatusButton.setId("update-status-button");

        add(datePicker, autoOpenStatus, enableButton, disableButton,
                updateStatusButton);
    }
}
