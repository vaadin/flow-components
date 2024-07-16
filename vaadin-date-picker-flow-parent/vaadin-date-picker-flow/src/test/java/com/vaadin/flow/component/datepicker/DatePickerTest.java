/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.UI;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    @Test
    public void datePicker_basicCases() {
        DatePicker picker = new DatePicker();

        Assert.assertNull(picker.getValue());
        Assert.assertFalse(picker.getElement().hasProperty("value"));

        picker.setValue(LocalDate.of(2018, 4, 25));
        Assert.assertEquals("2018-04-25",
                picker.getElement().getProperty("value"));

        picker.getElement().setProperty("value", "2017-03-24");
        Assert.assertEquals(LocalDate.of(2017, 3, 24), picker.getValue());

        // Cannot do removeProperty because
        // https://github.com/vaadin/flow/issues/3994
        picker.getElement().setProperty("value", null);
        Assert.assertNull(picker.getValue());
    }

    @Test
    public void defaultCtor_does_not_update_values() {
        DatePicker picker = new DatePicker();
        Assert.assertNull(picker.getValue());
        Assert.assertNull(picker.getElement().getProperty("value"));
    }

    @Test
    public void setInitialValue() {
        DatePicker picker = new DatePicker(LocalDate.of(2018, 4, 25));
        Assert.assertEquals(LocalDate.of(2018, 4, 25), picker.getValue());
        Assert.assertEquals("2018-04-25",
                picker.getElement().getProperty("value"));
    }

    @Test
    public void updatingToNullValue_displaysEmptyString() {
        DatePicker picker = new DatePicker();

        picker.setValue(LocalDate.now());
        picker.setValue(null);

        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void setOpened_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        Assert.assertFalse("Initially DatePicker should be closed",
                picker.isOpened());
        picker.setOpened(true);
        Assert.assertTrue(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
        picker.setOpened(false);
        Assert.assertFalse(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
    }

    @Test
    public void openAndClose_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        Assert.assertFalse("Initially DatePicker should be closed",
                picker.isOpened());
        picker.open();
        Assert.assertTrue(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
        picker.close();
        Assert.assertFalse(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        DatePicker picker = new DatePicker();

        Assert.assertFalse("Clear button should not be visible by default",
                picker.isClearButtonVisible());
        assertClearButtonPropertyValueEquals(picker, true);
        assertClearButtonPropertyValueEquals(picker, false);
    }

    public void assertClearButtonPropertyValueEquals(DatePicker picker,
            Boolean value) {
        picker.setClearButtonVisible(value);
        Assert.assertEquals(value, picker.isClearButtonVisible());
        Assert.assertEquals(picker.isClearButtonVisible(),
                picker.getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    public void setAutoOpenEnabled() {
        DatePicker picker = new DatePicker();
        Assert.assertTrue("Auto-open should be enabled by default",
                picker.isAutoOpen());
        picker.setAutoOpen(false);
        Assert.assertFalse("Should be possible to disable auto-open",
                picker.isAutoOpen());
        picker.setAutoOpen(true);
        Assert.assertTrue("Should be possible to enable auto-open",
                picker.isAutoOpen());
    }

    @Test
    public void setDateFormat_dateFormatsIsUpdated() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("MM-yyyy-dd");
        List<String> dateFormats = i18n.getDateFormats();

        Assert.assertNotNull(dateFormats);
        Assert.assertEquals(1, dateFormats.size());
        Assert.assertEquals("MM-yyyy-dd", dateFormats.get(0));
    }

    @Test
    public void setDateFormats_dateFormatsIsUpdated() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormats("MM-yyyy-dd", "MM.dd.yyyy", "MM§yyyy§dd");
        List<String> dateFormats = i18n.getDateFormats();

        Assert.assertNotNull(dateFormats);
        Assert.assertEquals(3, dateFormats.size());
        Assert.assertEquals("MM-yyyy-dd", dateFormats.get(0));
        Assert.assertEquals("MM.dd.yyyy", dateFormats.get(1));
        Assert.assertEquals("MM§yyyy§dd", dateFormats.get(2));
    }

    @Test
    public void setDateFormats_nullIsRemovedFromDateFormats() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormats("MM-yyyy-dd", null, "MM.dd.yyyy", "MM§yyyy§dd");
        List<String> dateFormats = i18n.getDateFormats();

        Assert.assertNotNull(dateFormats);
        Assert.assertEquals(3, dateFormats.size());
        Assert.assertEquals("MM-yyyy-dd", dateFormats.get(0));
        Assert.assertEquals("MM.dd.yyyy", dateFormats.get(1));
        Assert.assertEquals("MM§yyyy§dd", dateFormats.get(2));
    }

    @Test
    public void setDateFormat_dateFormatsIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        Assert.assertNull(i18n.getDateFormats());

        i18n.setDateFormat("MM-yyyy-dd");
        Assert.assertNotNull(i18n.getDateFormats());

        i18n.setDateFormat(null);

        Assert.assertNull(i18n.getDateFormats());
    }

    @Test
    public void setDateFormats_dateFormatsIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        Assert.assertNull(i18n.getDateFormats());

        i18n.setDateFormats("MM-yyyy-dd");
        Assert.assertNotNull(i18n.getDateFormats());

        i18n.setDateFormats(null);
        Assert.assertNull(i18n.getDateFormats());
    }

    @Test
    public void setDateFormats_throwsExceptionWhenSecondArgIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        Assert.assertThrows(NullPointerException.class,
                () -> i18n.setDateFormats("MM-yyyy-dd", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void datePickerFirstDayOfTheWeek() {
        DatePicker germanDatePicker = new DatePicker();
        germanDatePicker.setLabel("German");
        germanDatePicker.setValue(LocalDate.now());

        DatePickerI18n datePickerI18n = new DatePickerI18n();
        datePickerI18n.setWeek("Woche");
        datePickerI18n.setCalendar("Kalender");
        datePickerI18n.setClear("Löschen");
        datePickerI18n.setToday("Heute");
        datePickerI18n.setCancel("Abbrechen");
        datePickerI18n.setWeekdays(Arrays.asList("Sonntag", "Montag",
                "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"));
        datePickerI18n.setWeekdaysShort(
                Arrays.asList("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"));
        datePickerI18n.setMonthNames(Arrays.asList("Januar", "Februar", "März",
                "April", "Mai", "Juni", "Juli", "August", "September",
                "Oktober", "November", "Dezember"));
        datePickerI18n.setFirstDayOfWeek(7); // or any number outside 0-6 range

        germanDatePicker.setI18n(datePickerI18n);
    }

}
