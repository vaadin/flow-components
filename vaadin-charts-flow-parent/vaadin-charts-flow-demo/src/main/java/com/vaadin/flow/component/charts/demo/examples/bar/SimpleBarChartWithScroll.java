package com.vaadin.flow.component.charts.demo.examples.bar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.demo.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;

public class SimpleBarChartWithScroll extends AbstractChartExample {
    private boolean enabled = false;

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();

        ListSeries series = new ListSeries(10, 10, 10);
        conf.addSeries(series);

        Button toggle = new Button("Toggle scrollbar", event -> {
            enabled = !enabled;
            conf.getScrollbar().setEnabled(enabled);
        });

        add(chart, toggle);
    }
}
