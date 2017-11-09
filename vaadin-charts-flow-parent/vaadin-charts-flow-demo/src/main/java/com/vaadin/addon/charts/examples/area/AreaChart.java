package com.vaadin.addon.charts.examples.area;

import com.vaadin.addon.charts.SkipFromDemo;
import com.vaadin.addon.charts.VaadinChart;
import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.ui.html.NativeButton;

@SkipFromDemo
public class AreaChart extends AbstractChartExample {

    @Override
    public void initDemo() {
        final VaadinChart chart = new VaadinChart();

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("First Chart for Flow");
        chart.getConfiguration().getChart().setType(ChartType.AREA);
        add(chart);

        configuration.addSeries(new ListSeries("Tokyo", 20, 12, 34, 23, 65, 8,
                4, 7, 76, 19, 20, 8));
        configuration.addSeries(new ListSeries("Miami", 34, 29, 23, 65, 8, 4, 7,
                7, 59, 8, 9, 19));

        XAxis x = new XAxis();
        x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec");
        configuration.addxAxis(x);

        YAxis y = new YAxis();
        y.setMin(0);
        y.setTitle("Rainfall (mm)");
        configuration.addyAxis(y);

        NativeButton changeTitleButton = new NativeButton();
        changeTitleButton.setId("change_title");
        changeTitleButton.setText("Change title");
        changeTitleButton.addClickListener(e -> {
            configuration.setTitle("First Chart for Flow - title changed");
            chart.drawChart();
        });

        add(changeTitleButton);
    }

}
