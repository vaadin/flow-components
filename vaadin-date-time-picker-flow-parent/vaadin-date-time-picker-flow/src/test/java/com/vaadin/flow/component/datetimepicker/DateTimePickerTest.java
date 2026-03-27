/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class DateTimePickerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        DateTimePicker picker = new DateTimePicker();
        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        DateTimePicker picker = new DateTimePicker((LocalDateTime) null);
        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsDateTime_valuePropertyHasInitialValue() {
        DateTimePicker picker = new DateTimePicker(
                LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        Assertions.assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
        Assertions.assertEquals("2018-04-25T13:45:10",
                picker.getElement().getProperty("value"));
    }

    @Test
    void emptyField() {
        DateTimePicker picker = new DateTimePicker();
        assertEquals(null, picker.getValue());
    }

    @Test
    void setInitialValue() {
        DateTimePicker picker = new DateTimePicker(
                LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
    }

    @Test
    void setInitialValue_truncatesToMilliseconds() {
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
    void setValue() {
        DateTimePicker picker = new DateTimePicker();
        picker.setValue(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getValue());
    }

    @Test
    void setValue_truncatesToMilliseconds() {
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
    void setMinGetMin() {
        DateTimePicker picker = new DateTimePicker();
        picker.setMin(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getMin());
    }

    @Test
    void setMaxGetMax() {
        DateTimePicker picker = new DateTimePicker();
        picker.setMax(LocalDateTime.of(2018, 4, 25, 13, 45, 10));
        assertEquals(LocalDateTime.of(2018, 4, 25, 13, 45, 10),
                picker.getMax());
    }

    @Test
    void setErrorMessage() {
        DateTimePicker picker = new DateTimePicker();
        picker.setErrorMessage("error message");
        assertEquals("error message", picker.getErrorMessage());
    }

    @Test
    void setI18n() {
        DateTimePicker picker = new DateTimePicker();

        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n()
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
    void setAutoOpen() {
        final DateTimePicker picker = new DateTimePicker();
        assertTrue(picker.isAutoOpen(),
                "Auto-open should be enabled by default");
        picker.setAutoOpen(false);
        assertFalse(picker.isAutoOpen(),
                "Should be possible to disable auto-open");
        picker.setAutoOpen(true);
        assertTrue(picker.isAutoOpen(),
                "Should be possible to enable auto-open");
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-date-time-picker");

        String value = LocalDateTime.now().toString();
        element.setProperty("value", value);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(DateTimePicker.class))
                .thenAnswer(invocation -> new DateTimePicker());

        DateTimePicker field = Component.from(element, DateTimePicker.class);
        Assertions.assertEquals(value, field.getElement().getProperty("value"));
    }

    @Test
    void setAriaLabel() {
        final DateTimePicker picker = new DateTimePicker();
        Assertions.assertTrue(picker.getAriaLabel().isEmpty());
        picker.setAriaLabel("aria-label");
        Assertions.assertTrue(picker.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", picker.getAriaLabel().get());

        picker.setAriaLabel(null);
        Assertions.assertTrue(picker.getAriaLabel().isEmpty());
    }

    @Test
    void setDateAriaLabel() {
        final DateTimePicker picker = new DateTimePicker();
        Assertions.assertTrue(picker.getDateAriaLabel().isEmpty());
        picker.setDateAriaLabel("date-aria-label");
        Assertions.assertTrue(picker.getDateAriaLabel().isPresent());
        Assertions.assertEquals("date-aria-label",
                picker.getDateAriaLabel().get());

        picker.setDateAriaLabel(null);
        Assertions.assertTrue(picker.getDateAriaLabel().isEmpty());
    }

    @Test
    void setTimeAriaLabel() {
        final DateTimePicker picker = new DateTimePicker();
        Assertions.assertTrue(picker.getTimeAriaLabel().isEmpty());
        picker.setTimeAriaLabel("time-aria-label");
        Assertions.assertTrue(picker.getTimeAriaLabel().isPresent());
        Assertions.assertEquals("time-aria-label",
                picker.getTimeAriaLabel().get());

        picker.setTimeAriaLabel(null);
        Assertions.assertTrue(picker.getTimeAriaLabel().isEmpty());
    }

    @Test
    void implementsHasTooltip() {
        DateTimePicker picker = new DateTimePicker();
        Assertions.assertTrue(picker instanceof HasTooltip);
    }

    @Test
    void implementsInputField() {
        DateTimePicker field = new DateTimePicker();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime>, LocalDateTime>);
    }
}
