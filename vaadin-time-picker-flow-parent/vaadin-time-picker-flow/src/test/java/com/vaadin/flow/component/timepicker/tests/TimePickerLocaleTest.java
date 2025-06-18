/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.timepicker.tests;

import java.util.Locale;

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
