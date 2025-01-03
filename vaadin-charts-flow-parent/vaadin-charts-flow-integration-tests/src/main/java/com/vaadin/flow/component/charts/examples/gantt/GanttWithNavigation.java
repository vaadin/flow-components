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
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Completed;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.Navigator;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;

@SuppressWarnings("unused")
public class GanttWithNavigation extends AbstractChartExample {
    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.GANTT);
        chart.setMode(ChartMode.GANTT);

        final Configuration configuration = chart.getConfiguration();

        configuration.setTitle("Gantt Chart with Navigation");

        YAxis yAxis = configuration.getyAxis();
        yAxis.setUniqueNames(true);

        configureNavigator(configuration);

        configuration.getScrollbar().setEnabled(true);

        configuration.getRangeSelector().setEnabled(true);
        configuration.getRangeSelector().setSelected(0);

        PlotOptionsGantt plotOptionsGantt = new PlotOptionsGantt();
        configuration.setPlotOptions(plotOptionsGantt);

        final GanttSeries series = createProjectManagementSeries();
        configuration.addSeries(series);

        add(chart);
    }

    private GanttSeries createProjectManagementSeries() {
        GanttSeries series = new GanttSeries();
        series.setName("Project 1");
        final GanttSeriesItem startPrototype = new GanttSeriesItem(
                "Start prototype", Instant.parse("2013-10-18T00:00:00Z"),
                Instant.parse("2014-10-25T00:00:00Z"));
        startPrototype.setCompleted(0.25);
        series.add(startPrototype);

        series.add(new GanttSeriesItem("Test prototype",
                Instant.parse("2014-02-27T00:00:00Z"),
                Instant.parse("2014-10-29T00:00:00Z")));

        final GanttSeriesItem develop = new GanttSeriesItem("Develop",
                Instant.parse("2014-10-20T00:00:00Z"),
                Instant.parse("2014-10-25T00:00:00Z"));
        develop.setCompleted(new Completed(0.12, SolidColor.ORANGE));
        series.add(develop);

        series.add(new GanttSeriesItem("Run acceptance tests",
                Instant.parse("2014-10-23T00:00:00Z"),
                Instant.parse("2014-10-26T00:00:00Z")));
        return series;
    }

    private void configureNavigator(Configuration configuration) {
        final Navigator navigator = configuration.getNavigator();
        navigator.setEnabled(true);
        final PlotOptionsGantt navigatorPlotOptions = new PlotOptionsGantt();
        navigatorPlotOptions.setPointPadding(0.2);
        navigator.setSeries(navigatorPlotOptions);
        final YAxis navigatorYAxis = navigator.getYAxis();
        navigatorYAxis.setMin(0);
        navigatorYAxis.setMax(3);
        navigatorYAxis.setReversed(true);
        navigatorYAxis.setCategories();
    }
}
