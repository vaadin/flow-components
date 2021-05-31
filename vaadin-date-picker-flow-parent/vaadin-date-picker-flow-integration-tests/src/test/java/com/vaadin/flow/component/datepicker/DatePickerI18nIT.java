/*
 * Copyright 2000-2021 Vaadin Ltd.
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

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;

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
        WebElement setFinnishButton = getSetFinnishButton();
        setFinnishButton.click();

        assertI18n(datePicker, TestI18N.FINNISH);
    }

    private void assertI18n(DatePickerElement datePicker, DatePicker.DatePickerI18n i18n) {
        // Set to date in January
        datePicker.setDate(LocalDate.of(2021, 1, 1));
        datePicker.open();

        // Look for translation for January
        String januaryText = i18n.getMonthNames().get(0);
        WebElement januaryMonthCalender = getMonthCalendarWithName(
                getVisibleMonthCalendars(), januaryText);

        Assert.assertNotNull(
                String.format("Can not find month with name: %s", januaryText),
                januaryMonthCalender);

        // Verify week days translations
        List<WebElement> weekdays = getWeekdays(januaryMonthCalender);

        for (String weekdayShortName : i18n.getWeekdaysShort()) {
            WebElement weekday = getWeekdayByName(weekdays, weekdayShortName);

            Assert.assertNotNull(
                    String.format("Can not find week day with short name: %s",
                            weekdayShortName), weekday);
        }

        // Verify buttons
        WebElement todayButton = getTodayButton();
        WebElement cancelButton = getCancelButton();

        Assert.assertTrue(String.format("Today button does not contain: %s",
                i18n.getToday()),
                todayButton.getText().contains(i18n.getToday()));
        Assert.assertTrue(String.format("Cancel button does not contain: %s",
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

    private WebElement getSetFinnishButton() {
        return $("button").id(DatePickerI18nPage.ID_SET_FINNISH_BUTTON);
    }

    private WebElement getOverlayContent() {
        TestBenchElement overlay = $("vaadin-date-picker-overlay")
                .waitForFirst();
        WebElement content = findInShadowRoot(overlay, By.id("content")).get(0);
        WebElement overlayContent = findInShadowRoot(content,
                By.id("overlay-content")).get(0);

        return overlayContent;
    }

    private List<WebElement> getVisibleMonthCalendars() {
        return findInShadowRoot(getOverlayContent(),
                By.tagName("vaadin-month-calendar"));
    }

    private WebElement getMonthCalendarWithName(List<WebElement> monthCalendars,
            String monthName) {
        return monthCalendars.stream().filter(month -> {
            WebElement header = findInShadowRoot(month,
                    By.cssSelector("[part=month-header]")).get(0);
            return header.getText().contains(monthName);
        }).findFirst().orElse(null);
    }

    private List<WebElement> getWeekdays(WebElement monthCalendar) {
        return findInShadowRoot(monthCalendar,
                By.cssSelector("[part=weekday]"));
    }

    private WebElement getWeekdayByName(List<WebElement> weekdays,
            String name) {
        return weekdays.stream().filter(e -> e.getText().contains(name))
                .findFirst().orElse(null);
    }

    private WebElement getTodayButton() {
        return findInShadowRoot(getOverlayContent(),
                By.cssSelector("[part=today-button]")).get(0);
    }

    private WebElement getCancelButton() {
        return findInShadowRoot(getOverlayContent(),
                By.cssSelector("[part=cancel-button]")).get(0);
    }
}
