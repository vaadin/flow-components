package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.Locale;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;

@NotThreadSafe
public class DatePickerLocaleTest {

    private UI ui;

    @Before
    public void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void newDatePicker_returnsUiLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        ui.setLocale(finnishLocale);
        DatePicker datePicker = new DatePicker();
        Assert.assertEquals(finnishLocale, datePicker.getLocale());
    }

    @Test
    public void newDatePickerWithCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        DatePicker datePicker = new DatePicker(LocalDate.now(), usLocale);
        Assert.assertEquals(usLocale, datePicker.getLocale());
    }

    @Test
    public void setCustomLocale_returnsCustomLocale() {
        Locale finnishLocale = new Locale("fi-FI");
        Locale usLocale = new Locale("en-US");
        ui.setLocale(finnishLocale);
        DatePicker datePicker = new DatePicker();
        datePicker.setLocale(usLocale);
        Assert.assertEquals(usLocale, datePicker.getLocale());
    }
}
