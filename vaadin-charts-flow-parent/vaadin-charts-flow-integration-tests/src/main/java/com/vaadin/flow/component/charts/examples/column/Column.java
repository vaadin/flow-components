package com.vaadin.flow.component.charts.examples.column;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Crosshair;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

public class Column extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Monthly Average Rainfall");
        configuration.setSubTitle("Source: WorldClimate.com");
        chart.getConfiguration().getChart().setType(ChartType.COLUMN);

        configuration.addSeries(new ListSeries("Tokyo", 49.9, 71.5, 106.4,
                129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4));
        configuration.addSeries(new ListSeries("New York", 83.6, 78.8, 98.5,
                93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3));
        configuration.addSeries(new ListSeries("London", 48.9, 38.8, 39.3, 41.4,
                47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2));
        configuration.addSeries(new ListSeries("Berlin", 42.4, 33.2, 34.5, 39.7,
                52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1));

        XAxis x = new XAxis();
        x.setCrosshair(new Crosshair());
        x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec");
        configuration.addxAxis(x);

        YAxis y = new YAxis();
        y.setMin(0);
        y.setTitle("Rainfall (mm)");
        configuration.addyAxis(y);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        configuration.setTooltip(tooltip);

        add(chart);
    }
}
