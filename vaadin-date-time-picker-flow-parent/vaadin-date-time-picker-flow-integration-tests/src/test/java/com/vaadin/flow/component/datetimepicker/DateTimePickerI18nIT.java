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
package com.vaadin.flow.component.datetimepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-time-picker/i18n")
public class DateTimePickerI18nIT extends AbstractComponentIT {

    private DateTimePickerElement dateTimePicker;

    @Before
    public void init() {
        open();
        dateTimePicker = $(DateTimePickerElement.class).first();
    }

    @Test
    public void setAndRemoveDateAriaLabel_assertI18nProperty() {
        $("button").id("set-date-aria-label").click();
        Assert.assertEquals("Custom date",
                dateTimePicker.getPropertyString("i18n", "dateLabel"));

        $("button").id("remove-date-aria-label").click();
        Assert.assertNull(
                dateTimePicker.getPropertyString("i18n", "dateLabel"));
    }

    @Test
    public void setAndRemoveTimeAriaLabel_assertI18nProperty() {
        $("button").id("set-time-aria-label").click();
        Assert.assertEquals("Custom time",
                dateTimePicker.getPropertyString("i18n", "timeLabel"));

        $("button").id("remove-time-aria-label").click();
        Assert.assertNull(
                dateTimePicker.getPropertyString("i18n", "timeLabel"));
    }

    @Test
    public void setI18n_assertI18nProperty() {
        $("button").id("set-i18n").click();
        Assert.assertEquals("date",
                dateTimePicker.getPropertyString("i18n", "dateLabel"));
        Assert.assertEquals("time",
                dateTimePicker.getPropertyString("i18n", "timeLabel"));
    }

    @Test
    public void setI18n_setAndRemoveDateAriaLabel_assertI18nProperty() {
        $("button").id("set-i18n").click();

        $("button").id("set-date-aria-label").click();
        Assert.assertEquals("Custom date",
                dateTimePicker.getPropertyString("i18n", "dateLabel"));

        $("button").id("remove-date-aria-label").click();
        Assert.assertEquals("date",
                dateTimePicker.getPropertyString("i18n", "dateLabel"));
    }

    @Test
    public void setI18n_setAndRemoveTimeAriaLabel_assertI18nProperty() {
        $("button").id("set-i18n").click();

        $("button").id("set-time-aria-label").click();
        Assert.assertEquals("Custom time",
                dateTimePicker.getPropertyString("i18n", "timeLabel"));

        $("button").id("remove-time-aria-label").click();
        Assert.assertEquals("time",
                dateTimePicker.getPropertyString("i18n", "timeLabel"));
    }
}
