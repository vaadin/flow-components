package com.vaadin.flow.component.charts.examples.other;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.ColorAxis;
import com.vaadin.flow.component.charts.model.PlotOptionsTreemap;
import com.vaadin.flow.component.charts.model.TreeMapLayoutAlgorithm;
import com.vaadin.flow.component.charts.model.TreeSeries;
import com.vaadin.flow.component.charts.model.TreeSeriesItem;
import com.vaadin.flow.component.charts.model.style.SolidColor;

public class TreemapWithColorAxis extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.TREEMAP);

        ColorAxis colorAxis = new ColorAxis();
        colorAxis.setMinColor(new SolidColor("#FFFFFF"));
        colorAxis.setMaxColor(new SolidColor("#7BB5EF"));
        chart.getConfiguration().addColorAxis(colorAxis);

        PlotOptionsTreemap plotOptions = new PlotOptionsTreemap();
        plotOptions.setLayoutAlgorithm(TreeMapLayoutAlgorithm.SQUARIFIED);

        TreeSeries series = createSeries();

        series.setPlotOptions(plotOptions);

        chart.getConfiguration().addSeries(series);

        chart.getConfiguration().setTitle("Vaadin Charts Treemap");

        add(chart);
    }

    private TreeSeries createSeries() {
        List<TreeSeriesItem> items = new ArrayList<>();

        items.add(new TreeSeriesItem("A", 6));
        items.add(new TreeSeriesItem("B", 6));
        items.add(new TreeSeriesItem("C", 4));
        items.add(new TreeSeriesItem("D", 3));
        items.add(new TreeSeriesItem("E", 2));
        items.add(new TreeSeriesItem("F", 2));
        items.add(new TreeSeriesItem("G", 1));

        for (int i = 1; i <= items.size(); i++) {
            items.get(i - 1).setColorValue(i);
        }

        TreeSeries series = new TreeSeries();

        series.setData(items);

        return series;
    }
}
