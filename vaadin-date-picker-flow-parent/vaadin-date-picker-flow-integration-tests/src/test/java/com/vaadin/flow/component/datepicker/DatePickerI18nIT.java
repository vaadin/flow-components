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
 *
 */

package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@TestPath("vaadin-date-picker/date-picker-i18n")
public class DatePickerI18nIT extends AbstractComponentIT {

    @Test
    public void testSetInitialI18n() {
        open();

        assertI18n(getInitialI18nDatePicker(), TestI18N.FINNISH);
    }

    @Test
    public void testSetI18nAfterOpeningAndClosing() {
        open();

        // Open and close to force overlay to render with initial I18N settings
        DatePickerElement datePicker = getDynamicI18nDatePicker();
        datePicker.open();
        datePicker.close();

        // Then override settings and assert that overlay has updated
        TestBenchElement setFinnishButton = getSetFinnishButton();
        setFinnishButton.click();

        assertI18n(datePicker, TestI18N.FINNISH);
    }

    @Test
    public void testSetPartialI18nConfigShouldNotResultInError() {
        open();

        TestBenchElement setPartialI18nButton = getSetPartialI18nButton();
        setPartialI18nButton.click();

        DatePickerElement datePicker = getDynamicI18nDatePicker();
        datePicker.open();

        checkLogsForErrors();
    }

    private void assertI18n(DatePickerElement datePicker,
            DatePicker.DatePickerI18n i18n) {

        // Set to date in January and open
        datePicker.setDate(LocalDate.of(2021, 1, 1));
        datePicker.open();

        DatePickerElement.OverlayContentElement overlayContent = datePicker
                .getOverlayContent();

        // Look for translation for January
        String januaryText = i18n.getMonthNames().get(0);

        List<DatePickerElement.MonthCalendarElement> visibleMonthCalendars = overlayContent
                .getVisibleMonthCalendars();

        waitUntil((c) -> {
            Optional<DatePickerElement.MonthCalendarElement> maybeJanuaryMonthCalender = getMonthCalendarWithName(
                    visibleMonthCalendars, januaryText);
            return maybeJanuaryMonthCalender.isPresent();
        });

        // Verify week days translations
        DatePickerElement.MonthCalendarElement januaryMonthCalender = getMonthCalendarWithName(
                visibleMonthCalendars, januaryText).get();
        List<DatePickerElement.WeekdayElement> weekdays = januaryMonthCalender
                .getWeekdays();

        for (String weekdayShortName : i18n.getWeekdaysShort()) {
            Optional<DatePickerElement.WeekdayElement> weekday = getWeekdayByName(
                    weekdays, weekdayShortName);

            Assert.assertTrue(
                    String.format("Can not find week day with short name: %s",
                            weekdayShortName),
                    weekday.isPresent());
        }

        // Verify buttons
        ButtonElement todayButton = overlayContent.getTodayButton();
        ButtonElement cancelButton = overlayContent.getCancelButton();

        Assert.assertTrue(
                String.format("Today button does not contain: %s",
                        i18n.getToday()),
                todayButton.getText().contains(i18n.getToday()));
        Assert.assertTrue(
                String.format("Cancel button does not contain: %s",
                        i18n.getCancel()),
                cancelButton.getText().contains(i18n.getCancel()));
    }

    private DatePickerElement getInitialI18nDatePicker() {
        return $(DatePickerElement.class)
                .id(DatePickerI18nPage.ID_INITIAL_I18N_DATE_PICKER);
    }

    private DatePickerElement getDynamicI18nDatePicker() {
        return $(DatePickerElement.class)
                .id(DatePickerI18nPage.ID_DYNAMIC_I18N_DATE_PICKER);
    }

    private TestBenchElement getSetFinnishButton() {
        return $("button").id(DatePickerI18nPage.ID_SET_FINNISH_BUTTON);
    }

    private TestBenchElement getSetPartialI18nButton() {
        return $("button").id(DatePickerI18nPage.ID_SET_PARTIAL_I18N_BUTTON);
    }

    private Optional<DatePickerElement.MonthCalendarElement> getMonthCalendarWithName(
            List<DatePickerElement.MonthCalendarElement> monthCalendars,
            String monthName) {
        return monthCalendars.stream()
                .filter(month -> month.getHeaderText().contains(monthName))
                .findFirst();
    }

    private Optional<DatePickerElement.WeekdayElement> getWeekdayByName(
            List<DatePickerElement.WeekdayElement> weekdays, String name) {
        return weekdays.stream().filter(e -> e.getText().contains(name))
                .findFirst();
    }
}
