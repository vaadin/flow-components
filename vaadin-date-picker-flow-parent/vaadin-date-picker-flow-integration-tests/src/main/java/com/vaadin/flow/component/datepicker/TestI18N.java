/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
