/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datepicker.DatePicker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@NotThreadSafe
public class DateTimePickerTest {

    private UI ui;

    @Before
    public void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void emptyField() {
        DateTimePicker picker = new DateTimePicker();
        assertEquals(null, picker.getValue());
    }

    @Test
    public void setInitialValue() {
        DateTimePicker picker = new DateTimePicker(
                LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
    }

    @Test
    public void setInitialValue_truncatesToMilliseconds() {
        Duration oneMillisecondAndOneNano = Duration.ofMillis(1).plusNanos(1);
        Duration oneMillisecond = Duration.ofMillis(1);
        LocalDateTime baseDateTime = LocalDateTime.of(2018, 4, 25, 13, 45, 10);
        LocalDateTime dateTimeWithMillisAndNanos = baseDateTime
                .plus(oneMillisecondAndOneNano);
        LocalDateTime dateTimeWithMillisOnly = baseDateTime
                .plus(oneMillisecond);

        DateTimePicker picker = new DateTimePicker(dateTimeWithMillisAndNanos);
        assertEquals(dateTimeWithMillisOnly, picker.getValue());
    }

    @Test
    public void setValue() {
        DateTimePicker picker = new DateTimePicker();
        picker.setValue(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
    }

    @Test
    public void setValue_truncatesToMilliseconds() {
        Duration oneMillisecondAndOneNano = Duration.ofMillis(1).plusNanos(1);
        Duration oneMillisecond = Duration.ofMillis(1);
        LocalDateTime baseDateTime = LocalDateTime.of(2018, 4, 25, 13, 45, 10);
        LocalDateTime dateTimeWithMillisAndNanos = baseDateTime
                .plus(oneMillisecondAndOneNano);
        LocalDateTime dateTimeWithMillisOnly = baseDateTime
                .plus(oneMillisecond);

        DateTimePicker picker = new DateTimePicker();
        picker.setValue(dateTimeWithMillisAndNanos);
        assertEquals(dateTimeWithMillisOnly, picker.getValue());
    }

    @Test
    public void setMinGetMin() {
        DateTimePicker picker = new DateTimePicker();
        picker.setMin(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getMin());
    }

    @Test
    public void setMaxGetMax() {
        DateTimePicker picker = new DateTimePicker();
        picker.setMax(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getMax());
    }

    @Test
    public void setErrorMessage() {
        DateTimePicker picker = new DateTimePicker();
        picker.setErrorMessage("error message");
        assertEquals("error message", picker.getErrorMessage());
    }

    @Test
    public void setI18n() {
        DateTimePicker picker = new DateTimePicker();

        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n()
                .setWeek("viikko").setCalendar("kalenteri").setClear("tyhjennä")
                .setToday("tänään").setCancel("peruuta").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("tammikuu", "helmikuu",
                        "maaliskuu", "huhtikuu", "toukokuu", "kesäkuu",
                        "heinäkuu", "elokuu", "syyskuu", "lokakuu", "marraskuu",
                        "joulukuu"))
                .setWeekdays(Arrays.asList("sunnuntai", "maanantai", "tiistai",
                        "keskiviikko", "torstai", "perjantai", "lauantai"))
                .setWeekdaysShort(Arrays.asList("su", "ma", "ti", "ke", "to",
                        "pe", "la"));

        picker.setDatePickerI18n(i18n);
        assertEquals(i18n, picker.getDatePickerI18n());
    }

    @Test
    public void setAutoOpen() {
        final DateTimePicker picker = new DateTimePicker();
        assertTrue("Auto-open should be enabled by default",
                picker.isAutoOpen());
        picker.setAutoOpen(false);
        assertFalse("Should be possible to disable auto-open",
                picker.isAutoOpen());
        picker.setAutoOpen(true);
        assertTrue("Should be possible to enable auto-open",
                picker.isAutoOpen());
    }
}
