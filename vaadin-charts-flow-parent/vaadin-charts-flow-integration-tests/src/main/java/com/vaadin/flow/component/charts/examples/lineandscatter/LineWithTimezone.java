/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.lineandscatter;

import java.time.Instant;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.html.NativeButton;

public class LineWithTimezone extends AbstractChartExample {

    @Override
    public void initDemo() {
        final Chart chart = new Chart();

        Configuration configuration = chart.getConfiguration();

        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setPointStart(Instant.ofEpochSecond(1735689600));
        plotOptionsSeries.setPointInterval(36e5);
        configuration.setPlotOptions(plotOptionsSeries);

        configuration.addSeries(new ListSeries("Series", 43934, 52503, 57177,
                69658, 97031, 119931, 137133, 154175));

        var xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);

        add(chart);
        addTimezoneButton("America/New_York", chart);
        addTimezoneButton("Europe/Helsinki", chart);
        addTimezoneButton("UTC", chart);
    }

    private void addTimezoneButton(String timezone, Chart chart) {
        NativeButton button = new NativeButton(timezone, (e) -> {
            chart.getConfiguration().getTime().setTimezone(timezone);
            chart.drawChart();
        });
        button.setId(timezone.replace("/", "_"));
        add(button);
    }
}
