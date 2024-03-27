/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

/**
 * A view for integration tests verifying that the {@code HasValidation}
 * interface is implemented for {@code TimePicker}.
 *
 * TODO: Can be tested with unit tests.
 */
@Route("vaadin-date-time-picker/date-time-picker-validation")
public class DateTimePickerValidationPage extends ValidationTestView {
    @Override
    protected HasValidation getValidationComponent() {
        return new DateTimePicker();
    }
}
