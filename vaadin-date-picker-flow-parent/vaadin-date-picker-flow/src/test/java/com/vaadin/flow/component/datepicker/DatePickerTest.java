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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    @Test
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        DatePicker picker = new DatePicker();
        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
        DatePicker picker = new DatePicker((LocalDate) null);
        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void datePicker_basicCases() {
        DatePicker picker = new DatePicker();

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

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-date-picker");
        element.setProperty("value", "2007-12-03");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(DatePicker.class))
                .thenAnswer(invocation -> new DatePicker());

        DatePicker field = Component.from(element, DatePicker.class);
        Assert.assertEquals("2007-12-03",
                field.getElement().getProperty("value"));
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
