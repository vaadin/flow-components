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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("helper-text-component")
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
