package com.vaadin.flow.component.charts.examples.combinations;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Dimension;
import com.vaadin.flow.component.charts.model.PlotOptionsPolygon;
import com.vaadin.flow.component.charts.model.PlotOptionsScatter;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

import java.util.Random;

public class ScatterAndPolygon extends AbstractChartExample {
    @Override
    public void initDemo() {
        final Chart chart = new Chart(ChartType.SCATTER);

        Configuration conf = chart.getConfiguration();

        chart.setId("chart");
        conf.getChart().setZoomType(Dimension.XY);
        conf.disableCredits();
        conf.setTitle("Height vs Weight");
        conf.setSubTitle("Polygon series in Vaadin Charts.");

        Tooltip tooltip = conf.getTooltip();
        tooltip.setHeaderFormat("<b>{series.name}</b><br>");
        tooltip.setPointFormat("{point.x} cm, {point.y} kg");

        XAxis xAxis = conf.getxAxis();
        xAxis.setStartOnTick(true);
        xAxis.setEndOnTick(true);
        xAxis.setShowLastLabel(true);
        xAxis.setTitle("Height (cm)");

        YAxis yAxis = conf.getyAxis();
        yAxis.setTitle("Weight (kg)");

        PlotOptionsScatter plotOptionsScatter = new PlotOptionsScatter();
        DataSeries scatter = new DataSeries();
        scatter.setPlotOptions(plotOptionsScatter);
        scatter.setName("Observations");
        fillScatter(scatter);

        DataSeries polygon = new DataSeries();
        PlotOptionsPolygon plotOptionsPolygon = new PlotOptionsPolygon();
        plotOptionsPolygon.setEnableMouseTracking(false);
        polygon.setPlotOptions(plotOptionsPolygon);
        polygon.setName("Target");

        polygon.add(new DataSeriesItem(153, 42));
        polygon.add(new DataSeriesItem(149, 46));
        polygon.add(new DataSeriesItem(149, 55));
        polygon.add(new DataSeriesItem(152, 60));
        polygon.add(new DataSeriesItem(159, 70));
        polygon.add(new DataSeriesItem(170, 77));
        polygon.add(new DataSeriesItem(180, 70));
        polygon.add(new DataSeriesItem(180, 60));
        polygon.add(new DataSeriesItem(173, 52));
        polygon.add(new DataSeriesItem(166, 45));
        conf.addSeries(polygon);

        conf.addSeries(scatter);

        add(chart);
    }

    private void fillScatter(DataSeries series) {
        Random random = new Random(13); // NOSONAR
        for (int i = 0; i < 100; i++) {
            double x = random.nextDouble() * 30 + 150;
            double y = 60;
            if (random.nextBoolean()) {
                y += random.nextDouble() * 15;
                if (random.nextBoolean() && x > 170) {
                    y += random.nextDouble() * 30;
                }
            } else {
                y -= random.nextDouble() * 20;
            }
            series.add(new DataSeriesItem(x, y));
        }
    }
}
