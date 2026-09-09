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
package com.vaadin.flow.component.datepicker;

import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.ENABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.FIXED_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.MONTH_HEADER;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.OTHER_FIXED_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.PART_NAME;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.PART_NAME_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.PROVIDER_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.REFRESHED_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.SATURDAY_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.SUNDAY_DAY;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement.DayElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement.MonthCalendarElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement.OverlayContentElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DatePickerDateMetadataPage}.
 */
@TestPath("vaadin-date-picker/date-metadata")
public class DatePickerDateMetadataIT extends AbstractComponentIT {

    private DatePickerElement datePicker;

    @Before
    public void init() {
        open();
        datePicker = $(DatePickerElement.class).id("date-picker");
    }

    @Test
    public void openOverlay_fixedDatesAreDisabled() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        Assert.assertTrue(isDayDisabled(calendar, FIXED_DISABLED_DAY));
        Assert.assertTrue(isDayDisabled(calendar, OTHER_FIXED_DISABLED_DAY));
        Assert.assertFalse(isDayDisabled(calendar, ENABLED_DAY));
    }

    @Test
    public void openOverlay_weekendsAreDisabled() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        Assert.assertTrue(isDayDisabled(calendar, SATURDAY_DAY));
        Assert.assertTrue(isDayDisabled(calendar, SUNDAY_DAY));
    }

    @Test
    public void openOverlay_providerDisablesDate() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        waitUntil(driver -> isDayDisabled(calendar, PROVIDER_DISABLED_DAY));
        Assert.assertFalse(isDayDisabled(calendar, ENABLED_DAY));
    }

    @Test
    public void openOverlay_providerAddsPartName() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        waitUntil(driver -> hasDayPart(calendar.getDay(PART_NAME_DAY),
                PART_NAME));
        Assert.assertFalse(isDayDisabled(calendar, PART_NAME_DAY));
    }

    @Test
    public void refreshDateMetadata_disabledDatesAreUpdated() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);
        waitUntil(driver -> isDayDisabled(calendar, PROVIDER_DISABLED_DAY));
        Assert.assertFalse(isDayDisabled(calendar, REFRESHED_DISABLED_DAY));

        datePicker.close();
        clickElementWithJs("refresh");

        MonthCalendarElement refreshedCalendar = openCalendar(datePicker,
                MONTH_HEADER);
        waitUntil(driver -> isDayDisabled(refreshedCalendar,
                REFRESHED_DISABLED_DAY));
    }

    private boolean isDayDisabled(MonthCalendarElement calendar, int day) {
        return calendar.getDay(day).hasAttribute("disabled");
    }

    private boolean hasDayPart(DayElement day, String partName) {
        return List.of(day.getDomAttribute("part").split(" "))
                .contains(partName);
    }

    private MonthCalendarElement openCalendar(DatePickerElement picker,
            String monthHeader) {
        picker.open();
        return getCalendar(picker, monthHeader);
    }

    private MonthCalendarElement getCalendar(DatePickerElement picker,
            String monthHeader) {
        OverlayContentElement overlayContent = picker.getOverlayContent();
        return waitUntil(driver -> overlayContent
                .getVisibleMonthCalendars().stream().filter(calendar -> calendar
                        .getHeaderText().contains(monthHeader))
                .findFirst().orElse(null));
    }

}
