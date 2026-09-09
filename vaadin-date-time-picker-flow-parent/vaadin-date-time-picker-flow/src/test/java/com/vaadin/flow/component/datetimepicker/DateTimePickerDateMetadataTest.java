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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.datepicker.DateMetadata;
import com.vaadin.flow.component.datepicker.DateMetadataProvider;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker.DateTimePickerI18n;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class DateTimePickerDateMetadataTest {

    private static final LocalDate DISABLED_DATE = LocalDate.of(2023, 1, 10);

    private static final LocalDateTime DISABLED_DATE_TIME = DISABLED_DATE
            .atTime(12, 0);

    private static final String ERROR_MESSAGE = "Date is disabled";

    private static final String INCOMPLETE_INPUT_ERROR_MESSAGE = "Fill in both date and time";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private DateTimePicker picker;

    @BeforeEach
    void setup() {
        picker = new DateTimePicker();
        picker.setI18n(new DateTimePickerI18n()
                .setDisabledDateErrorMessage(ERROR_MESSAGE));
    }

    @Test
    void setDisabledDates_delegatesToDatePicker() {
        picker.setDisabledDates(List.of(DISABLED_DATE));

        Assertions.assertEquals(Set.of(DISABLED_DATE),
                picker.getDisabledDates());
        Assertions.assertEquals(Set.of(DISABLED_DATE),
                getDatePicker().getDisabledDates());
    }

    @Test
    void setDisabledWeekdays_delegatesToDatePicker() {
        picker.setDisabledWeekdays(
                Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        Assertions.assertEquals(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                picker.getDisabledWeekdays());
        Assertions.assertEquals(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                getDatePicker().getDisabledWeekdays());
    }

    @Test
    void setDateMetadataProvider_delegatesToDatePicker() {
        DateMetadataProvider provider = range -> List.of();
        picker.setDateMetadataProvider(provider);

        Assertions.assertSame(provider, picker.getDateMetadataProvider());
        Assertions.assertSame(provider,
                getDatePicker().getDateMetadataProvider());
    }

    @Test
    void disabledDate_validationFails() {
        picker.setDisabledDates(List.of(DISABLED_DATE));

        picker.setValue(DISABLED_DATE_TIME);

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    @Test
    void disabledWeekday_validationFails() {
        // 2023-01-10 is a Tuesday
        picker.setDisabledWeekdays(Set.of(DayOfWeek.TUESDAY));

        picker.setValue(DISABLED_DATE_TIME);

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    @Test
    void providerDisabledDate_validationFails() {
        picker.setDateMetadataProvider(
                range -> List.of(new DateMetadata(DISABLED_DATE, true)));

        picker.setValue(DISABLED_DATE_TIME);

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    @Test
    void disabledDateWithoutTime_validationFails() {
        picker.setDisabledDates(List.of(DISABLED_DATE));

        // Simulate incomplete input: date picker has a value, time picker is
        // empty, so the component value itself stays empty
        getDatePicker().setValue(DISABLED_DATE);
        fireUnparsableChangeDomEvent();

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    @Test
    void enabledDate_validationPasses() {
        picker.setDisabledDates(List.of(DISABLED_DATE.plusDays(1)));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.SUNDAY));
        picker.setDateMetadataProvider(
                range -> List.of(new DateMetadata(DISABLED_DATE, false)));

        picker.setValue(DISABLED_DATE_TIME);

        Assertions.assertFalse(picker.isInvalid());
    }

    @Test
    void disabledDateOutsideMinMax_reportsMinMaxErrorMessage() {
        picker.setI18n(new DateTimePickerI18n()
                .setDisabledDateErrorMessage(ERROR_MESSAGE)
                .setMinErrorMessage("Value is too small"));
        picker.setDisabledDates(List.of(DISABLED_DATE));
        picker.setMin(DISABLED_DATE.plusDays(5).atStartOfDay());

        picker.setValue(DISABLED_DATE_TIME);

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Value is too small", picker.getErrorMessage());
    }

    @Test
    void refreshDateMetadata_valueSet_revalidates() {
        AtomicBoolean restrictive = new AtomicBoolean(false);
        picker.setDateMetadataProvider(range -> restrictive.get()
                ? List.of(new DateMetadata(DISABLED_DATE, true))
                : List.of());

        picker.setValue(DISABLED_DATE_TIME);
        Assertions.assertFalse(picker.isInvalid());

        restrictive.set(true);
        picker.refreshDateMetadata();

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    @Test
    void refreshDateMetadata_dateOnlySet_revalidates() {
        picker.setI18n(new DateTimePickerI18n()
                .setDisabledDateErrorMessage(ERROR_MESSAGE)
                .setIncompleteInputErrorMessage(
                        INCOMPLETE_INPUT_ERROR_MESSAGE));
        AtomicBoolean restrictive = new AtomicBoolean(false);
        picker.setDateMetadataProvider(range -> restrictive.get()
                ? List.of(new DateMetadata(DISABLED_DATE, true))
                : List.of());

        // Simulate incomplete input: date picker has a value, time picker is
        // empty, so the component value itself stays empty
        getDatePicker().setValue(DISABLED_DATE);
        fireUnparsableChangeDomEvent();
        // Invalid because the time is missing, not because of the date
        Assertions.assertEquals(INCOMPLETE_INPUT_ERROR_MESSAGE,
                picker.getErrorMessage());

        restrictive.set(true);
        picker.refreshDateMetadata();

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    @Test
    void refreshDateMetadata_valueSet_firesValidationStatusChangeEvent() {
        picker.setDateMetadataProvider(range -> List.of());
        picker.setValue(DISABLED_DATE_TIME);

        AtomicInteger events = new AtomicInteger();
        picker.addValidationStatusChangeListener(
                event -> events.incrementAndGet());

        picker.refreshDateMetadata();

        Assertions.assertEquals(1, events.get());
    }

    @Test
    void refreshDateMetadata_noProvider_doesNotValidate() {
        picker.setRequiredIndicatorVisible(true);

        picker.refreshDateMetadata();

        Assertions.assertFalse(picker.isInvalid());
    }

    @Test
    void perDate_worksThroughDateTimePicker() {
        picker.setDateMetadataProvider(
                DateMetadataProvider.perDate(date -> date.equals(DISABLED_DATE)
                        ? new DateMetadata(date, true)
                        : null));

        picker.setValue(DISABLED_DATE_TIME);

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals(ERROR_MESSAGE, picker.getErrorMessage());
    }

    private DatePicker getDatePicker() {
        return (DatePicker) SlotUtils.getChildInSlot(picker, "date-picker");
    }

    private void fireUnparsableChangeDomEvent() {
        Element element = picker.getElement();
        DomEvent domEvent = new DomEvent(element, "unparsable-change",
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class)
                .fireEvent(domEvent);
    }
}
