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
import java.time.temporal.ChronoUnit;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartMode;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.AxisGrid;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.TimeUnit;
import com.vaadin.flow.component.charts.model.TimeUnitMultiples;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.Style;

@SuppressWarnings("unused")
@SkipFromDemo
public class GanttCustomXAxis extends AbstractChartExample {

    private static final Instant TODAY = Instant.now()
            .truncatedTo(ChronoUnit.DAYS);

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.GANTT);
        chart.setMode(ChartMode.GANTT);

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setHeight("330");

        configuration.setTitle("Custom XAxis Demo");
        configuration.setSubTitle("Showing years, months and week numbers");
        configuration.setTooltip(new Tooltip(true));

        configureYearsMonthsWeeksOnXAxis(configuration);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setCategories("Prototyping", "Development", "Testing");

        PlotOptionsGantt plotOptionsGantt = new PlotOptionsGantt();
        configuration.setPlotOptions(plotOptionsGantt);

        final GanttSeries projectDevelopmentSeries = createProjectDevelopmentSeries();
        configuration.addSeries(projectDevelopmentSeries);

        add(chart);
    }

    private void configureYearsMonthsWeeksOnXAxis(Configuration configuration) {
        configureWeeksAxis(configuration);
        configureMonthsAxis(configuration);
        configureYearsAxis(configuration);
    }

    private void configureWeeksAxis(Configuration configuration) {
        XAxis axis = new XAxis();
        configuration.addxAxis(axis);

        axis.setMinPadding(0.02);
        axis.setMaxPadding(0.02);
        axis.setUnits(new TimeUnitMultiples(TimeUnit.WEEK, 1));

        final Labels labels = new Labels();
        labels.setPadding(1);
        labels.setAlign(HorizontalAlign.LEFT);
        var style = new Style();
        style.setFontSize("8px");
        labels.setStyle(style);
        axis.setLabels(labels);

        axis.setGrid(new AxisGrid());
        axis.getGrid().setCellHeight(20);
    }

    private void configureMonthsAxis(Configuration configuration) {
        XAxis axis = new XAxis();
        configuration.addxAxis(axis);
        axis.setTickInterval(1000 * 60 * 60 * 24 * 30L);
        axis.setUnits(new TimeUnitMultiples(TimeUnit.MONTH, 1));

        final Labels labels = new Labels();
        labels.setAlign(HorizontalAlign.LEFT);
        var style = new Style();
        style.setFontSize("8px");
        labels.setStyle(style);
        axis.setLabels(labels);

        axis.setGrid(new AxisGrid());
        axis.getGrid().setCellHeight(20);
    }

    private void configureYearsAxis(Configuration configuration) {
        XAxis axis = new XAxis();
        configuration.addxAxis(axis);
        axis.setTickInterval(1000 * 60 * 60 * 24 * 365L);
        axis.setUnits(new TimeUnitMultiples(TimeUnit.YEAR, 1));

        final Labels labels = new Labels();
        labels.setAlign(HorizontalAlign.LEFT);
        var style = new Style();
        style.setFontSize("8px");
        labels.setStyle(style);
        axis.setLabels(labels);

        axis.setGrid(new AxisGrid());
        axis.getGrid().setCellHeight(20);
    }

    private GanttSeries createProjectDevelopmentSeries() {
        GanttSeries series = new GanttSeries();
        series.setName("Project 1");

        GanttSeriesItem item;

        item = new GanttSeriesItem(0, startOfYearPlus(1), startOfYearPlus(3));
        item.setCustom(new TaskCustomData("JonArild"));
        series.add(item);

        item = new GanttSeriesItem(1, startOfYearPlus(4), startOfYearPlus(5));
        item.setCustom(new TaskCustomData("Oystein"));
        series.add(item);

        item = new GanttSeriesItem(1, startOfYearPlus(8), startOfYearPlus(10));
        item.setCustom(new TaskCustomData("JonArild"));
        series.add(item);

        item = new GanttSeriesItem(2, startOfYearPlus(5), startOfYearPlus(7));
        item.setCustom(new TaskCustomData("Torstein"));
        series.add(item);

        item = new GanttSeriesItem(2, startOfYearPlus(9), startOfYearPlus(11));
        item.setCustom(new TaskCustomData("Torstein"));
        series.add(item);

        return series;
    }

    private Instant startOfYearPlus(int months) {
        return TODAY.plus(months * 30L, ChronoUnit.DAYS);
    }

    @SuppressWarnings("unused")
    static class TaskCustomData extends AbstractConfigurationObject {
        private String assignee;

        public TaskCustomData(String assignee) {
            this.assignee = assignee;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }
    }
}
