/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import net.jcip.annotations.NotThreadSafe;

import static org.junit.Assert.*;

@NotThreadSafe
public class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    private static LocalDate TEST_VALUE = LocalDate.now();

    private static class TestDatePicker
            extends GeneratedVaadinDatePicker<TestDatePicker, LocalDate> {

        TestDatePicker() {
            super(TEST_VALUE, null, String.class, value -> null, value -> null,
                    true);
        }
    }

    @Test
    public void datePicker_basicCases() {
        DatePicker picker = new DatePicker();

        assertEquals(null, picker.getValue());
        assertFalse(picker.getElement().hasProperty("value"));

        picker.setValue(LocalDate.of(2018, 4, 25));
        assertEquals("2018-04-25", picker.getElement().getProperty("value"));

        picker.getElement().setProperty("value", "2017-03-24");
        assertEquals(LocalDate.of(2017, 3, 24), picker.getValue());

        // Cannot do removeProperty because
        // https://github.com/vaadin/flow/issues/3994
        picker.getElement().setProperty("value", null);
        assertEquals(null, picker.getValue());
    }

    @Test
    public void defaultCtor_does_not_update_values() {
        DatePicker picker = new DatePicker();
        assertNull(picker.getValue());
        assertEquals(null, picker.getElement().getProperty("value"));
    }

    @Test
    public void setInitialValue() {
        DatePicker picker = new DatePicker(LocalDate.of(2018, 4, 25));
        assertEquals(LocalDate.of(2018, 4, 25), picker.getValue());
        assertEquals("2018-04-25", picker.getElement().getProperty("value"));
    }

    @Test
    public void updatingToNullValue_displaysEmptyString() {
        DatePicker picker = new DatePicker();

        picker.setValue(LocalDate.now());
        picker.setValue(null);

        assertNull(picker.getValue());
        assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void setOpened_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        assertFalse("Initially DatePicker should be closed", picker.isOpened());
        picker.setOpened(true);
        assertTrue(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
        picker.setOpened(false);
        assertFalse(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
    }

    @Test
    public void openAndClose_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        assertFalse("Initially DatePicker should be closed", picker.isOpened());
        picker.open();
        assertTrue(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
        picker.close();
        assertFalse(OPENED_PROPERTY_NOT_UPDATED, picker.isOpened());
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        DatePicker picker = new DatePicker();

        assertFalse("Clear button should not be visible by default",
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

        Mockito.when(instantiator.createComponent(TestDatePicker.class))
                .thenAnswer(invocation -> new TestDatePicker());

        TestDatePicker field = Component.from(element, TestDatePicker.class);
        Assert.assertEquals("2007-12-03",
                field.getElement().getPropertyRaw("value"));
    }

    public void assertClearButtonPropertyValueEquals(DatePicker picker,
            Boolean value) {
        picker.setClearButtonVisible(value);
        assertEquals(value, picker.isClearButtonVisible());
        assertEquals(picker.isClearButtonVisible(),
                picker.getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    public void setAutoOpenEnabled() {
        DatePicker picker = new DatePicker();
        assertTrue("Auto-open should be enabled by default",
                picker.isAutoOpen());
        picker.setAutoOpen(false);
        assertFalse("Should be possible to disable auto-open",
                picker.isAutoOpen());
        picker.setAutoOpen(true);
        assertTrue("Should be possible to enable auto-open",
                picker.isAutoOpen());
    }

    @Test
    public void setDateFormat_dateFormatsIsUpdated() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("MM-yyyy-dd");
        List<String> dateFormats = i18n.getDateFormats();

        assertNotNull(dateFormats);
        assertEquals(1, dateFormats.size());
        assertEquals("MM-yyyy-dd", dateFormats.get(0));
    }

    @Test
    public void setDateFormats_dateFormatsIsUpdated() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormats("MM-yyyy-dd", "MM.dd.yyyy", null, "MM§yyyy§dd");
        List<String> dateFormats = i18n.getDateFormats();

        assertNotNull(dateFormats);
        assertEquals(3, dateFormats.size());
        assertEquals("MM-yyyy-dd", dateFormats.get(0));
        assertEquals("MM.dd.yyyy", dateFormats.get(1));
        assertEquals("MM§yyyy§dd", dateFormats.get(2));
    }

    @Test
    public void setDateFormat_dateFormatsIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        assertNull(i18n.getDateFormats());

        i18n.setDateFormat("MM-yyyy-dd");
        assertNotNull(i18n.getDateFormats());

        i18n.setDateFormat(null);
        assertNull(i18n.getDateFormats());
    }

    @Test
    public void setDateFormats_dateFormatsIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        assertNull(i18n.getDateFormats());

        i18n.setDateFormats("MM-yyyy-dd");
        assertNotNull(i18n.getDateFormats());

        i18n.setDateFormats(null);
        assertNull(i18n.getDateFormats());
    }

    @Test
    public void setDateFormats_throwsExceptionWhenSecondArgIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        assertThrows(IllegalArgumentException.class,
                () -> i18n.setDateFormats("MM-yyyy-dd", null));
    }

}
