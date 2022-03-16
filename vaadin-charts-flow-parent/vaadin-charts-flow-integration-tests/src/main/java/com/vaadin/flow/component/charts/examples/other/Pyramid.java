package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabelsFunnel;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsPyramid;

public class Pyramid extends AbstractChartExample {
    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.PYRAMID);

        Configuration configuration = chart.getConfiguration();

        configuration.setTitle("Sales pyramid");
        configuration.getLegend().setEnabled(false);

        PlotOptionsPyramid plotOptionsSeries = new PlotOptionsPyramid();
        DataLabelsFunnel dataLabels = plotOptionsSeries.getDataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setSoftConnector(true);
        dataLabels.setFormat("<b>{point.name}</b> ({point.y:,.0f})");

        plotOptionsSeries.setCenter("40%", "50%");
        plotOptionsSeries.setWidth("60%");

        configuration.setPlotOptions(plotOptionsSeries);

        DataSeries series = new DataSeries("Unique users");

        series.add(new DataSeriesItem("Website visits", 15654));
        series.add(new DataSeriesItem("Downloads", 4064));
        series.add(new DataSeriesItem("Requested price list", 1987));
        series.add(new DataSeriesItem("Invoice sent", 976));
        series.add(new DataSeriesItem("Finalized", 846));

        configuration.addSeries(series);

        add(chart);
    }
}
