/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker.tests;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.timepicker.TimePicker;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class TimePickerLocaleTest {

    private UI ui;

    @Before
    public void setup() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void newTimePicker_returnsUiLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        ui.setLocale(finnishLocale);
        TimePicker timePicker = new TimePicker();
        Assert.assertEquals(finnishLocale, timePicker.getLocale());
    }

    @Test
    public void setCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        TimePicker timePicker = new TimePicker();
        timePicker.setLocale(usLocale);
        Assert.assertEquals(usLocale, timePicker.getLocale());
    }
}
