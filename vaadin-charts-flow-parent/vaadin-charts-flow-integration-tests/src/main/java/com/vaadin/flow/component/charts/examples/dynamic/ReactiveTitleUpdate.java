/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.dynamic;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Demonstrates reactive title updates: the button mutates
 * {@code configuration.getTitle().setText(...)} and deliberately does NOT call
 * {@link Chart#drawChart()}. With the {@code reactiveCharts} feature flag on,
 * the change still propagates to the client.
 */
@Route("vaadin-charts/dynamic/reactive-title")
public class ReactiveTitleUpdate extends AbstractChartExample {

    public static final String INITIAL_TITLE = "Reactive initial title";
    public static final String UPDATED_TITLE = "Reactive updated title";

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.LINE);
        chart.setId("reactiveChart");

        Configuration configuration = chart.getConfiguration();
        configuration.setTitle(INITIAL_TITLE);
        configuration.addSeries(new ListSeries("Series", 1, 2, 3, 4, 5));

        NativeButton updateTitleButton = new NativeButton("Update title",
                event -> configuration.getTitle().setText(UPDATED_TITLE));
        updateTitleButton.setId("updateTitleButton");

        add(chart, updateTitleButton);
    }
}
