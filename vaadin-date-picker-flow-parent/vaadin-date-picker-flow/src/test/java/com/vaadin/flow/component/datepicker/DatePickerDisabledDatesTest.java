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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@NotThreadSafe
class DatePickerDisabledDatesTest {

    @RegisterExtension
    MockUIExtension uiExtension = new MockUIExtension();

    private DatePicker datePicker;

    @BeforeEach
    void init() {
        datePicker = new DatePicker();
        datePicker.setI18n(new DatePickerI18n()
                .setDateDisabledErrorMessage("Date is not available"));
    }

    // --- Communication to the connector -----------------------------------

    @Test
    void setDisabledDates_configHasIsoDates() {
        datePicker.setDisabledDates(
                List.of(LocalDate.of(2023, 1, 5), LocalDate.of(2023, 1, 6)));

        ObjectNode config = datePicker.createDisabledDatesConfig();
        Assertions.assertEquals(List.of("2023-01-05", "2023-01-06"),
                toStringList((ArrayNode) config.get("dates")));
        Assertions.assertTrue(((ArrayNode) config.get("weekdays")).isEmpty());
        Assertions.assertFalse(config.get("hasProvider").asBoolean());
    }

    @Test
    void setDisabledWeekdays_configHasIsoWeekdayNumbers() {
        datePicker.setDisabledWeekdays(
                EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        ObjectNode config = datePicker.createDisabledDatesConfig();
        Assertions.assertEquals(
                Set.of(DayOfWeek.SATURDAY.getValue(),
                        DayOfWeek.SUNDAY.getValue()),
                toIntSet((ArrayNode) config.get("weekdays")));
    }

    @Test
    void setDisabledDatesProvider_configHasProviderFlag() {
        datePicker.setDisabledDatesProvider(date -> false);

        ObjectNode config = datePicker.createDisabledDatesConfig();
        Assertions.assertTrue(config.get("hasProvider").asBoolean());
    }

    // --- Server computation for a requested range -------------------------

    @Test
    void setDisabledDatesProvider_computesDisabledDatesForRange() {
        // Disable Mondays.
        datePicker.setDisabledDatesProvider(
                date -> date.getDayOfWeek() == DayOfWeek.MONDAY);

        ArrayNode result = datePicker.computeDisabledDatesForRange("2023-03-01",
                "2023-03-14");
        Assertions.assertEquals(List.of("2023-03-06", "2023-03-13"),
                toStringList(result));
    }

    @Test
    void noProvider_computesEmptyRange() {
        ArrayNode result = datePicker.computeDisabledDatesForRange("2023-03-01",
                "2023-03-31");
        Assertions.assertTrue(result.isEmpty());
    }

    // --- Server-side validation -------------------------------------------

    @Test
    void setDisabledDates_selectDisabledDate_isInvalid() {
        datePicker.setDisabledDates(List.of(LocalDate.of(2023, 1, 5)));

        datePicker.setValue(LocalDate.of(2023, 1, 5));
        Assertions.assertTrue(datePicker.isInvalid());
        Assertions.assertEquals("Date is not available",
                datePicker.getErrorMessage());

        datePicker.setValue(LocalDate.of(2023, 1, 6));
        Assertions.assertFalse(datePicker.isInvalid());
    }

    @Test
    void setDisabledWeekdays_selectSaturday_isInvalid() {
        datePicker.setDisabledWeekdays(
                EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        datePicker.setValue(LocalDate.of(2023, 1, 7)); // Saturday
        Assertions.assertTrue(datePicker.isInvalid());

        datePicker.setValue(LocalDate.of(2023, 1, 9)); // Monday
        Assertions.assertFalse(datePicker.isInvalid());
    }

    @Test
    void setDisabledDatesProvider_selectDisabledDate_isInvalid() {
        datePicker.setDisabledDatesProvider(
                date -> date.getDayOfWeek() == DayOfWeek.MONDAY);

        datePicker.setValue(LocalDate.of(2023, 3, 6)); // Monday
        Assertions.assertTrue(datePicker.isInvalid());

        datePicker.setValue(LocalDate.of(2023, 3, 7)); // Tuesday
        Assertions.assertFalse(datePicker.isInvalid());
    }

    @Test
    void minTakesPriorityOverDisabledDates() {
        datePicker.setMin(LocalDate.of(2023, 1, 10));
        datePicker.setDisabledDates(List.of(LocalDate.of(2023, 1, 5)));
        datePicker.setI18n(new DatePickerI18n().setMinErrorMessage("Too early")
                .setDateDisabledErrorMessage("Date is not available"));

        datePicker.setValue(LocalDate.of(2023, 1, 5)); // before min and
                                                       // disabled
        Assertions.assertTrue(datePicker.isInvalid());
        Assertions.assertEquals("Too early", datePicker.getErrorMessage());
    }

    // --- Getters ----------------------------------------------------------

    @Test
    void setDisabledWeekdays_getReturnsSameSet() {
        Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.MONDAY,
                DayOfWeek.FRIDAY);
        datePicker.setDisabledWeekdays(weekdays);
        Assertions.assertEquals(weekdays, datePicker.getDisabledWeekdays());
    }

    // --- Helpers ----------------------------------------------------------

    private static List<String> toStringList(ArrayNode array) {
        return StreamSupport.stream(array.spliterator(), false)
                .map(JsonNode::asString).toList();
    }

    private static Set<Integer> toIntSet(ArrayNode array) {
        return StreamSupport.stream(array.spliterator(), false)
                .map(JsonNode::asInt).collect(Collectors.toSet());
    }
}
