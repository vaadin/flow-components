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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@NotThreadSafe
class DatePickerDisabledDatesTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private DatePicker picker;

    @BeforeEach
    void setup() {
        picker = new DatePicker();
    }

    @Test
    void setDisabledDates_configContainsZeroBasedMonthTriples() {
        picker.setDisabledDates(
                List.of(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 12, 31)));

        ArrayNode dates = getDisabledDatesFromConfig();
        Assertions.assertEquals(2, dates.size());
        // January is month 0 on the wire, December is month 11
        assertDateTriple(dates.get(0), 2023, 0, 10);
        assertDateTriple(dates.get(1), 2023, 11, 31);
    }

    @Test
    void setDisabledDates_null_configIsEmpty() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        picker.setDisabledDates(null);

        Assertions.assertEquals(0, getDisabledDatesFromConfig().size());
        Assertions.assertTrue(picker.getDisabledDates().isEmpty());
    }

    @Test
    void setDisabledDates_containsNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> picker.setDisabledDates(
                        Arrays.asList(LocalDate.of(2023, 1, 10), null)));
    }

    @Test
    void setDisabledWeekdays_configContainsIsoWeekdayNumbers() {
        picker.setDisabledWeekdays(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        ArrayNode weekdays = (ArrayNode) picker.createDateMetadataConfig()
                .get("disabledWeekdays");
        // ISO weekday numbers: Monday is 1, Sunday is 7
        Assertions.assertEquals(3, weekdays.size());
        Assertions.assertEquals(1, weekdays.get(0).intValue());
        Assertions.assertEquals(6, weekdays.get(1).intValue());
        Assertions.assertEquals(7, weekdays.get(2).intValue());
    }

    @Test
    void setDisabledWeekdays_null_configIsEmpty() {
        picker.setDisabledWeekdays(Set.of(DayOfWeek.SUNDAY));
        picker.setDisabledWeekdays(null);

        ArrayNode weekdays = (ArrayNode) picker.createDateMetadataConfig()
                .get("disabledWeekdays");
        Assertions.assertEquals(0, weekdays.size());
        Assertions.assertTrue(picker.getDisabledWeekdays().isEmpty());
    }

    @Test
    void setDisabledWeekdays_containsNull_throws() {
        Assertions.assertThrows(NullPointerException.class, () -> picker
                .setDisabledWeekdays(Arrays.asList(DayOfWeek.MONDAY, null)));
    }

    @Test
    void getDisabledWeekdays_empty_returnsEmptySet() {
        Assertions.assertTrue(picker.getDisabledWeekdays().isEmpty());
    }

    @Test
    void getDisabledDates_returnedSetIsUnmodifiable() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));

        Set<LocalDate> dates = picker.getDisabledDates();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dates.add(LocalDate.of(2023, 1, 11)));
    }

    @Test
    void getDisabledWeekdays_returnedSetIsUnmodifiable() {
        picker.setDisabledWeekdays(Set.of(DayOfWeek.MONDAY));

        Set<DayOfWeek> weekdays = picker.getDisabledWeekdays();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> weekdays.add(DayOfWeek.TUESDAY));
    }

    @Test
    void isDateDisabled_null_returnsFalse() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.TUESDAY));

        Assertions.assertFalse(picker.isDateDisabled(null));
    }

    @Test
    void isDateDisabled_disabledDate_returnsTrue() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));

        Assertions.assertTrue(picker.isDateDisabled(LocalDate.of(2023, 1, 10)));
    }

    @Test
    void isDateDisabled_disabledWeekday_returnsTrue() {
        picker.setDisabledWeekdays(Set.of(DayOfWeek.TUESDAY));

        // 2023-01-10 is a Tuesday
        Assertions.assertTrue(picker.isDateDisabled(LocalDate.of(2023, 1, 10)));
    }

    @Test
    void isDateDisabled_enabledDate_returnsFalse() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.SUNDAY));

        // 2023-01-11 is a Wednesday
        Assertions
                .assertFalse(picker.isDateDisabled(LocalDate.of(2023, 1, 11)));
    }

    @Test
    void isDateDisabled_outsideMinMax_returnsFalse() {
        picker.setMin(LocalDate.of(2023, 1, 10));
        picker.setMax(LocalDate.of(2023, 1, 20));

        // Both dates are out of range, which the validation confirms, but
        // neither is disabled as such: min and max are not considered
        picker.setValue(LocalDate.of(2023, 1, 5));
        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertFalse(picker.isDateDisabled(LocalDate.of(2023, 1, 5)));

        picker.setValue(LocalDate.of(2023, 1, 25));
        Assertions.assertTrue(picker.isInvalid());
        Assertions
                .assertFalse(picker.isDateDisabled(LocalDate.of(2023, 1, 25)));
    }

    @Test
    void multipleSettersInOneRoundTrip_singleConfigInvocation() {
        ui.add(picker);
        // Discard the invocations that the attach itself produces
        ui.dumpPendingJavaScriptInvocations();

        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.SUNDAY));

        Assertions.assertEquals(1, getDateMetadataConfigInvocations().size());
    }

    @Test
    void attach_configuredPicker_configPushed() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        ui.add(picker);

        Assertions.assertEquals(1, getDateMetadataConfigInvocations().size());
    }

    @Test
    void attach_nothingConfigured_noConfigPushed() {
        ui.add(picker);

        Assertions.assertTrue(getDateMetadataConfigInvocations().isEmpty());
    }

    @Test
    void disabledDate_validationFails() {
        picker.setI18n(new DatePickerI18n()
                .setDisabledDateErrorMessage("Date is disabled"));
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));

        picker.setValue(LocalDate.of(2023, 1, 10));

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Date is disabled", picker.getErrorMessage());
    }

    @Test
    void disabledWeekday_validationFails() {
        picker.setI18n(new DatePickerI18n()
                .setDisabledDateErrorMessage("Date is disabled"));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.TUESDAY));

        // 2023-01-10 is a Tuesday
        picker.setValue(LocalDate.of(2023, 1, 10));

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Date is disabled", picker.getErrorMessage());
    }

    @Test
    void enabledDate_validationPasses() {
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        picker.setDisabledWeekdays(Set.of(DayOfWeek.SUNDAY));

        // 2023-01-11 is a Wednesday
        picker.setValue(LocalDate.of(2023, 1, 11));

        Assertions.assertFalse(picker.isInvalid());
    }

    @Test
    void disabledDate_customErrorMessage_takesPriority() {
        picker.setI18n(new DatePickerI18n()
                .setDisabledDateErrorMessage("Date is disabled"));
        picker.setErrorMessage("Custom error message");
        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));

        picker.setValue(LocalDate.of(2023, 1, 10));

        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Custom error message",
                picker.getErrorMessage());
    }

    @Test
    void disabledDateOutsideMinMax_reportsMinMaxErrorMessage() {
        picker.setI18n(
                new DatePickerI18n().setMinErrorMessage("Date is too small")
                        .setMaxErrorMessage("Date is too big")
                        .setDisabledDateErrorMessage("Date is disabled"));
        picker.setMin(LocalDate.of(2023, 1, 10));
        picker.setMax(LocalDate.of(2023, 1, 20));
        picker.setDisabledDates(
                List.of(LocalDate.of(2023, 1, 5), LocalDate.of(2023, 1, 25)));

        picker.setValue(LocalDate.of(2023, 1, 5));
        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Date is too small", picker.getErrorMessage());

        picker.setValue(LocalDate.of(2023, 1, 25));
        Assertions.assertTrue(picker.isInvalid());
        Assertions.assertEquals("Date is too big", picker.getErrorMessage());
    }

    @Test
    void setDisabledDates_doesNotRevalidate() {
        picker.setValue(LocalDate.of(2023, 1, 10));

        picker.setDisabledDates(List.of(LocalDate.of(2023, 1, 10)));
        Assertions.assertFalse(picker.isInvalid());

        picker.setDisabledWeekdays(Set.of(DayOfWeek.TUESDAY));
        Assertions.assertFalse(picker.isInvalid());
    }

    private ArrayNode getDisabledDatesFromConfig() {
        ObjectNode config = picker.createDateMetadataConfig();
        return (ArrayNode) config.get("disabledDates");
    }

    private void assertDateTriple(JsonNode triple, int year, int zeroBasedMonth,
            int day) {
        Assertions.assertEquals(3, triple.size());
        Assertions.assertEquals(year, triple.get(0).intValue());
        Assertions.assertEquals(zeroBasedMonth, triple.get(1).intValue());
        Assertions.assertEquals(day, triple.get(2).intValue());
    }

    private List<JavaScriptInvocation> getDateMetadataConfigInvocations() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("setDateMetadataConfig"))
                .toList();
    }
}
