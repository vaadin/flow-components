/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.lineandscatter;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DragDrop;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@SkipFromDemo
public class LineDragAndDrop extends AbstractChartExample {
    private VerticalLayout logPane;

    @Override
    public void initDemo() {
        final Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.setTooltip(new Tooltip());

        configuration.setTitle("Solar Employment Growth by Sector, 2010-2016");
        configuration.setSubTitle("With the ability to drag and drop points");

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle("Number of Employees");

        Legend legend = configuration.getLegend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setVerticalAlign(VerticalAlign.MIDDLE);
        legend.setAlign(HorizontalAlign.RIGHT);

        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setPointStart(2010);
        configureDragAndDrop(plotOptionsSeries);
        configuration.setPlotOptions(plotOptionsSeries);

        configuration.addSeries(new ListSeries("Installation", 43934.5, 52503,
                57177, 69658, 97031, 119931, 137133, 154175));

        add(chart);
        configureEvents(chart);

        add(chart);
        logPane = new VerticalLayout();
        logPane.setSpacing(false);
        add(logPane);
    }

    private void configureDragAndDrop(PlotOptionsSeries plotOptions) {
        final DragDrop dragDrop = plotOptions.getDragDrop();
        dragDrop.setDraggableX(true);
        dragDrop.setDraggableY(true);
    }

    private void configureEvents(Chart chart) {
        chart.addPointDragStartListener(event -> {
            logMessage("Task with id " + event.getCategory() + " dragging started:" + event.getxValue() + ", " + event.getyValue());
        });

        chart.addPointDropListener(event -> {
            logMessage("Task with id " + event.getCategory() + " dropped:" + event.getxValue() + ", " + event.getyValue());
        });
    }

    private void logMessage(String message) {
        logPane.add(new Div(message));
        if (logPane.getComponentCount() > 10) {
            logPane.remove(logPane.getComponentAt(0));
        }
    }
}
