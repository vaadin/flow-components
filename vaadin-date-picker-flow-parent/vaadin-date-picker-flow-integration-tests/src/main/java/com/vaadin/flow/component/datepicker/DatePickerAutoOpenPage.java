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
