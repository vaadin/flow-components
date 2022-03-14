package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabelsFunnel;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.HorizontalAlign;
import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsFunnel;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.SerializableSupplier;

import java.util.Random;

@SkipFromDemo
public class DynamicChangingChart extends AbstractChartExample {
    final Random random = new Random(0); // NOSONAR

    @Override
    public void initDemo() {

        final Chart chart = new Chart();
        chart.setConfiguration(getFunnelConfiguration());
        add(chart);
        add(createConfigurationButton(chart, "funnel",
                this::getFunnelConfiguration));
        add(createConfigurationButton(chart, "polar",
                this::getPolarConfiguration));
        add(createConfigurationButton(chart, "line",
                this::getLineConfiguration));
    }

    private Component createConfigurationButton(Chart chart, String type,
            SerializableSupplier<Configuration> configurationSupplier) {
        final NativeButton button = new NativeButton(
                String.format("Set %s configuration", type),
                e -> chart.setConfiguration(configurationSupplier.get()));
        button.setId(String.format("set_%s_button", type));
        return button;
    }

    private Configuration getFunnelConfiguration() {
        DataSeries dataSeries = new DataSeries("Unique users");
        dataSeries.add(new DataSeriesItem("Website visits", 15654));
        dataSeries.add(new DataSeriesItem("Downloads", 4064));
        dataSeries.add(new DataSeriesItem("Requested price list", 1987));
        dataSeries.add(new DataSeriesItem("Invoice sent", 976));
        dataSeries.add(new DataSeriesItem("Finalized", 846));

        Configuration configuration = new Configuration();

        configuration.setTitle("Sales funnel");
        configuration.getLegend().setEnabled(false);

        PlotOptionsFunnel options = new PlotOptionsFunnel();
        options.setReversed(false);
        options.setNeckWidth("30%");
        options.setNeckHeight("30%");

        options.setWidth("70%");

        DataLabelsFunnel dataLabels = new DataLabelsFunnel();
        dataLabels.setFormat("<b>{point.name}</b> ({point.y:,.0f})");
        options.setDataLabels(dataLabels);

        dataSeries.setPlotOptions(options);
        configuration.addSeries(dataSeries);

        return configuration;
    }

    public Configuration getPolarConfiguration() {

        Configuration configuration = new Configuration();
        configuration.getChart().setPolar(true);
        configuration.setTitle("Polar Chart");

        Pane pane = new Pane(0, 360);
        configuration.addPane(pane);

        XAxis xAxis = new XAxis();
        xAxis.setTickInterval(45);
        xAxis.setMin(0);
        xAxis.setMax(360);
        Labels labels = new Labels();
        labels.setFormatter("function() {return this.value + '°';}");
        xAxis.setLabels(labels);
        YAxis yAxis = new YAxis();
        yAxis.setMin(0);
        configuration.addxAxis(xAxis);
        configuration.addyAxis(yAxis);

        PlotOptionsSeries series = new PlotOptionsSeries();
        PlotOptionsColumn column = new PlotOptionsColumn();
        series.setPointStart(0);
        series.setPointInterval(45);
        column.setPointPadding(0);
        column.setGroupPadding(0);

        configuration.setPlotOptions(series, column);

        ListSeries col = new ListSeries(8, 7, 6, 5, 4, 3, 2, 1);
        ListSeries line = new ListSeries(1, 2, 3, 4, 5, 6, 7, 8);
        ListSeries area = new ListSeries(1, 8, 2, 7, 3, 6, 4, 5);

        col.setPlotOptions(new PlotOptionsColumn());
        col.setName(ChartType.COLUMN.toString());

        line.setPlotOptions(new PlotOptionsLine());
        line.setName(ChartType.LINE.toString());

        area.setPlotOptions(new PlotOptionsArea());
        area.setName(ChartType.AREA.toString());

        configuration.setSeries(col, line, area);

        return configuration;
    }

    private Configuration getLineConfiguration() {

        Configuration configuration = new Configuration();

        configuration.setTitle("Solar Employment Growth by Sector, 2010-2016");
        configuration.setSubTitle("Source: thesolarfoundation.com");

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle("Number of Employees");

        Legend legend = configuration.getLegend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setVerticalAlign(VerticalAlign.MIDDLE);
        legend.setAlign(HorizontalAlign.RIGHT);

        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setPointStart(2010);
        configuration.setPlotOptions(plotOptionsSeries);

        configuration.addSeries(new ListSeries("Installation", 43934, 52503,
                57177, 69658, 97031, 119931, 137133, 154175));
        configuration.addSeries(new ListSeries("Manufacturing", 24916, 24064,
                29742, 29851, 32490, 30282, 38121, 40434));
        configuration.addSeries(new ListSeries("Sales & Distribution", 11744,
                17722, 16005, 19771, 20185, 24377, 32147, 39387));
        configuration.addSeries(new ListSeries("Project Development", null,
                null, 7988, 12169, 15112, 22452, 34400, 34227));
        configuration.addSeries(new ListSeries("Other", 12908, 5948, 8105,
                11248, 8989, 11816, 18274, 18111));

        return configuration;
    }
}
