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

import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.DISABLED_DATE_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.ENABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.FIXED_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.LOADING_MONTH_HEADER;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.LOADING_MONTH_VALUE_PREFIX;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.MONTH_HEADER;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.OTHER_FIXED_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.PART_NAME;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.PART_NAME_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.PROVIDER_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.REFRESHED_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.SATURDAY_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.SLOW_PROVIDER_DISABLED_DAY;
import static com.vaadin.flow.component.datepicker.DatePickerDateMetadataPage.SUNDAY_DAY;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
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

        Assert.assertTrue(calendar.isDateDisabled(FIXED_DISABLED_DAY));
        Assert.assertTrue(calendar.isDateDisabled(OTHER_FIXED_DISABLED_DAY));
        Assert.assertFalse(calendar.isDateDisabled(ENABLED_DAY));
    }

    @Test
    public void openOverlay_weekendsAreDisabled() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        Assert.assertTrue(calendar.isDateDisabled(SATURDAY_DAY));
        Assert.assertTrue(calendar.isDateDisabled(SUNDAY_DAY));
    }

    @Test
    public void openOverlay_providerDisablesDate() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        waitUntil(driver -> calendar.isDateDisabled(PROVIDER_DISABLED_DAY));
        Assert.assertFalse(calendar.isDateDisabled(ENABLED_DAY));
    }

    @Test
    public void openOverlay_disabledDateHasAriaDisabled() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        waitUntil(driver -> calendar.isDateDisabled(PROVIDER_DISABLED_DAY));
        Assert.assertEquals("true",
                calendar.getDateAriaDisabled(FIXED_DISABLED_DAY));
        Assert.assertEquals("true",
                calendar.getDateAriaDisabled(PROVIDER_DISABLED_DAY));
        Assert.assertEquals("false", calendar.getDateAriaDisabled(ENABLED_DAY));
    }

    @Test
    public void openOverlay_providerAddsPartName() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);

        waitUntil(driver -> calendar.hasDatePart(PART_NAME_DAY, PART_NAME));
        Assert.assertFalse(calendar.isDateDisabled(PART_NAME_DAY));
    }

    @Test
    public void setDisabledDate_fieldIsInvalidWithI18nMessage() {
        datePicker.setInputValue("1/" + FIXED_DISABLED_DAY + "/2023");

        waitUntil(driver -> datePicker.isInvalid());
        Assert.assertEquals(DISABLED_DATE_ERROR_MESSAGE,
                datePicker.getErrorMessage());
    }

    @Test
    public void setProviderDisabledDate_fieldIsInvalid() {
        datePicker.setInputValue("1/" + PROVIDER_DISABLED_DAY + "/2023");

        waitUntil(driver -> datePicker.isInvalid());
        Assert.assertEquals(DISABLED_DATE_ERROR_MESSAGE,
                datePicker.getErrorMessage());
    }

    @Test
    public void setEnabledDate_fieldIsValid() {
        datePicker.setInputValue("1/" + ENABLED_DAY + "/2023");

        waitUntil(driver -> LocalDate.of(2023, 1, ENABLED_DAY)
                .equals(datePicker.getDate()));
        Assert.assertFalse(datePicker.isInvalid());
    }

    @Test
    public void refreshDateMetadata_disabledDatesAreUpdated() {
        MonthCalendarElement calendar = openCalendar(datePicker, MONTH_HEADER);
        waitUntil(driver -> calendar.isDateDisabled(PROVIDER_DISABLED_DAY));
        Assert.assertFalse(calendar.isDateDisabled(REFRESHED_DISABLED_DAY));

        datePicker.close();
        clickElementWithJs("refresh");

        MonthCalendarElement refreshedCalendar = openCalendar(datePicker,
                MONTH_HEADER);
        waitUntil(driver -> refreshedCalendar
                .isDateDisabled(REFRESHED_DISABLED_DAY));
    }

    @Test
    public void openOverlay_slowProvider_datesLoadingButSelectable() {
        DatePickerElement slowDatePicker = $(DatePickerElement.class)
                .id("slow-date-picker");

        // The provider blocks the session lock while it answers, so waiting for
        // the server would also wait for the loading state to be over.
        getCommandExecutor().disableWaitForVaadin();
        try {
            // The metadata for the year of the initial value is already
            // fetched when the page is ready, so the loading state shows on
            // the months of the neighboring year that the overlay renders.
            slowDatePicker.open();
            MonthCalendarElement calendar = getCalendar(slowDatePicker,
                    LOADING_MONTH_HEADER);
            waitUntil(driver -> calendar
                    .isDateLoading(SLOW_PROVIDER_DISABLED_DAY));

            Assert.assertTrue(slowDatePicker.getOverlayContent().isLoading());
            Assert.assertFalse(
                    calendar.isDateDisabled(SLOW_PROVIDER_DISABLED_DAY));

            calendar.clickDate(SLOW_PROVIDER_DISABLED_DAY);
            Assert.assertEquals(
                    LOADING_MONTH_VALUE_PREFIX + SLOW_PROVIDER_DISABLED_DAY,
                    slowDatePicker.getPropertyString("value"));
        } finally {
            getCommandExecutor().enableWaitForVaadin();
        }

        // Once the provider has answered, the date is no longer loading and is
        // rendered as disabled.
        MonthCalendarElement calendar = openCalendar(slowDatePicker,
                LOADING_MONTH_HEADER);
        waitUntil(
                driver -> calendar.isDateDisabled(SLOW_PROVIDER_DISABLED_DAY));
        Assert.assertFalse(calendar.isDateLoading(SLOW_PROVIDER_DISABLED_DAY));
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
