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
package com.vaadin.flow.component.timepicker.tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.timepicker.TimePicker.TimePickerI18n;
import com.vaadin.flow.internal.JacksonUtils;

class TimePickerI18nTest {

    @Test
    void timeFormats_defaultNull() {
        Assertions.assertNull(new TimePickerI18n().getTimeFormats());
    }

    @Test
    void setTimeFormat_returnsSingleFormat() {
        TimePickerI18n i18n = new TimePickerI18n();

        Assertions.assertSame(i18n, i18n.setTimeFormat("HH:mm"));
        Assertions.assertEquals(List.of("HH:mm"), i18n.getTimeFormats());
    }

    @Test
    void setTimeFormats_returnsPrimaryAndAdditionalFormats() {
        TimePickerI18n i18n = new TimePickerI18n();

        Assertions.assertSame(i18n,
                i18n.setTimeFormats("HH.mm", "Hmm", "HH:mm"));
        Assertions.assertEquals(List.of("HH.mm", "Hmm", "HH:mm"),
                i18n.getTimeFormats());
    }

    @Test
    void getTimeFormats_isUnmodifiable() {
        TimePickerI18n i18n = new TimePickerI18n().setTimeFormats("HH.mm",
                "Hmm");

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> i18n.getTimeFormats().add("HH:mm"));
    }

    @Test
    void setTimeFormatsWithNullAdditionalFormats_throws() {
        TimePickerI18n i18n = new TimePickerI18n();

        Assertions.assertThrows(NullPointerException.class,
                () -> i18n.setTimeFormats("HH:mm", (String[]) null));
    }

    @Test
    void setTimeFormatsWithNullAdditionalFormat_dropsNullFormat() {
        TimePickerI18n i18n = new TimePickerI18n().setTimeFormats("HH.mm", null,
                "Hmm");

        Assertions.assertEquals(List.of("HH.mm", "Hmm"), i18n.getTimeFormats());
    }

    @Test
    void setNullPrimaryFormat_resetsTimeFormats() {
        TimePickerI18n i18n = new TimePickerI18n().setTimeFormats("HH.mm",
                "Hmm");

        i18n.setTimeFormats(null, "Hmm");
        Assertions.assertNull(i18n.getTimeFormats());

        i18n.setTimeFormat("HH:mm");
        i18n.setTimeFormat(null);
        Assertions.assertNull(i18n.getTimeFormats());
    }

    @Test
    void serialize_containsOnlyTimeFormats() {
        TimePickerI18n i18n = new TimePickerI18n()
                .setBadInputErrorMessage("bad input")
                .setRequiredErrorMessage("required").setMinErrorMessage("min")
                .setMaxErrorMessage("max");

        Assertions.assertEquals("{}", JacksonUtils.beanToJson(i18n).toString());

        i18n.setTimeFormats("HH.mm", "Hmm");
        Assertions.assertEquals("{\"timeFormats\":[\"HH.mm\",\"Hmm\"]}",
                JacksonUtils.beanToJson(i18n).toString());
    }
}
