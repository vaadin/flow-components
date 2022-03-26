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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;

/**
 * Integration tests for the {@link DateTimePickerPage}.
 */
@TestPath("vaadin-date-time-picker/date-time-picker-it")
public class DateTimePickerIT extends AbstractComponentIT {

    private DateTimePickerElement picker;
    private TestBenchElement message;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
        picker = $(DateTimePickerElement.class).first();
        message = $("div").id("message");
    }

    @Test
    public void selectDateTime() {
        picker.setDateTime(LocalDateTime.of(1985, 1, 10, 13, 51));

        Assert.assertThat(message.getText(),
                CoreMatchers.containsString("1985-01-10 13:51:00"));

        picker.clear();

        Assert.assertEquals("No date is selected", message.getText());
    }

    @Test
    public void selectDateOrTimeSeparately() {
        picker.setDate(LocalDate.of(1985, 1, 11));
        // message should not have date yet as time is still unset
        // (DateTimePicker value should still be null)
        Assert.assertThat(message.getText(),
                CoreMatchers.not(CoreMatchers.containsString("1985-01-11")));

        picker.setTime(LocalTime.of(14, 42));
        Assert.assertThat(message.getText(),
                CoreMatchers.containsString("1985-01-11 14:42"));

        picker.setDate(LocalDate.of(1987, 4, 10));
        Assert.assertThat(message.getText(),
                CoreMatchers.containsString("1987-04-10 14:42"));

        picker.setDate(null);
        Assert.assertEquals("No date is selected", message.getText());
    }

    @Test
    public void focus() {
        TestBenchElement focusButton = $("button").id("button-focus");
        TestBenchCommandExecutor cmd = focusButton.getCommandExecutor();

        Assert.assertEquals(true, cmd.executeScript(
                "return document.activeElement !== arguments[0].__datePicker.inputElement",
                picker));

        focusButton.click();

        Assert.assertEquals(true, cmd.executeScript(
                "return document.activeElement === arguments[0].__datePicker.inputElement",
                picker));
    }

    @Test
    public void checkDateTimeSetFromServer() {
        DateTimePickerElement picker = $(DateTimePickerElement.class)
                .id("date-time-picker-value-from-server");
        TestBenchElement message = $("div").id("message-value-from-server");
        TestBenchElement button = $("button").id("button-value-from-server");

        // check initial values of date-time-picker, date-picker and time-picker
        Assert.assertEquals(LocalDateTime.of(2017, 3, 1, 12, 10),
                picker.getDateTime());
        Assert.assertEquals(LocalDate.of(2017, 3, 1), picker.getDate());
        Assert.assertEquals(LocalTime.of(12, 10), picker.getTime());

        // update value via button on server side
        button.click();
        waitUntil(webDriver -> message.getText().contains("2019-10"));
        // check value change message from server
        Assert.assertThat(message.getText(),
                CoreMatchers.containsString("2019-10-15 09:40"));
        // check values of date-time-picker, date-picker and time-picker
        Assert.assertEquals(LocalDateTime.of(2019, 10, 15, 9, 40),
                picker.getDateTime());
        Assert.assertEquals(LocalDate.of(2019, 10, 15), picker.getDate());
        Assert.assertEquals(LocalTime.of(9, 40), picker.getTime());
    }

    @Test
    @Ignore
    // https://github.com/vaadin/vaadin-date-time-picker-flow/issues/40
    public void hasInitialValue_clearFromServer_valueNull_fieldsEmpty() {
        DateTimePickerElement picker = $(DateTimePickerElement.class)
                .id("date-time-picker-value-from-server");
        TestBenchElement message = $("div").id("message-value-from-server");
        clickElementWithJs("clear-from-server");
        Assert.assertEquals("No date is selected", message.getText());
        Assert.assertNull("Expected internal date picker to be empty",
                picker.getDate());
        Assert.assertNull("Expected internal time picker to be empty",
                picker.getTime());
    }

    @Test
    public void testLocale() {
        DateTimePickerElement picker = $(DateTimePickerElement.class)
                .id("date-time-picker-locale");
        TestBenchElement button = $("button").id("button-locale");

        Assert.assertEquals(LocalDateTime.of(2018, 1, 2, 15, 30),
                picker.getDateTime());
        // Finnish locale by default
        Assert.assertEquals("2.1.2018", picker.getDatePresentation());
        Assert.assertEquals("15.30", picker.getTimePresentation());

        // Set US locale
        button.click();
        Assert.assertEquals("1/2/2018", picker.getDatePresentation());
        Assert.assertEquals("3:30 PM", picker.getTimePresentation());
    }

    @Test
    public void testSetHighPrecisionValueShouldWorkNotThrowErrors() {
        DateTimePickerElement picker = $(DateTimePickerElement.class)
                .id("date-time-picker-set-high-precision-value");

        Assert.assertEquals(LocalDateTime.of(2018, 1, 2, 15, 30),
                picker.getDateTime());

        checkLogsForErrors();
    }

    @Test
    public void testSetHighPrecisionInitialValueShouldWorkAndNotThrowErrors() {
        DateTimePickerElement picker = $(DateTimePickerElement.class)
                .id("date-time-picker-high-precision-initial-value");

        Assert.assertEquals(LocalDateTime.of(2018, 1, 2, 15, 30),
                picker.getDateTime());

        checkLogsForErrors();
    }

    @Test
    public void testSmallVariantHasTheme() {
        DateTimePickerElement picker = $(DateTimePickerElement.class)
                .id("date-time-picker-variant");

        Assert.assertEquals("small", picker.getAttribute("theme"));
    }
}
