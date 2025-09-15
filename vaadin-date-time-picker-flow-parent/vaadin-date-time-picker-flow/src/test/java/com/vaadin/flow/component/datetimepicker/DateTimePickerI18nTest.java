/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DateTimePickerI18nTest {

    private DateTimePicker dateTimePicker;

    @Before
    public void setup() {
        dateTimePicker = new DateTimePicker();
    }

    @Test
    public void setDateAriaLabel_removeDateAriaLabel() {
        dateTimePicker.setDateAriaLabel("Custom date");
        Assert.assertEquals("Custom date", getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel").asText());

        dateTimePicker.setDateAriaLabel(null);
        Assert.assertTrue(getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel") instanceof NullNode);
    }

    @Test
    public void setDateAriaLabel_setI18n() {
        dateTimePicker.setDateAriaLabel("Custom date");

        dateTimePicker.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date"));
        Assert.assertEquals("Custom date", getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel").asText());
    }

    @Test
    public void setTimeAriaLabel_removeTimeAriaLabel() {
        dateTimePicker.setTimeAriaLabel("Custom time");
        Assert.assertEquals("Custom time", getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel").asText());

        dateTimePicker.setTimeAriaLabel(null);
        Assert.assertTrue(getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel") instanceof NullNode);
    }

    @Test
    public void setTimeAriaLabel_setI18n() {
        dateTimePicker.setTimeAriaLabel("Custom time");

        dateTimePicker.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setTimeLabel("I18n time"));
        Assert.assertEquals("Custom time", getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel").asText());
    }

    @Test
    public void setI18n() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        Assert.assertEquals("I18n date", getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel").asText());
        Assert.assertEquals("I18n time", getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel").asText());
    }

    @Test
    public void setI18n_setDateAriaLabel_removeDateAriaLabel() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        dateTimePicker.setDateAriaLabel("Custom date");
        Assert.assertEquals("Custom date", getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel").asText());

        dateTimePicker.setDateAriaLabel(null);
        Assert.assertEquals("I18n date", getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel").asText());
    }

    @Test
    public void setI18n_setTimeAriaLabel_removeTimeAriaLabel() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        dateTimePicker.setTimeAriaLabel("Custom time");
        Assert.assertEquals("Custom time", getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel").asText());

        dateTimePicker.setTimeAriaLabel(null);
        Assert.assertEquals("I18n time", getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel").asText());
    }

    private ObjectNode getI18nPropertyAsJson(DateTimePicker dateTimePicker) {
        return (ObjectNode) dateTimePicker.getElement().getPropertyRaw("i18n");
    }
}
