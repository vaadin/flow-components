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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import org.junit.Assert;
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
    public void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        DateTimePicker picker = new DateTimePicker();
        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsNull_valuePropertyHasEmptyString() {
        DateTimePicker picker = new DateTimePicker((LocalDateTime) null);
        Assert.assertNull(picker.getValue());
        Assert.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    public void initialValueIsDateTime_valuePropertyHasInitialValue() {
        DateTimePicker picker = new DateTimePicker(
                LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        Assert.assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
        Assert.assertEquals("2018-04-25T13:45:10",
                picker.getElement().getProperty("value"));
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

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-date-time-picker");

        String value = LocalDateTime.now().toString();
        element.setProperty("value", value);
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(DateTimePicker.class))
                .thenAnswer(invocation -> new DateTimePicker());

        DateTimePicker field = Component.from(element, DateTimePicker.class);
        Assert.assertEquals(value, field.getElement().getProperty("value"));
    }
}
