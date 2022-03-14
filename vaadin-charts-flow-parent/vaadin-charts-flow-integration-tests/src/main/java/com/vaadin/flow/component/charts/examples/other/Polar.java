package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

public class Polar extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration conf = chart.getConfiguration();
        conf.getChart().setPolar(true);
        conf.setTitle("Polar Chart");

        Pane pane = new Pane(0, 360);
        conf.addPane(pane);

        XAxis xAxis = new XAxis();
        xAxis.setTickInterval(45);
        xAxis.setMin(0);
        xAxis.setMax(360);
        Labels labels = new Labels();
        labels.setFormatter("function() {return this.value + '°';}");
        xAxis.setLabels(labels);
        YAxis yAxis = new YAxis();
        yAxis.setMin(0);
        conf.addxAxis(xAxis);
        conf.addyAxis(yAxis);

        PlotOptionsSeries series = new PlotOptionsSeries();
        PlotOptionsColumn column = new PlotOptionsColumn();
        series.setPointStart(0);
        series.setPointInterval(45);
        column.setPointPadding(0);
        column.setGroupPadding(0);

        conf.setPlotOptions(series, column);

        ListSeries col = new ListSeries(8, 7, 6, 5, 4, 3, 2, 1);
        ListSeries line = new ListSeries(1, 2, 3, 4, 5, 6, 7, 8);
        ListSeries area = new ListSeries(1, 8, 2, 7, 3, 6, 4, 5);

        col.setPlotOptions(new PlotOptionsColumn());
        col.setName(ChartType.COLUMN.toString());

        line.setPlotOptions(new PlotOptionsLine());
        line.setName(ChartType.LINE.toString());

        area.setPlotOptions(new PlotOptionsArea());
        area.setName(ChartType.AREA.toString());

        conf.setSeries(col, line, area);

        add(chart);
    }
}
