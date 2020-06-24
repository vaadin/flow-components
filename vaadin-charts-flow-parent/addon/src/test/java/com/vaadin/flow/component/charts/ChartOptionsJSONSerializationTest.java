package com.vaadin.flow.component.charts;

import static com.vaadin.flow.component.charts.util.ChartSerialization.toJSON;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.util.ChartSerialization;

@RunWith(MockitoJUnitRunner.class)
public class ChartOptionsJSONSerializationTest {

    @Mock
    private UI ui;

    private ChartOptions options;

    @Before
    public void setup() {
        options = ChartOptions.get(ui);
    }

    @Test
    public void toJSON_NoLang_EmptyJsonSerialized() {
        assertEquals("{}", toJSON(options));
    }

    @Test
    public void toJSON_LangWithFinnishLocale_LocaleSerialized_Months() throws IOException {
        final String[] fiMonths=new String[] {"Tammikuu", "Helmikuu", "Maaliskuu",
                "Huhtikuu", "Toukokuu", "Kesäkuu", "Heinäkuu", "Elokuu",
                "Syyskuu", "Lokakuu", "Marraskuu", "Joulukuu"};
        final Lang fi = new Lang();
        fi.setMonths(fiMonths);

        options.setLang(fi);
        String json = toJSON(options);
        ObjectMapper om = ChartSerialization.createObjectMapper();
        ChartOptions chartOptions = om.readValue(json, ChartOptions.class);

        Assert.assertArrayEquals(fiMonths, chartOptions.getLang().getMonths());
    }

    @Test
    public void toJSON_LangWithFinnishLocale_LocaleSerialized_ShortMonths() throws IOException {
        final String[] fiShortMonths=new String[]{"Tammi", "Helmi", "Maalis", "Huhti",
                "Touko", "Kesä", "Heinä", "Elo", "Syys", "Loka", "Marras",
                "Joulu"};
        final Lang fi = new Lang();
        fi.setShortMonths(fiShortMonths);
        options.setLang(fi);
        String json = toJSON(options);
        ObjectMapper om = ChartSerialization.createObjectMapper();
        ChartOptions fromJson = om.readValue(json, ChartOptions.class);

        Assert.assertArrayEquals(fiShortMonths, fromJson.getLang().getShortMonths());
    }

    @Test
    public void toJSON_LangWithFinnishLocale_LocaleSerialized_Days() throws IOException {
        final String[] fiDays=new String[]{"Ma", "Ti", "Ke", "To", "Pe", "La", "Su"};
        final Lang fi = new Lang();
        fi.setWeekdays(fiDays);

        options.setLang(fi);
        String json = toJSON(options);
        ObjectMapper om = ChartSerialization.createObjectMapper();
        ChartOptions chartOptions = om.readValue(json, ChartOptions.class);

        Assert.assertArrayEquals(fiDays,  chartOptions.getLang().getWeekdays());
    }
}