/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static com.vaadin.flow.component.charts.util.ChartSerialization.toJSON;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.ObjectMapper;

class ChartOptionsJSONSerializationTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ChartOptions options;

    @BeforeEach
    void setup() {
        options = ChartOptions.get(ui.getUI());
    }

    @Test
    void toJSON_NoLang_EmptyJsonSerialized() {
        assertEquals("{}", toJSON(options));
    }

    @Test
    void toJSON_LangWithFinnishLocale_LocaleSerialized_Months() {
        final String[] fiMonths = new String[] { "Tammikuu", "Helmikuu",
                "Maaliskuu", "Huhtikuu", "Toukokuu", "Kesäkuu", "Heinäkuu",
                "Elokuu", "Syyskuu", "Lokakuu", "Marraskuu", "Joulukuu" };
        final Lang fi = new Lang();
        fi.setMonths(fiMonths);

        options.setLang(fi);
        String json = toJSON(options);
        ObjectMapper om = ChartSerialization.createObjectMapper();
        ChartOptions chartOptions = om.readValue(json, ChartOptions.class);

        assertArrayEquals(fiMonths, chartOptions.getLang().getMonths());
    }

    @Test
    void toJSON_LangWithFinnishLocale_LocaleSerialized_ShortMonths() {
        final String[] fiShortMonths = new String[] { "Tammi", "Helmi",
                "Maalis", "Huhti", "Touko", "Kesä", "Heinä", "Elo", "Syys",
                "Loka", "Marras", "Joulu" };
        final Lang fi = new Lang();
        fi.setShortMonths(fiShortMonths);
        options.setLang(fi);
        String json = toJSON(options);
        ObjectMapper om = ChartSerialization.createObjectMapper();
        ChartOptions fromJson = om.readValue(json, ChartOptions.class);

        assertArrayEquals(fiShortMonths, fromJson.getLang().getShortMonths());
    }

    @Test
    void toJSON_LangWithFinnishLocale_LocaleSerialized_Days() {
        final String[] fiDays = new String[] { "Ma", "Ti", "Ke", "To", "Pe",
                "La", "Su" };
        final Lang fi = new Lang();
        fi.setWeekdays(fiDays);

        options.setLang(fi);
        String json = toJSON(options);
        ObjectMapper om = ChartSerialization.createObjectMapper();
        ChartOptions chartOptions = om.readValue(json, ChartOptions.class);

        assertArrayEquals(fiDays, chartOptions.getLang().getWeekdays());
    }
}
