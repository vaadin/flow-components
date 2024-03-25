/**
 * Copyright (C) 2000-2024 Vaadin Ltd
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

import java.util.function.Consumer;

@Route
public class DateTimePickerAutoOpenPage extends Div {

    public DateTimePickerAutoOpenPage() {
        final DateTimePicker dateTimePicker = new DateTimePicker();
        final Div autoOpenStatus = new Div();
        autoOpenStatus.setId("auto-open-status");
        final Runnable updateDiv = () -> autoOpenStatus
                .setText(String.valueOf(dateTimePicker.isAutoOpen()));
        final Consumer<Boolean> updateAutoOpenStatus = status -> {
            dateTimePicker.setAutoOpen(status);
            updateDiv.run();
        };
        final NativeButton enableButton = new NativeButton("Enable auto open",
                e -> updateAutoOpenStatus.accept(true));
        enableButton.setId("enable-button");
        final NativeButton disableButton = new NativeButton("Disable auto open",
                e -> updateAutoOpenStatus.accept(false));
        disableButton.setId("disable-button");
        final NativeButton updateStatusButton = new NativeButton(
                "Update status", e -> updateDiv.run());
        updateStatusButton.setId("update-status-button");

        add(dateTimePicker, autoOpenStatus, enableButton, disableButton,
                updateStatusButton);
    }
}
