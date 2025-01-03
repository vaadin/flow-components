/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.gantt;

import java.time.Instant;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartMode;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItemXrange;
import com.vaadin.flow.component.charts.model.PlotOptionsXrange;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

@SuppressWarnings("unused")
@SkipFromDemo
public class GanttUsingXRangeSeries extends AbstractChartExample {
    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.XRANGE);
        chart.setMode(ChartMode.GANTT);

        final Configuration configuration = chart.getConfiguration();

        configuration.setTitle("Gantt Chart using XRANGE series");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setMin(Instant.parse("2014-10-17T00:00:00Z"));
        xAxis.setMax(Instant.parse("2014-10-30T00:00:00Z"));

        YAxis yAxis = configuration.getyAxis();
        yAxis.setCategories("Start prototype", "Test prototype", "Develop",
                "Run acceptance tests");

        PlotOptionsXrange plotOptionsXrange = new PlotOptionsXrange();
        configuration.setPlotOptions(plotOptionsXrange);

        DataSeries series = new DataSeries();
        series.setName("Project 1");
        final DataSeriesItemXrange startPrototype = new DataSeriesItemXrange(
                Instant.parse("2014-10-18T00:00:00Z"),
                Instant.parse("2014-10-25T00:00:00Z"), 0);
        series.add(startPrototype);

        series.add(
                new DataSeriesItemXrange(Instant.parse("2014-10-27T00:00:00Z"),
                        Instant.parse("2014-10-29T00:00:00Z"), 1));

        final DataSeriesItemXrange develop = new DataSeriesItemXrange(
                Instant.parse("2014-10-20T00:00:00Z"),
                Instant.parse("2014-10-25T00:00:00Z"), 2);
        series.add(develop);

        series.add(
                new DataSeriesItemXrange(Instant.parse("2014-10-23T00:00:00Z"),
                        Instant.parse("2014-10-26T00:00:00Z"), 3));

        configuration.addSeries(series);

        add(chart);
    }
}
