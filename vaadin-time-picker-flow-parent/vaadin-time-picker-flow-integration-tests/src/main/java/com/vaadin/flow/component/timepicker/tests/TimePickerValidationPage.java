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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

import java.time.LocalTime;

/**
 * View for testing validation with {@link TimePicker}.
 */
@Route("vaadin-time-picker/time-picker-validation")
public class TimePickerValidationPage extends ValidationTestView {

    public TimePickerValidationPage() {
        createPickerWithMaxAndMinValues();
    }

    @Override
    protected HasValidation getValidationComponent() {
        return new TimePicker();
    }

    private void createPickerWithMaxAndMinValues() {
        final TimePicker timePicker = new TimePicker();
        timePicker.setMinTime(LocalTime.parse("09:30"));
        timePicker.setMaxTime(LocalTime.parse("17:00"));
        timePicker.setId("picker-with-valid-range");

        final Div isValid = new Div();
        isValid.setId("is-invalid");
        final NativeButton checkIsValid = new NativeButton(
                "Check if current value of step-number-field is invalid");
        checkIsValid.setId("check-is-invalid");
        checkIsValid.addClickListener(event -> isValid
                .setText(timePicker.isInvalid() ? "invalid" : "valid"));
        add(timePicker, checkIsValid, isValid);
    }

}
