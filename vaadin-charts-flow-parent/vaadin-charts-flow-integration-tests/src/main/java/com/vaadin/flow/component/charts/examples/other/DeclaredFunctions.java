/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.other;

import java.io.Serializable;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;
import com.vaadin.flow.router.Route;

@Route("vaadin-charts/other/declared-functions")
public class DeclaredFunctions extends AbstractChartExample {

    @JsDefinition
    public interface Formatters extends Serializable {
        @JsExpression("return this.y / $0 + $1;")
        ChartFunction scaled(int divisor, String suffix);
    }

    private static final Formatters FORMATTERS = JsSnippets
            .of(Formatters.class);

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.COLUMN);
        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Declared functions");
        configuration.getTooltip()
                .setFormatterFunction(FORMATTERS.scaled(1, " units"));
        configuration
                .addSeries(withLabels(new ListSeries("Sales", 2000, 4000, 8000),
                        FORMATTERS.scaled(1000, "k")));

        NativeButton addSeries = new NativeButton("Add series",
                event -> configuration.addSeries(
                        withLabels(new ListSeries("Percent", 10, 20, 30),
                                FORMATTERS.scaled(1, "%"))));
        addSeries.setId("add-series");

        add(chart, addSeries);
    }

    private static ListSeries withLabels(ListSeries series,
            ChartFunction formatter) {
        DataLabels dataLabels = new DataLabels(true);
        dataLabels.setFormatterFunction(formatter);
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setDataLabels(dataLabels);
        series.setPlotOptions(plotOptions);
        return series;
    }
}
