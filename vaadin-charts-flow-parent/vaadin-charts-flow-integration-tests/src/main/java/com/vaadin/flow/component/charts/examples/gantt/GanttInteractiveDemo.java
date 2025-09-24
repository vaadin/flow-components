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
import java.util.ArrayList;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DragDrop;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@SuppressWarnings("unused")
@SkipFromDemo
public class GanttInteractiveDemo extends AbstractChartExample {

    private VerticalLayout logPane;

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.GANTT);
        configureEvents(chart);

        final Configuration configuration = chart.getConfiguration();
        var tooltip = new Tooltip(true);
        tooltip.setXDateFormat("%a %b %d, %H:%M");
        configuration.setTooltip(tooltip);

        configuration.setTitle("Interactive Gantt Chart");
        configuration.setSubTitle("Drag and drop points to edit");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setCurrentDateIndicator(true);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setType(AxisType.CATEGORY);
        yAxis.setCategories("Tech", "Marketing", "Sales");
        yAxis.setMin(0);
        yAxis.setMax(2);

        PlotOptionsGantt plotOptionsGantt = new PlotOptionsGantt();
        plotOptionsGantt.setAnimation(false); // Do not animate dependency
                                              // connectors
        configureDragAndDrop(plotOptionsGantt);

        plotOptionsGantt.setAllowPointSelect(true);
        configuration.setPlotOptions(plotOptionsGantt);

        final GanttSeries series = createSeries();
        configureDataLabels(series);
        configuration.addSeries(series);

        add(chart);
        logPane = new VerticalLayout();
        logPane.setSpacing(false);
        add(logPane);
    }

    private void configureEvents(Chart chart) {
        chart.addPointSelectListener(event -> {
            final GanttSeriesItem ganttSeriesItem = ((GanttSeries) event
                    .getSeries()).get(event.getItemIndex());
            var taskId = ganttSeriesItem.getId();
            logMessage("Task with id " + taskId + " was selected");
        });

        chart.addPointUnselectListener(event -> {
            final GanttSeriesItem ganttSeriesItem = ((GanttSeries) event
                    .getSeries()).get(event.getItemIndex());
            var taskId = ganttSeriesItem.getId();
            logMessage("Task with id " + taskId + " was deselected");
        });

        chart.addPointDragStartListener(event -> {
            final GanttSeriesItem ganttSeriesItem = ((GanttSeries) event
                    .getSeries()).get(event.getItemIndex());
            var taskId = ganttSeriesItem.getId();
            logMessage("Task with id " + taskId + " dragging started: "
                    + event.getStart() + " - " + event.getEnd());
        });

        chart.addPointDropListener(event -> {
            final GanttSeriesItem ganttSeriesItem = ((GanttSeries) event
                    .getSeries()).get(event.getItemIndex());
            var taskId = ganttSeriesItem.getId();
            logMessage("Task with id " + taskId + " was dropped: "
                    + event.getStart() + " - " + event.getEnd());
        });
    }

    private void logMessage(String message) {
        logPane.add(new Div(message));
        if (logPane.getComponentCount() > 10) {
            logPane.remove(logPane.getComponentAt(0));
        }
    }

    private void configureDataLabels(GanttSeries series) {
        var plotOptions = new PlotOptionsGantt();
        var dataLabels = new ArrayList<DataLabels>();
        var pointNameDataLabel = new DataLabels(true);
        pointNameDataLabel.setAlign(HorizontalAlign.CENTER);
        pointNameDataLabel.setFormat("{point.name}");
        dataLabels.add(pointNameDataLabel);
        plotOptions.setDataLabels(dataLabels);
        series.setPlotOptions(plotOptions);
    }

    private void configureDragAndDrop(PlotOptionsGantt plotOptions) {
        final DragDrop dragDrop = plotOptions.getDragDrop();
        dragDrop.setDraggableX(true);
        dragDrop.setDraggableY(true);
        dragDrop.setDragMinY(0);
        dragDrop.setDragMaxY(2);
        dragDrop.setDragPrecisionX(1000 * 60 * 60 * 8); // Snap to eight hours
    }

    private GanttSeries createSeries() {
        GanttSeries series = new GanttSeries();
        final GanttSeriesItem tech1 = new GanttSeriesItem(0,
                Instant.parse("2014-10-18T00:00:00Z"),
                Instant.parse("2014-10-20T00:00:00Z"));
        tech1.setId("tech1");
        tech1.setName("Prototype");
        series.add(tech1);

        final GanttSeriesItem tech2 = new GanttSeriesItem();
        tech2.setMilestone(true);
        tech2.setStart(Instant.parse("2014-10-22T00:00:00Z"));
        tech2.setY(0);
        tech2.setId("tech2");
        tech2.addDependency("tech1");
        tech1.setName("Prototype done");
        series.add(tech2);

        final GanttSeriesItem tech3 = new GanttSeriesItem(0,
                Instant.parse("2014-10-24T00:00:00Z"),
                Instant.parse("2014-10-27T00:00:00Z"));
        tech3.setId("tech3");
        tech3.setName("Testing");
        tech3.addDependency("tech2");

        series.add(tech3);

        final GanttSeriesItem marketing1 = new GanttSeriesItem(1,
                Instant.parse("2014-10-20T00:00:00Z"),
                Instant.parse("2014-10-23T00:00:00Z"));
        marketing1.setId("marketing1");
        marketing1.setName("Product pages");
        series.add(marketing1);

        final GanttSeriesItem marketing2 = new GanttSeriesItem(1,
                Instant.parse("2014-10-25T00:00:00Z"),
                Instant.parse("2014-10-29T00:00:00Z"));
        marketing2.setId("marketing2");
        marketing2.setName("Newsletter");
        series.add(marketing2);

        final GanttSeriesItem sales1 = new GanttSeriesItem(2,
                Instant.parse("2014-10-23T00:00:00Z"),
                Instant.parse("2014-10-26T00:00:00Z"));
        sales1.setId("sales1");
        sales1.setName("Licensing");
        series.add(sales1);
        return series;
    }
}
