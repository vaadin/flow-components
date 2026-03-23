/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.util.ChartSerialization;

/**
 * Applies pending chart state and renders chart data. Encapsulates the
 * query-execution-to-drawChart pipeline so that different controllers
 * (standalone chart, dashboard) share the same rendering logic.
 *
 * @author Vaadin Ltd
 */
public class ChartRenderer {

    private final DatabaseProvider databaseProvider;
    private DataConverter dataConverter;
    private final ChartConfigurationApplier configurationApplier;

    /**
     * Creates a new chart renderer.
     *
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     */
    public ChartRenderer(DatabaseProvider databaseProvider) {
        this.databaseProvider = Objects.requireNonNull(databaseProvider,
                "databaseProvider must not be null");
        this.dataConverter = new DefaultDataConverter();
        this.configurationApplier = new ChartConfigurationApplier();
    }

    /**
     * Sets a custom data converter for transforming query results into chart
     * data.
     *
     * @param dataConverter
     *            the data converter to use, not {@code null}
     */
    public void setDataConverter(DataConverter dataConverter) {
        this.dataConverter = Objects.requireNonNull(dataConverter,
                "Data converter cannot be null");
    }

    /**
     * Returns the data converter.
     *
     * @return the data converter
     */
    public DataConverter getDataConverter() {
        return dataConverter;
    }

    /**
     * Returns the configuration applier.
     *
     * @return the configuration applier
     */
    public ChartConfigurationApplier getConfigurationApplier() {
        return configurationApplier;
    }

    /**
     * Applies pending state from the chart's {@link ChartEntry} if present.
     * After applying, the pending state is cleared.
     *
     * @param chart
     *            the chart to update, not {@code null}
     */
    public void applyPendingState(Chart chart) {
        ChartEntry entry = ChartEntry.get(chart);
        if (entry == null || !entry.hasPendingState()) {
            return;
        }
        try {
            String configJson = entry.getPendingConfigurationJson();
            List<String> effectiveQueries = entry.getQueries();

            if (!effectiveQueries.isEmpty()) {
                String effectiveConfig = configJson != null ? configJson
                        : ChartSerialization.toJSON(chart.getConfiguration());
                renderChart(chart, effectiveQueries, effectiveConfig);
            } else if (configJson != null) {
                chart.getElement().getNode().runWhenAttached(
                        ui -> ui.access(() -> configurationApplier
                                .applyConfiguration(chart, configJson)));
            }
        } finally {
            entry.clearPendingState();
        }
    }

    /**
     * Renders a chart by executing queries, converting results to series, and
     * applying configuration.
     *
     * @param chart
     *            the chart to render, not {@code null}
     * @param queries
     *            the SQL queries to execute (one per series), not {@code null}
     * @param configJson
     *            the chart configuration JSON, or {@code null} to keep current
     */
    public void renderChart(Chart chart, List<String> queries,
            String configJson) {
        chart.getElement().getNode().runWhenAttached(ui -> ui.access(() -> {
            Configuration config = chart.getConfiguration();
            List<Series> allSeries = new ArrayList<>();
            for (String query : queries) {
                var results = databaseProvider.executeQuery(query);
                allSeries.addAll(dataConverter.convertToSeries(results));
            }
            config.setSeries(allSeries.toArray(new Series[0]));
            if (configJson != null) {
                configurationApplier.applyConfiguration(chart, configJson);
            }
            chart.drawChart();
        }));
    }
}
