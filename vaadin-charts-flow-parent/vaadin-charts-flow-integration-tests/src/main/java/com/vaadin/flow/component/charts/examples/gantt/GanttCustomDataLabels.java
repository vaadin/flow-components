/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.gantt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

@SuppressWarnings("unused")
public class GanttCustomDataLabels extends AbstractChartExample {

    private static final Instant TODAY = Instant.now()
            .truncatedTo(ChronoUnit.DAYS);

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.GANTT);

        final Configuration configuration = chart.getConfiguration();

        configuration.setTitle("Highcharts Gantt Chart");
        configuration.setSubTitle("With custom symbols in data labels");
        configuration.setTooltip(new Tooltip(true));

        XAxis xAxis = configuration.getxAxis();
        xAxis.setMinPadding(0.05);
        xAxis.setMaxPadding(0.05);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setCategories("Prototyping", "Development", "Testing");

        PlotOptionsGantt plotOptionsGantt = new PlotOptionsGantt();
        configuration.setPlotOptions(plotOptionsGantt);

        final GanttSeries projectDevelopmentSeries = createProjectDevelopmentSeries();
        PlotOptionsGantt seriesPlotOptions = new PlotOptionsGantt();
        var dataLabels = new ArrayList<DataLabels>();

        var assigneeLabel = new DataLabels(true);
        assigneeLabel.setAlign(HorizontalAlign.LEFT);
        assigneeLabel.setFormat("{point.custom.assignee}");
        dataLabels.add(assigneeLabel);

        var avatarLabel = new DataLabels(true);
        avatarLabel.setAlign(HorizontalAlign.LEFT);
        avatarLabel.setUseHTML(true);
        avatarLabel.setFormat(
                "<div style=\"width: 20px; height: 20px; overflow: hidden; margin-left: -30px\">"
                        + "                <img src=\"https://ui-avatars.com/api/?background=random&color=fff&size=20&length=1&rounded=true&name={point.custom.assignee}\"> "
                        + "                </div>");
        dataLabels.add(avatarLabel);

        seriesPlotOptions.setDataLabels(dataLabels);
        projectDevelopmentSeries.setPlotOptions(seriesPlotOptions);
        configuration.addSeries(projectDevelopmentSeries);

        add(chart);
    }

    private GanttSeries createProjectDevelopmentSeries() {
        GanttSeries series = new GanttSeries();
        series.setName("Project 1");

        GanttSeriesItem item;

        item = new GanttSeriesItem(0, todayPlus(1), todayPlus(3));
        item.setCustom(new TaskCustomData("JonArild"));
        series.add(item);

        item = new GanttSeriesItem(1, todayPlus(2), todayPlus(5));
        item.setCustom(new TaskCustomData("Oystein"));
        series.add(item);

        item = new GanttSeriesItem(2, todayPlus(5), todayPlus(7));
        item.setCustom(new TaskCustomData("Torstein"));
        series.add(item);

        item = new GanttSeriesItem(1, todayPlus(8), todayPlus(16));
        item.setCustom(new TaskCustomData("JonArild"));
        series.add(item);

        item = new GanttSeriesItem(2, todayPlus(10), todayPlus(23));
        item.setCustom(new TaskCustomData("Torstein"));
        series.add(item);

        return series;
    }

    private Instant todayPlus(int days) {
        return TODAY.plus(days, ChronoUnit.DAYS);
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
