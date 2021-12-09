package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.UI;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Locale;

@NotThreadSafe
public class DateTimePickerLocaleTest {

    private UI ui;

    @Before
    public void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void newDateTimePicker_returnsUiLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        ui.setLocale(finnishLocale);
        DateTimePicker dateTimePicker = new DateTimePicker();
        Assert.assertEquals(finnishLocale, dateTimePicker.getLocale());
    }

    @Test
    public void newDateTimePickerWithCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        DateTimePicker dateTimePicker = new DateTimePicker(LocalDateTime.now(),
                usLocale);
        Assert.assertEquals(usLocale, dateTimePicker.getLocale());
    }

    @Test
    public void setCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLocale(usLocale);
        Assert.assertEquals(usLocale, dateTimePicker.getLocale());
    }
}
