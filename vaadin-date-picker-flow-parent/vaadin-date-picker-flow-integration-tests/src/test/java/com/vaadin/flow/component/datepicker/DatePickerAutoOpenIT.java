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
package com.vaadin.flow.component.datepicker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DatePickerAutoOpenPage}.
 */
@TestPath("datepickerautoopenpage")
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
