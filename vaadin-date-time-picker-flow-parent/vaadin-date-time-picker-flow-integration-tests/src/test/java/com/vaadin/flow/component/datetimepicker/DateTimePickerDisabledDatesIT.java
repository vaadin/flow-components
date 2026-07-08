/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement.MonthCalendarElement;
import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-date-time-picker/date-time-picker-disabled-dates")
public class DateTimePickerDisabledDatesIT extends AbstractComponentIT {

    private DatePickerElement datePicker;

    @Before
    public void init() {
        open();
        datePicker = $(DateTimePickerElement.class).id("date-time-picker")
                .getDatePicker();
    }

    @Test
    public void openDateOverlay_fixedAndProviderDatesAreDisabled() {
        datePicker.open();

        MonthCalendarElement january = getJanuary2023();
        // The provider resolves asynchronously; wait for the 15th to become
        // disabled once the result arrives.
        waitUntil(driver -> january.isDateDisabled(
                DateTimePickerDisabledDatesPage.PROVIDER_DISABLED_DAY));

        // Fixed disabled dates (January 10 and 20).
        Assert.assertTrue("January 10 (fixed) should be disabled",
                january.isDateDisabled(10));
        Assert.assertTrue("January 20 (fixed) should be disabled",
                january.isDateDisabled(20));
        Assert.assertFalse("An unaffected date should remain enabled",
                january.isDateDisabled(
                        DateTimePickerDisabledDatesPage.PROVIDER_DISABLED_DAY
                                + 1));
    }

    private MonthCalendarElement getJanuary2023() {
        return datePicker.getOverlayContent().getVisibleMonthCalendars()
                .stream()
                .filter(calendar -> "January 2023"
                        .equals(calendar.getHeaderText()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "January 2023 month calendar not found"));
    }
}
