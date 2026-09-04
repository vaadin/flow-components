/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;

/**
 * Benchmarks {@link ChartAIController}: chart type and the aggregated series
 * data are compared against the expected values.
 */
@EnabledIfEnvironmentVariable(named = AIBenchmark.MODEL_VARIABLE, matches = ".+")
class ChartAIControllerBenchmark {

    private static final Map<String, Double> SALES_BY_REGION = Map.of("Europe",
            3200.0, "North America", 2100.0, "Asia", 900.0);

    @RegisterExtension
    AIBenchmark bench = new AIBenchmark();

    @Test
    void salesByRegionAsPieChart() {
        bench.score(() -> {
            try (var db = BenchmarkDatabase.sales()) {
                var chart = new Chart();
                var controller = new ChartAIController(chart, db);
                try (var conversation = bench.conversation(chart, controller)) {
                    conversation
                            .say("Show total sales by region as a pie chart");
                }
                Assertions.assertEquals(ChartType.PIE, chartType(chart),
                        () -> "chart type, with " + queries(controller));
                Assertions.assertEquals(SALES_BY_REGION, seriesValues(chart));
            }
        });
    }

    @Test
    void switchesChartTypeAndKeepsData() {
        bench.score(() -> {
            try (var db = BenchmarkDatabase.sales()) {
                var chart = new Chart();
                var controller = new ChartAIController(chart, db);
                try (var conversation = bench.conversation(chart, controller)) {
                    conversation
                            .say("Show total sales by region as a pie chart");
                    conversation.say("Make it a column chart instead");
                }
                Assertions.assertEquals(ChartType.COLUMN, chartType(chart),
                        () -> "chart type, with " + queries(controller));
                Assertions.assertEquals(SALES_BY_REGION, seriesValues(chart));
            }
        });
    }

    /** The state is {@code null} until the first successful render. */
    private static String queries(ChartAIController controller) {
        var state = controller.getState();
        return state == null ? "no rendered state"
                : "queries " + state.queries();
    }

    /**
     * The type is {@code null} when the model only updated the data source and
     * never called {@code update_chart_configuration}.
     */
    private static ChartType chartType(Chart chart) {
        return chart.getConfiguration().getChart().getType();
    }

    private static Map<String, Double> seriesValues(Chart chart) {
        var series = chart.getConfiguration().getSeries();
        Assertions.assertEquals(1, series.size(),
                "expected exactly one series, got " + series.size());
        var values = new LinkedHashMap<String, Double>();
        for (var item : ((DataSeries) series.get(0)).getData()) {
            values.put(item.getName(), item.getY().doubleValue());
        }
        return values;
    }
}
