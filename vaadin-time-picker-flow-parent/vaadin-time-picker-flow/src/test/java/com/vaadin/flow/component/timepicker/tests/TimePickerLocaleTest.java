package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.timepicker.TimePicker;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

@NotThreadSafe
public class TimePickerLocaleTest {

    private UI ui;

    @Before
    public void setUp() {
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
