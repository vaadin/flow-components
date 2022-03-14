package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PointPlacement;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

public class WindRose extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.COLUMN);

        Configuration conf = chart.getConfiguration();

        conf.getChart().setPolar(true);

        conf.setTitle("Wind rose for South Shore Met Station, Oregon");
        conf.setSubTitle("Source: or.water.usgs.gov");

        XAxis xAxis = conf.getxAxis();
        xAxis.setCategories("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW");

        YAxis yAxis = conf.getyAxis();
        yAxis.setReversedStacks(false);
        yAxis.setTitle("Frequency (%)");
        yAxis.setMin(0);
        yAxis.setEndOnTick(false);
        yAxis.setShowLastLabel(true);
        yAxis.getLabels()
                .setFormatter("function() { return this.value + '%';}");

        conf.getTooltip().setValueSuffix("%");

        conf.getPane().setSize("85%");

        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setStacking(Stacking.NORMAL);
        plotOptionsColumn.setGroupPadding(0);
        plotOptionsColumn.setPointPlacement(PointPlacement.ON);
        conf.setPlotOptions(plotOptionsColumn);

        Legend legend = conf.getLegend();
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setY(100);
        legend.setLayout(LayoutDirection.VERTICAL);

        DataSeries series = new DataSeries();
        series.setName("&lt; 0.5 m/s");
        series.setData(1.81, 0.62, 0.82, 0.59, 0.62, 1.22, 1.61, 2.04, 2.66,
                2.96, 2.53, 1.97, 1.64, 1.32, 1.58, 1.51);
        conf.addSeries(series);

        series = new DataSeries();
        series.setName("0.5-2 m/s");
        series.setData(1.78, 1.09, 0.82, 1.22, 2.2, 2.01, 3.06, 3.42, 4.74,
                4.14, 4.01, 2.66, 1.71, 2.4, 4.28, 5);
        conf.addSeries(series);

        series = new DataSeries();
        series.setName("2-4 m/s");
        series.setData(0.16, 0, 0.07, 0.07, 0.49, 1.55, 2.37, 1.97, 0.43, 0.26,
                1.22, 1.97, 0.92, 0.99, 1.28, 1.32);
        conf.addSeries(series);

        series = new DataSeries();
        series.setName("4-6 m/s");
        series.setData(0, 0, 0, 0, 0, 0.3, 2.14, 0.86, 0, 0, 0.49, 0.79, 1.45,
                1.61, 0.76, 0.13);
        conf.addSeries(series);

        series = new DataSeries();
        series.setName("6-8 m/s");
        series.setData(0, 0, 0, 0, 0, 0.13, 1.74, 0.53, 0, 0, 0.13, 0.3, 0.26,
                0.33, 0.66, 0.23);
        conf.addSeries(series);

        series = new DataSeries();
        series.setName("8-10 m/s");
        series.setData(0, 0, 0, 0, 0, 0, 0.39, 0.49, 0, 0, 0, 0, 0.1, 0, 0.69,
                0.13);
        conf.addSeries(series);

        series = new DataSeries();
        series.setName("&gt; 10 m/s");
        series.setData(0, 0, 0, 0, 0, 0, 0.13, 0, 0, 0, 0, 0, 0, 0, 0.03, 0.07);
        conf.addSeries(series);

        add(chart);
    }
}
