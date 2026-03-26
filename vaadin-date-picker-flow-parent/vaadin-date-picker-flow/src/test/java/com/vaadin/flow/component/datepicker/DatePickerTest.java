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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class DatePickerTest {

    private static final String OPENED_PROPERTY_NOT_UPDATED = "The server-side \"opened\"-property was not updated synchronously";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void initialValueIsNotSpecified_valuePropertyHasEmptyString() {
        DatePicker picker = new DatePicker();
        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void initialValueIsNull_valuePropertyHasEmptyString() {
        DatePicker picker = new DatePicker((LocalDate) null);
        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void datePicker_basicCases() {
        DatePicker picker = new DatePicker();

        picker.setValue(LocalDate.of(2018, 4, 25));
        Assertions.assertEquals("2018-04-25",
                picker.getElement().getProperty("value"));

        picker.getElement().setProperty("value", "2017-03-24");
        Assertions.assertEquals(LocalDate.of(2017, 3, 24), picker.getValue());

        // Cannot do removeProperty because
        // https://github.com/vaadin/flow/issues/3994
        picker.getElement().setProperty("value", null);
        Assertions.assertNull(picker.getValue());
    }

    @Test
    void setInitialValue() {
        DatePicker picker = new DatePicker(LocalDate.of(2018, 4, 25));
        Assertions.assertEquals(LocalDate.of(2018, 4, 25), picker.getValue());
        Assertions.assertEquals("2018-04-25",
                picker.getElement().getProperty("value"));
    }

    @Test
    void emptyValueIsNull() {
        DatePicker picker = new DatePicker();
        Assertions.assertNull(picker.getEmptyValue());
    }

    @Test
    void setInitialValue_emptyValueIsNull() {
        DatePicker picker = new DatePicker(LocalDate.of(2018, 4, 25));
        Assertions.assertNull(picker.getEmptyValue());
    }

    @Test
    void updatingToNullValue_displaysEmptyString() {
        DatePicker picker = new DatePicker();

        picker.setValue(LocalDate.now());
        picker.setValue(null);

        Assertions.assertNull(picker.getValue());
        Assertions.assertEquals("", picker.getElement().getProperty("value"));
    }

    @Test
    void setOpened_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        Assertions.assertFalse(picker.isOpened(),
                "Initially DatePicker should be closed");
        picker.setOpened(true);
        Assertions.assertTrue(picker.isOpened(), OPENED_PROPERTY_NOT_UPDATED);
        picker.setOpened(false);
        Assertions.assertFalse(picker.isOpened(), OPENED_PROPERTY_NOT_UPDATED);
    }

    @Test
    void openAndClose_openedPropertyIsUpdated() {
        DatePicker picker = new DatePicker();
        Assertions.assertFalse(picker.isOpened(),
                "Initially DatePicker should be closed");
        picker.open();
        Assertions.assertTrue(picker.isOpened(), OPENED_PROPERTY_NOT_UPDATED);
        picker.close();
        Assertions.assertFalse(picker.isOpened(), OPENED_PROPERTY_NOT_UPDATED);
    }

    @Test
    void clearButtonVisiblePropertyValue() {
        DatePicker picker = new DatePicker();

        Assertions.assertFalse(picker.isClearButtonVisible(),
                "Clear button should not be visible by default");
        assertClearButtonPropertyValueEquals(picker, true);
        assertClearButtonPropertyValueEquals(picker, false);
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-date-picker");
        element.setProperty("value", "2007-12-03");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(DatePicker.class))
                .thenAnswer(invocation -> new DatePicker());

        DatePicker field = Component.from(element, DatePicker.class);
        Assertions.assertEquals("2007-12-03",
                field.getElement().getProperty("value"));
    }

    public void assertClearButtonPropertyValueEquals(DatePicker picker,
            Boolean value) {
        picker.setClearButtonVisible(value);
        Assertions.assertEquals(value, picker.isClearButtonVisible());
        Assertions.assertEquals(picker.isClearButtonVisible(),
                picker.getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    void setAutoOpenEnabled() {
        DatePicker picker = new DatePicker();
        Assertions.assertTrue(picker.isAutoOpen(),
                "Auto-open should be enabled by default");
        picker.setAutoOpen(false);
        Assertions.assertFalse(picker.isAutoOpen(),
                "Should be possible to disable auto-open");
        picker.setAutoOpen(true);
        Assertions.assertTrue(picker.isAutoOpen(),
                "Should be possible to enable auto-open");
    }

    @Test
    void setDateFormat_dateFormatsIsUpdated() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("MM-yyyy-dd");
        List<String> dateFormats = i18n.getDateFormats();

        Assertions.assertNotNull(dateFormats);
        Assertions.assertEquals(1, dateFormats.size());
        Assertions.assertEquals("MM-yyyy-dd", dateFormats.get(0));
    }

    @Test
    void setDateFormats_dateFormatsIsUpdated() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormats("MM-yyyy-dd", "MM.dd.yyyy", "MM§yyyy§dd");
        List<String> dateFormats = i18n.getDateFormats();

        Assertions.assertNotNull(dateFormats);
        Assertions.assertEquals(3, dateFormats.size());
        Assertions.assertEquals("MM-yyyy-dd", dateFormats.get(0));
        Assertions.assertEquals("MM.dd.yyyy", dateFormats.get(1));
        Assertions.assertEquals("MM§yyyy§dd", dateFormats.get(2));
    }

    @Test
    void setDateFormats_nullIsRemovedFromDateFormats() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormats("MM-yyyy-dd", null, "MM.dd.yyyy", "MM§yyyy§dd");
        List<String> dateFormats = i18n.getDateFormats();

        Assertions.assertNotNull(dateFormats);
        Assertions.assertEquals(3, dateFormats.size());
        Assertions.assertEquals("MM-yyyy-dd", dateFormats.get(0));
        Assertions.assertEquals("MM.dd.yyyy", dateFormats.get(1));
        Assertions.assertEquals("MM§yyyy§dd", dateFormats.get(2));
    }

    @Test
    void setDateFormat_dateFormatsIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        Assertions.assertNull(i18n.getDateFormats());

        i18n.setDateFormat("MM-yyyy-dd");
        Assertions.assertNotNull(i18n.getDateFormats());

        i18n.setDateFormat(null);

        Assertions.assertNull(i18n.getDateFormats());
    }

    @Test
    void setDateFormats_dateFormatsIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        Assertions.assertNull(i18n.getDateFormats());

        i18n.setDateFormats("MM-yyyy-dd");
        Assertions.assertNotNull(i18n.getDateFormats());

        i18n.setDateFormats(null);
        Assertions.assertNull(i18n.getDateFormats());
    }

    @Test
    void setDateFormats_throwsExceptionWhenSecondArgIsNull() {
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();

        Assertions.assertThrows(NullPointerException.class,
                () -> i18n.setDateFormats("MM-yyyy-dd", null));
    }

    @Test
    void datePickerFirstDayOfTheWeek() {
        DatePicker germanDatePicker = new DatePicker();
        germanDatePicker.setLabel("German");
        germanDatePicker.setValue(LocalDate.now());

        DatePickerI18n datePickerI18n = new DatePickerI18n();
        datePickerI18n.setToday("Heute");
        datePickerI18n.setCancel("Abbrechen");
        datePickerI18n.setWeekdays(Arrays.asList("Sonntag", "Montag",
                "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"));
        datePickerI18n.setWeekdaysShort(
                Arrays.asList("So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"));
        datePickerI18n.setMonthNames(Arrays.asList("Januar", "Februar", "März",
                "April", "Mai", "Juni", "Juli", "August", "September",
                "Oktober", "November", "Dezember"));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> datePickerI18n.setFirstDayOfWeek(7));
    }

    @Test
    void implementsHasAllowedCharPattern() {
        Assertions.assertTrue(
                HasAllowedCharPattern.class
                        .isAssignableFrom(new DatePicker().getClass()),
                "DatePicker should support char pattern");
    }

    @Test
    void implementsHasTooltip() {
        DatePicker picker = new DatePicker();
        Assertions.assertTrue(picker instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        Assertions.assertTrue(
                HasAriaLabel.class.isAssignableFrom(DatePicker.class),
                "Date picker should support aria-label and aria-labelledby");
    }

    @Test
    void setAriaLabel() {
        DatePicker datePicker = new DatePicker();

        datePicker.setAriaLabel("aria-label");
        Assertions.assertTrue(datePicker.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", datePicker.getAriaLabel().get());

        datePicker.setAriaLabel(null);
        Assertions.assertTrue(datePicker.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        DatePicker datePicker = new DatePicker();

        datePicker.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(datePicker.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                datePicker.getAriaLabelledBy().get());

        datePicker.setAriaLabelledBy(null);
        Assertions.assertTrue(datePicker.getAriaLabelledBy().isEmpty());
    }

    @Test
    void setPrefix_hasPrefix() {
        DatePicker picker = new DatePicker();
        TestPrefix prefix = new TestPrefix();

        picker.setPrefixComponent(prefix);

        Assertions.assertEquals(prefix, picker.getPrefixComponent());
    }

    @Test
    void setTextAsPrefix_throws() {
        DatePicker picker = new DatePicker();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> picker.setPrefixComponent(new Text("Prefix")));
    }

    @Test
    void unregisterOpenedChangeListenerOnEvent() {
        var picker = new DatePicker();

        var listenerInvokedCount = new AtomicInteger(0);
        picker.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        picker.open();
        picker.close();

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void unregisterInvalidChangeListenerOnEvent() {
        var picker = new DatePicker();

        var listenerInvokedCount = new AtomicInteger(0);
        picker.addInvalidChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        picker.setInvalid(true);
        picker.setInvalid(false);

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void implementsInputField() {
        var field = new DatePicker();
        Assertions.assertTrue(
                field instanceof InputField<AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>, LocalDate>);
    }

    @Test
    void setFallbackParser_getFallbackParser() {
        DatePicker datePicker = new DatePicker();
        Assertions.assertNull(datePicker.getFallbackParser());

        SerializableFunction<String, Result<LocalDate>> fallbackParser = (
                s) -> {
            if (s.equals("tomorrow")) {
                return Result.ok(LocalDate.now().plusDays(1));
            } else {
                return Result.error("Invalid date format");
            }
        };

        datePicker.setFallbackParser(fallbackParser);
        Assertions.assertEquals(fallbackParser, datePicker.getFallbackParser());

        datePicker.setFallbackParser(null);
        Assertions.assertNull(datePicker.getFallbackParser());
    }

    @Tag("div")
    private static class TestPrefix extends Component {
    }
}
