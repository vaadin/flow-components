/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.charts.typetests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DashStyle;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.spreadsheet.tests.charts.ChartTestBase;

class RadarTest extends ChartTestBase {

    private Number[][] chartData = {
            { 0, 0, 0, 0, 0, 0, 0, 1500, 5000, 8500, 3500, 500 },
            { 2500, 5500, 9000, 6500, 3500, 0, 0, 0, 0, 0, 0, 0 },
            { 500, 750, 1500, 2000, 5500, 7500, 8500, 7000, 3500, 2500, 500,
                    100 },
            { 0, 1500, 2500, 4000, 3500, 1500, 800, 550, 2500, 6000, 5500,
                    3000 }

    };

    @Test
    void notFilledRadar() throws Exception {
        Configuration conf = getChartFromSampleFile("Type Sample - Radar.xlsx",
                "G14").getConfiguration();
        assertSeriesType(conf.getSeries(), ChartType.AREA);
        assertData(conf.getSeries(), chartData);
        Assertions.assertTrue(conf.getChart().getPolar());
        Assertions.assertNotNull(
                (((PlotOptionsArea) conf.getSeries().get(0).getPlotOptions())
                        .getFillColor() != null));
    }

    @Test
    void filledRadar() throws Exception {
        Configuration conf = getChartFromSampleFile(
                "Type Sample - Filled Radar.xlsx", "G14").getConfiguration();

        assertSeriesType(conf.getSeries(), ChartType.AREA);
        assertDashStyle(conf.getSeries(), DashStyle.SOLID);
        Assertions.assertTrue(conf.getChart().getPolar());
        Assertions.assertNull(
                ((PlotOptionsArea) conf.getSeries().get(0).getPlotOptions())
                        .getFillColor());

    }
}
