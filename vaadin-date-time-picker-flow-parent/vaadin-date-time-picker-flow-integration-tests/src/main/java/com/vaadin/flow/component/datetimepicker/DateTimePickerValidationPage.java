/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

import java.time.LocalDateTime;

/**
 * View for testing validation with {@link DateTimePicker}.
 */
@Route("vaadin-date-time-picker/date-time-picker-validation")
public class DateTimePickerValidationPage extends ValidationTestView {

    @Override
    protected HasValidation getValidationComponent() {
        return new DateTimePicker();
    }
}
