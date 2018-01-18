/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * View for testing validation with {@link DatePicker}.
 */
@Route("date-picker-validation")
public class DatePickerValidationPage extends Div {

    final String STATUS = "The Invalid state of the DatePicker is ";
    final String VALUE = "The Server side value is ";

    public DatePickerValidationPage() {
        initView();
    }

    private void initView() {
        DatePicker datePicker = new DatePicker();
        Label label = new Label();
        Label value = new Label();
        value.setId("server-side-value");
        datePicker.setId("field");
        add(datePicker);

        NativeButton button = new NativeButton("Make the input invalid");
        button.setId("invalidate");
        button.addClickListener(event -> {
            datePicker.setErrorMessage("Invalidated from server");
            datePicker.setInvalid(true);
            value.setText(VALUE + String.valueOf(datePicker.getValue()));
            label.setText(STATUS + String.valueOf(datePicker.isInvalid()));
        });
        add(button);

        button = new NativeButton("Make the input valid");
        button.setId("validate");
        button.addClickListener(event -> {
            datePicker.setErrorMessage(null);
            datePicker.setInvalid(false);
            value.setText(VALUE + String.valueOf(datePicker.getValue()));
            label.setText(STATUS + String.valueOf(datePicker.isInvalid()));
        });

        datePicker.addValueChangeListener(event -> {
            label.setText(STATUS + String.valueOf(datePicker.isInvalid()));
            if (datePicker.isInvalid()) {
                datePicker.setErrorMessage("Invalidated from server");
                value.setText(VALUE + String.valueOf(datePicker.getValue()));
            } else {
                datePicker.setErrorMessage(null);
                value.setText(VALUE + String.valueOf(datePicker.getValue()));
            }
        });
        add(button);
        add(label, value);
    }
}
