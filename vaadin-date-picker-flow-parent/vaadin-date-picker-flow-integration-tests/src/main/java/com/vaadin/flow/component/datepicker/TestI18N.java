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

import java.util.Arrays;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;

public class TestI18N {

    public static final DatePickerI18n FINNISH = new DatePickerI18n()
            .setWeek("viikko").setCalendar("kalenteri").setClear("tyhjennä")
            .setToday("tänään").setCancel("peruuta").setFirstDayOfWeek(1)
            .setMonthNames(Arrays.asList("tammikuu", "helmikuu", "maaliskuu",
                    "huhtikuu", "toukokuu", "kesäkuu", "heinäkuu", "elokuu",
                    "syyskuu", "lokakuu", "marraskuu", "joulukuu"))
            .setWeekdays(Arrays.asList("sunnuntai", "maanantai", "tiistai",
                    "keskiviikko", "torstai", "perjantai", "lauantai"))
            .setWeekdaysShort(
                    Arrays.asList("su", "ma", "ti", "ke", "to", "pe", "la"));

    /**
     * Intentionally leaves some fields in the initial / null state
     */
    public static final DatePickerI18n FINNISH_PARTIAL = new DatePickerI18n()
            .setWeekdays(Arrays.asList("sunnuntai", "maanantai", "tiistai",
                    "keskiviikko", "torstai", "perjantai", "lauantai"))
            .setWeekdaysShort(
                    Arrays.asList("su", "ma", "ti", "ke", "to", "pe", "la"));

    private TestI18N() {
    }
}
