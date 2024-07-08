/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the {@link DatePickerAutoOpenPage}.
 */
@TestPath("vaadin-date-picker/datepickerautoopenpage")
public class DatePickerAutoOpenIT extends AbstractComponentIT {

    @Test
    public void testSettingAutoOpenOnServerSide() {
        open();
        $(TestBenchElement.class).id("enable-button").click();
        final DatePickerElement datePickerElement = $(DatePickerElement.class)
                .first();
        assertTrue(datePickerElement.isAutoOpen());
        $(TestBenchElement.class).id("disable-button").click();
        assertFalse(datePickerElement.isAutoOpen());
    }
}
