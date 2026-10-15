/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import elemental.json.JsonNull;
import elemental.json.JsonObject;

public class DateTimePickerI18nTest {

    private DateTimePicker dateTimePicker;

    @Before
    public void setup() {
        dateTimePicker = new DateTimePicker();
    }

    @Test
    public void setDateAriaLabel_removeDateAriaLabel() {
        dateTimePicker.setDateAriaLabel("Custom date");
        Assert.assertEquals("Custom date",
                getI18nPropertyAsJson(dateTimePicker).getString("dateLabel"));

        dateTimePicker.setDateAriaLabel(null);
        Assert.assertTrue(getI18nPropertyAsJson(dateTimePicker)
                .get("dateLabel") instanceof JsonNull);
    }

    @Test
    public void setDateAriaLabel_setI18n() {
        dateTimePicker.setDateAriaLabel("Custom date");

        dateTimePicker.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date"));
        Assert.assertEquals("Custom date",
                getI18nPropertyAsJson(dateTimePicker).getString("dateLabel"));
    }

    @Test
    public void setTimeAriaLabel_removeTimeAriaLabel() {
        dateTimePicker.setTimeAriaLabel("Custom time");
        Assert.assertEquals("Custom time",
                getI18nPropertyAsJson(dateTimePicker).getString("timeLabel"));

        dateTimePicker.setTimeAriaLabel(null);
        Assert.assertTrue(getI18nPropertyAsJson(dateTimePicker)
                .get("timeLabel") instanceof JsonNull);
    }

    @Test
    public void setTimeAriaLabel_setI18n() {
        dateTimePicker.setTimeAriaLabel("Custom time");

        dateTimePicker.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setTimeLabel("I18n time"));
        Assert.assertEquals("Custom time",
                getI18nPropertyAsJson(dateTimePicker).getString("timeLabel"));
    }

    @Test
    public void setI18n() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        Assert.assertEquals("I18n date",
                getI18nPropertyAsJson(dateTimePicker).getString("dateLabel"));
        Assert.assertEquals("I18n time",
                getI18nPropertyAsJson(dateTimePicker).getString("timeLabel"));
    }

    @Test
    public void setI18n_setDateAriaLabel_removeDateAriaLabel() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        dateTimePicker.setDateAriaLabel("Custom date");
        Assert.assertEquals("Custom date",
                getI18nPropertyAsJson(dateTimePicker).getString("dateLabel"));

        dateTimePicker.setDateAriaLabel(null);
        Assert.assertEquals("I18n date",
                getI18nPropertyAsJson(dateTimePicker).getString("dateLabel"));
    }

    @Test
    public void setI18n_setTimeAriaLabel_removeTimeAriaLabel() {
        DateTimePicker.DateTimePickerI18n i18n = new DateTimePicker.DateTimePickerI18n()
                .setDateLabel("I18n date").setTimeLabel("I18n time");
        dateTimePicker.setI18n(i18n);

        dateTimePicker.setTimeAriaLabel("Custom time");
        Assert.assertEquals("Custom time",
                getI18nPropertyAsJson(dateTimePicker).getString("timeLabel"));

        dateTimePicker.setTimeAriaLabel(null);
        Assert.assertEquals("I18n time",
                getI18nPropertyAsJson(dateTimePicker).getString("timeLabel"));
    }

    private JsonObject getI18nPropertyAsJson(DateTimePicker dateTimePicker) {
        return (JsonObject) dateTimePicker.getElement().getPropertyRaw("i18n");
    }
}
