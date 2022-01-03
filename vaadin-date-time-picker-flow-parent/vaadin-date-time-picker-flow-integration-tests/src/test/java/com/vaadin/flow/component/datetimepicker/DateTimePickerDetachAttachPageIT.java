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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * Integration tests for attaching / detaching date time picker.
 */
@TestPath("vaadin-date-time-picker/date-time-picker-detach-attach")
public class DateTimePickerDetachAttachPageIT extends AbstractComponentIT {

    @Test
    public void clientSideValidationIsOverriddenOnAttach() {
        open();

        assertDateTimePickerIsValidOnTab();

        // Detaching and attaching date time picker
        TestBenchElement toggleAttach = $("button").id("toggle-attached");
        toggleAttach.click();
        toggleAttach.click();

        assertDateTimePickerIsValidOnTab();
    }

    private void assertDateTimePickerIsValidOnTab() {
        DateTimePickerElement dateTimePicker = $(DateTimePickerElement.class)
                .first();
        dateTimePicker.sendKeys(Keys.TAB);
        Assert.assertFalse("Date time picker should be valid after Tab",
                dateTimePicker.getPropertyBoolean("invalid"));
    }
}
