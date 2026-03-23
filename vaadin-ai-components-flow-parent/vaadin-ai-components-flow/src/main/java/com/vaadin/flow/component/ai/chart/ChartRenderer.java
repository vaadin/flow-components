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
import java.util.Set;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.GanttSeries;
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

            // Apply axis defaults from series data before LLM config,
            // so that LLM-provided axis settings take priority.
            applyAxisDefaults(config, allSeries);

            if (configJson != null) {
                configurationApplier.applyConfiguration(chart, configJson);
            }
            chart.drawChart();
        }));
    }

    /**
     * Chart types where item names are labels (e.g. pie slices, sankey nodes),
     * not X-axis categories.
     */
    private static final Set<ChartType> NO_CATEGORY_AXIS_TYPES = Set.of(
            ChartType.PIE, ChartType.SANKEY, ChartType.ORGANIZATION,
            ChartType.TREEMAP, ChartType.TIMELINE, ChartType.FLAGS,
            ChartType.FUNNEL, ChartType.PYRAMID);

    /**
     * Applies axis defaults inferred from the series data. Sets
     * {@code xAxis.categories} when items have names, and
     * {@code xAxis.type = DATETIME} when X values are epoch timestamps or a
     * {@link GanttSeries} is present.
     */
    private static void applyAxisDefaults(Configuration config,
            List<Series> allSeries) {
        if (allSeries.isEmpty()) {
            return;
        }

        var xAxis = config.getxAxis();
        var chartType = config.getChart().getType();

        // Gantt series always needs a datetime X-axis.
        if (allSeries.stream().anyMatch(GanttSeries.class::isInstance)) {
            if (xAxis.getType() == null) {
                xAxis.setType(AxisType.DATETIME);
            }
            return;
        }

        // Inspect the first DataSeries for category names and datetime values.
        if (!(allSeries.getFirst() instanceof DataSeries firstSeries)) {
            return;
        }
        var items = firstSeries.getData();
        if (items.isEmpty()) {
            return;
        }

        // Extract category names if items have names and the chart type
        // uses a category axis (not pie, sankey, etc.).
        if (xAxis.getCategories() == null
                && !NO_CATEGORY_AXIS_TYPES.contains(chartType)) {
            var categories = extractCategories(items);
            if (categories != null) {
                xAxis.setCategories(categories.toArray(new String[0]));
            }
        }

        // Detect datetime X values (epoch ms > year 2000).
        if (xAxis.getType() == null) {
            if (hasDatetimeXValues(items)) {
                xAxis.setType(AxisType.DATETIME);
            }
        }
    }

    /**
     * Returns category names extracted from items, or {@code null} if items
     * don't have names.
     */
    private static List<String> extractCategories(List<DataSeriesItem> items) {
        var categories = new ArrayList<String>(items.size());
        for (var item : items) {
            var name = item.getName();
            if (name == null) {
                return null;
            }
            categories.add(name);
        }
        return categories;
    }

    /**
     * Checks whether the first non-null X value looks like an epoch millisecond
     * timestamp (after year 2000).
     */
    private static boolean hasDatetimeXValues(List<DataSeriesItem> items) {
        // Epoch ms for 2000-01-01
        final long EPOCH_MS_YEAR_2000 = 946_684_800_000L;
        for (var item : items) {
            var x = item.getX();
            if (x != null) {
                return x.longValue() > EPOCH_MS_YEAR_2000;
            }
        }
        return false;
    }
}
