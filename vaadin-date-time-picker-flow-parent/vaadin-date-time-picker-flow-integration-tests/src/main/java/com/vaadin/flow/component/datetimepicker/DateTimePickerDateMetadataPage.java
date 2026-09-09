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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.datepicker.DateMetadata;
import com.vaadin.flow.component.datepicker.DateRange;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-time-picker/date-metadata")
public class DateTimePickerDateMetadataPage extends Div {

    // January 2023 starts on a Sunday, so the weekend days are 7, 8, 14, 15,
    // 21, 22, 28 and 29. The days below are picked so that each one is disabled
    // by exactly one constraint.
    public static final LocalDateTime INITIAL_VALUE = LocalDate.of(2023, 1, 5)
            .atTime(12, 0);
    public static final String MONTH_HEADER = "January 2023";
    public static final int ENABLED_DAY = 12;
    public static final int FIXED_DISABLED_DAY = 10;
    public static final int OTHER_FIXED_DISABLED_DAY = 20;
    public static final int SATURDAY_DAY = 7;
    public static final int SUNDAY_DAY = 8;
    public static final int PROVIDER_DISABLED_DAY = 17;
    public static final int REFRESHED_DISABLED_DAY = 18;
    public static final int PART_NAME_DAY = 25;
    public static final String PART_NAME = "busy";
    private final Set<Integer> disabledDays = new LinkedHashSet<>(
            List.of(PROVIDER_DISABLED_DAY));

    public DateTimePickerDateMetadataPage() {
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setId("date-time-picker");
        dateTimePicker.setValue(INITIAL_VALUE);
        dateTimePicker.setDisabledDates(
                List.of(LocalDate.of(2023, 1, FIXED_DISABLED_DAY),
                        LocalDate.of(2023, 1, OTHER_FIXED_DISABLED_DAY)));
        dateTimePicker.setDisabledWeekdays(
                List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        dateTimePicker.setDateMetadataProvider(this::getDateMetadata);

        NativeButton refresh = new NativeButton("Refresh date metadata",
                event -> {
                    disabledDays.add(REFRESHED_DISABLED_DAY);
                    dateTimePicker.refreshDateMetadata();
                });
        refresh.setId("refresh");

        add(dateTimePicker, refresh);
    }

    /**
     * Answers from the mutable set of disabled days, so that a refresh can
     * change the answer without the provider being set again.
     */
    private Collection<DateMetadata> getDateMetadata(DateRange range) {
        List<DateMetadata> metadata = new ArrayList<>();
        for (LocalDate date = range.start(); !date
                .isAfter(range.end()); date = date.plusDays(1)) {
            if (disabledDays.contains(date.getDayOfMonth())) {
                metadata.add(new DateMetadata(date, true));
            } else if (date.getDayOfMonth() == PART_NAME_DAY) {
                metadata.add(new DateMetadata(date, PART_NAME));
            }
        }
        return metadata;
    }

}
