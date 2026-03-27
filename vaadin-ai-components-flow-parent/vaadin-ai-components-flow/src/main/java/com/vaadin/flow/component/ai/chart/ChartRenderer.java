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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.Series;
import com.vaadin.flow.component.charts.util.ChartSerialization;

/**
 * Stateless utility for rendering chart data from SQL queries. Encapsulates the
 * query-execution-to-drawChart pipeline so that different controllers
 * (standalone chart, dashboard) share the same rendering logic.
 *
 * @author Vaadin Ltd
 */
public final class ChartRenderer implements Serializable {

    private ChartRenderer() {
    }

    /**
     * Applies pending state from the chart's {@link ChartEntry} if present.
     * Uses {@code runWhenAttached} and {@code UI.access()} to ensure the chart
     * is attached and the update happens safely on the UI thread. After
     * applying, the pending state is cleared.
     *
     * @param chart
     *            the chart to update, not {@code null}
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     * @param dataConverter
     *            the data converter for transforming query results, not
     *            {@code null}
     */
    public static void applyPendingState(Chart chart,
            DatabaseProvider databaseProvider, DataConverter dataConverter) {
        ChartEntry entry = ChartEntry.get(chart);
        if (entry == null || !entry.hasPendingState()) {
            return;
        }
        String configJson = entry.getPendingConfigurationJson();
        List<String> effectiveQueries = entry.getQueries();

        if (!effectiveQueries.isEmpty()) {
            try {
                String effectiveConfig = configJson != null ? configJson
                        : ChartSerialization.toJSON(chart.getConfiguration());
                renderChart(chart, databaseProvider, dataConverter,
                        effectiveQueries, effectiveConfig);
            } finally {
                entry.clearPendingState();
            }
        } else {
            // Config-only: no queries to render yet. Clear only the
            // data flag but keep pendingConfigurationJson so it's used
            // when data arrives in a later request.
            entry.setPendingDataUpdate(false);
        }
    }

    /**
     * Renders a chart by executing queries, converting results to series, and
     * applying configuration.
     *
     * @param chart
     *            the chart to render, not {@code null}
     * @param databaseProvider
     *            the database provider for query execution, not {@code null}
     * @param dataConverter
     *            the data converter for transforming query results, not
     *            {@code null}
     * @param queries
     *            the SQL queries to execute (one per series), not {@code null}
     * @param configJson
     *            the chart configuration JSON, or {@code null} to keep current
     */
    private static void renderChart(Chart chart,
            DatabaseProvider databaseProvider, DataConverter dataConverter,
            List<String> queries, String configJson) {
        chart.getElement().getNode().runWhenAttached(ui -> ui.access(() -> {
            Configuration config = chart.getConfiguration();
            List<Series> allSeries = new ArrayList<>();
            for (String query : queries) {
                var results = databaseProvider.executeQuery(query);
                allSeries.addAll(dataConverter.convertToSeries(results));
            }
            config.setSeries(allSeries.toArray(new Series[0]));

            if (configJson != null) {
                ChartConfigurationApplier.applyConfiguration(chart, configJson);
            }

            // Apply axis defaults from series data after LLM config,
            // so that data-driven axis type detection (e.g. datetime)
            // overrides any incorrect LLM-provided axis type.
            applyAxisDefaults(config, allSeries);

            // Name single unnamed series using the chart title so the
            // legend shows a meaningful label instead of "Series 1".
            nameUnnamedSeries(config, allSeries);

            // Full reset required. Without it, axis categories are
            // lost when the chart is rendered via async Push (see
            // DashboardChartControllerIT).
            chart.drawChart(true);
        }));
    }

    /**
     * Chart types where item names are labels (e.g. pie slices, sankey nodes),
     * not X-axis categories.
     */
    private static final Set<ChartType> NO_CATEGORY_AXIS_TYPES = Set.of(
            ChartType.PIE, ChartType.SANKEY, ChartType.ORGANIZATION,
            ChartType.TREEMAP, ChartType.TIMELINE, ChartType.FLAGS,
            ChartType.FUNNEL, ChartType.PYRAMID, ChartType.CANDLESTICK,
            ChartType.OHLC, ChartType.XRANGE);

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

        // Collect all DataSeries items for inspection.
        var allItems = new ArrayList<DataSeriesItem>();
        for (var series : allSeries) {
            if (series instanceof DataSeries ds) {
                allItems.addAll(ds.getData());
            }
        }
        if (allItems.isEmpty()) {
            return;
        }

        // Extract category names if items have names and the chart type
        // uses a category axis (not pie, sankey, etc.).
        // Collects from all series to handle multi-series with different
        // categories (e.g. stacked by region where each region has
        // different months).
        if (!NO_CATEGORY_AXIS_TYPES.contains(chartType)) {
            var categories = extractCategories(allItems);
            if (!categories.isEmpty()) {
                xAxis.setCategories(categories.toArray(new String[0]));
            }
        }

        // Detect datetime X values (epoch ms > year 2000).
        // Always override: the data is authoritative for axis type when
        // values are clearly timestamps (e.g. OHLC dates).
        // Check per-series: if any series has all-datetime X values, set
        // the axis to datetime. This handles multi-query scenarios where
        // one series (e.g. OHLC) has datetime X and another (e.g. volume)
        // has row-index X values.
        if (hasDatetimeXValues(allSeries)) {
            xAxis.setType(AxisType.DATETIME);
        }
    }

    /**
     * Returns unique category names extracted from items (preserving insertion
     * order), or an empty list if any item has no name.
     */
    private static List<String> extractCategories(List<DataSeriesItem> items) {
        var seen = new LinkedHashSet<String>();
        for (var item : items) {
            var name = item.getName();
            if (name == null) {
                return List.of();
            }
            seen.add(name);
        }
        return new ArrayList<>(seen);
    }

    /**
     * When there is exactly one series and it has no name, uses the chart title
     * as the series name so the legend shows a meaningful label instead of
     * "Series 1".
     */
    private static void nameUnnamedSeries(Configuration config,
            List<Series> allSeries) {
        if (allSeries.size() != 1) {
            return;
        }
        var series = allSeries.getFirst();
        if (series instanceof AbstractSeries abstractSeries
                && abstractSeries.getName() == null) {
            var title = config.getTitle();
            if (title != null && title.getText() != null) {
                abstractSeries.setName(title.getText());
            }
        }
    }

    /**
     * Checks whether any series has all non-null X values that look like epoch
     * millisecond timestamps (after year 2000). Checks per-series to handle
     * multi-query scenarios where one series has datetime X and another has
     * row-index X values.
     */
    private static boolean hasDatetimeXValues(List<Series> allSeries) {
        final long EPOCH_MS_YEAR_2000 = 946_684_800_000L;
        for (var series : allSeries) {
            if (!(series instanceof DataSeries ds)) {
                continue;
            }
            boolean found = false;
            boolean allDatetime = true;
            for (var item : ds.getData()) {
                var x = item.getX();
                if (x != null) {
                    if (x.longValue() <= EPOCH_MS_YEAR_2000) {
                        allDatetime = false;
                        break;
                    }
                    found = true;
                }
            }
            if (found && allDatetime) {
                return true;
            }
        }
        return false;
    }
}
