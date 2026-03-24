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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

class DateTimePickerI18nTest {

    private DateTimePicker dateTimePicker;

    @BeforeEach
    void setup() {
        dateTimePicker = new DateTimePicker();
    }

    @Test
    void setDateAriaLabel_removeDateAriaLabel() {
        dateTimePicker.setDateAriaLabel("Custom date");
        Assertions.assertEquals("Custom date",
                getI18nPropertyAsJson(dateTimePicker).get("dateLabel")
                        .asString());

        dateTimePicker.setDateAriaLabel(null);
        Assertions.assertTrue(getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel") instanceof NullNode);
    }

    @Test
    void setDateAriaLabel_setI18n() {
        dateTimePicker.setDateAriaLabel("Custom date");

        dateTimePicker.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date"));
        Assertions.assertEquals("Custom date",
                getI18nPropertyAsJson(dateTimePicker).get("dateLabel")
                        .asString());
    }

    @Test
    void setTimeAriaLabel_removeTimeAriaLabel() {
        dateTimePicker.setTimeAriaLabel("Custom time");
        Assertions.assertEquals("Custom time",
                getI18nPropertyAsJson(dateTimePicker).get("timeLabel")
                        .asString());

        dateTimePicker.setTimeAriaLabel(null);
        Assertions.assertTrue(getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel") instanceof NullNode);
    }

    @Test
    void setTimeAriaLabel_setI18n() {
        dateTimePicker.setTimeAriaLabel("Custom time");

        dateTimePicker.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setTimeLabel("I18n time"));
        Assertions.assertEquals("Custom time",
                getI18nPropertyAsJson(dateTimePicker).get("timeLabel")
                        .asString());
    }

    @Test
    void setI18n() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        Assertions.assertEquals("I18n date",
                getI18nPropertyAsJson(dateTimePicker).get("dateLabel")
                        .asString());
        Assertions.assertEquals("I18n time",
                getI18nPropertyAsJson(dateTimePicker).get("timeLabel")
                        .asString());
    }

    @Test
    void setI18n_setDateAriaLabel_removeDateAriaLabel() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        dateTimePicker.setDateAriaLabel("Custom date");
        Assertions.assertEquals("Custom date",
                getI18nPropertyAsJson(dateTimePicker).get("dateLabel")
                        .asString());

        dateTimePicker.setDateAriaLabel(null);
        Assertions.assertEquals("I18n date",
                getI18nPropertyAsJson(dateTimePicker).get("dateLabel")
                        .asString());
    }

    @Test
    void setI18n_setTimeAriaLabel_removeTimeAriaLabel() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        dateTimePicker.setTimeAriaLabel("Custom time");
        Assertions.assertEquals("Custom time",
                getI18nPropertyAsJson(dateTimePicker).get("timeLabel")
                        .asString());

        dateTimePicker.setTimeAriaLabel(null);
        Assertions.assertEquals("I18n time",
                getI18nPropertyAsJson(dateTimePicker).get("timeLabel")
                        .asString());
    }

    private ObjectNode getI18nPropertyAsJson(DateTimePicker dateTimePicker) {
        return (ObjectNode) dateTimePicker.getElement().getPropertyRaw("i18n");
    }
}
