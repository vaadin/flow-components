/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

/**
 * A view for integration tests verifying that the {@code HasValidation}
 * interface is implemented for {@code TimePicker}.
 *
 * TODO: Can be tested with unit tests.
 */
@Route("vaadin-time-picker/time-picker-validation")
public class TimePickerValidationPage extends ValidationTestView {
    @Override
    protected HasValidation getValidationComponent() {
        return new TimePicker();
    }
}
