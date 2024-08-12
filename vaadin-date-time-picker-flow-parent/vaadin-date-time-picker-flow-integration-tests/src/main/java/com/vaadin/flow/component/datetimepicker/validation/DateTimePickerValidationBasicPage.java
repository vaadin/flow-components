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
package com.vaadin.flow.component.datetimepicker.validation;

import java.time.LocalDateTime;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-date-time-picker/validation/basic")
public class DateTimePickerValidationBasicPage
        extends AbstractValidationPage<DateTimePicker> {
    public static final String ATTACH_FIELD_BUTTON = "attach-field-button";
    public static final String DETACH_FIELD_BUTTON = "detach-field-button";

    public static final String REQUIRED_BUTTON = "required-button";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public DateTimePickerValidationBasicPage() {
        super();

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));

        add(createInput(MIN_INPUT, "Set min date time", event -> {
            var value = LocalDateTime.parse(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date time", event -> {
            var value = LocalDateTime.parse(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));

        addAttachDetachControls();
    }

    protected void addAttachDetachControls() {
        NativeButton attachButton = createButton(ATTACH_FIELD_BUTTON,
                "Attach field", event -> add(testField));
        NativeButton detachButton = createButton(DETACH_FIELD_BUTTON,
                "Detach field", event -> add(testField));

        add(new Div(attachButton, detachButton));
    }

    protected DateTimePicker createTestField() {
        return new DateTimePicker();
    }
}
