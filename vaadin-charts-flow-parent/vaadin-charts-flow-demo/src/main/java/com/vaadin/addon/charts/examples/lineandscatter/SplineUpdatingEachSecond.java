package com.vaadin.addon.charts.examples.lineandscatter;

import java.util.Random;

import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;

public class SplineUpdatingEachSecond extends AbstractChartExample {

    @Override public void initDemo() {
        final Random random = new Random();

        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getTitle().setText("Live random data");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);
        xAxis.setTickPixelInterval(150);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("Value"));

        configuration.getTooltip().setEnabled(false);
        configuration.getLegend().setEnabled(false);

        final DataSeries series = new DataSeries();
        series.setPlotOptions(new PlotOptionsSpline());
        series.setName("Random data");
        for (int i = -19; i <= 0; i++) {
            series.add(new DataSeriesItem(System.currentTimeMillis() + i * 1000, random.nextDouble()));
        }

        configuration.setSeries(series);

        runWhileAttached(chart, () -> {
                final long x = System.currentTimeMillis();
                final double y = random.nextDouble();
                series.add(new DataSeriesItem(x, y), true, true);
                chart.drawChart();
        }, 1000, 1000);

        add(chart);
    }
}
