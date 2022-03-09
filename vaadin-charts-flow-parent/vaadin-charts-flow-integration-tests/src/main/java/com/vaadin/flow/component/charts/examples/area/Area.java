package com.vaadin.flow.component.charts.examples.area;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsArea;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.model.TickmarkPlacement;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

public class Area extends AbstractChartExample {
    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.AREA);

        final Configuration configuration = chart.getConfiguration();

        configuration.setTitle(
                "Historic and Estimated Worldwide Population Growth by Region");
        configuration.setSubTitle("Source: Wikipedia.org");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setCategories("1750", "1800", "1850", "1900", "1950", "1999",
                "2050");
        xAxis.setTickmarkPlacement(TickmarkPlacement.ON);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle("Billions");
        yAxis.getLabels()
                .setFormatter("function () { return this.value / 1000;}");

        configuration.getTooltip().setValueSuffix(" millions");

        PlotOptionsArea plotOptionsArea = new PlotOptionsArea();
        plotOptionsArea.setStacking(Stacking.NORMAL);
        configuration.setPlotOptions(plotOptionsArea);

        configuration.addSeries(
                new ListSeries("Asia", 502, 635, 809, 947, 1402, 3634, 5268));
        configuration.addSeries(
                new ListSeries("Africa", 106, 107, 111, 133, 221, 767, 1766));
        configuration.addSeries(
                new ListSeries("Europe", 163, 203, 276, 408, 547, 729, 628));
        configuration.addSeries(
                new ListSeries("America", 18, 31, 54, 156, 339, 818, 1201));
        configuration
                .addSeries(new ListSeries("Oceania", 2, 2, 2, 6, 13, 30, 46));

        add(chart);
    }
}
