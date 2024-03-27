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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * View for testing validation with {@link DatePicker}.
 */
@Route("vaadin-date-picker/date-picker-validation")
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
