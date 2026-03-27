/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Series;

/**
 * Tests for the {@link Configuration}
 *
 */
class ConfigurationTest {

    @Test
    void configurationSetSeriesWithArraysAsListTest() {
        Configuration conf = new Configuration();

        conf.setSeries(Arrays.asList(new ListSeries()));
        conf.addSeries(new ListSeries());

        assertEquals(2, conf.getSeries().size());
    }

    @Test
    void configurationSetSeriesWithListShouldMakeShallowCopyTest() {
        Configuration conf = new Configuration();

        List<Series> series = new ArrayList<>();
        conf.setSeries(series);

        series.add(new ListSeries());

        assertEquals(0, conf.getSeries().size());
    }
}
