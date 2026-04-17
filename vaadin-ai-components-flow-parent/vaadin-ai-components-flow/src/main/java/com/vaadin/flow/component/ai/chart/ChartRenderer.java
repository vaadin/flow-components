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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.FlagItem;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsFlags;
import com.vaadin.flow.component.charts.model.Series;

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
    public static void renderChart(Chart chart,
            DatabaseProvider databaseProvider, DataConverter dataConverter,
            List<String> queries, String configJson) {
        List<Series> allSeries = new ArrayList<>();
        for (String query : queries) {
            var results = databaseProvider.executeQuery(query);
            allSeries.addAll(dataConverter.convertToSeries(results));
        }

        Configuration config = chart.getConfiguration();
        if (configJson != null) {
            var parsed = ChartConfigurationParser.parse(configJson);
            if (chartTypeChanged(config, parsed)) {
                config = parsed;
                chart.setConfiguration(config);
            } else {
                ChartConfigurationParser.merge(configJson, config);
            }
        }

        // Extract per-series config (plotOptions, yAxis) from the
        // configuration's series before replacing them with SQL data.
        // The config series act as templates set by the parser.
        var seriesConfig = extractSeriesConfig(config);
        config.setSeries(allSeries.toArray(new Series[0]));

        // Name single unnamed series using the chart title so the
        // legend shows a meaningful label instead of "Series 1".
        // Must run before applySeriesConfig which matches by name.
        nameUnnamedSeries(config, allSeries);

        applySeriesConfig(allSeries, seriesConfig);
        applyFlagsPlotOptions(allSeries);

        // Apply axis defaults from series data after LLM config,
        // so that data-driven axis type detection (e.g. datetime)
        // overrides any incorrect LLM-provided axis type.
        applyAxisDefaults(config, allSeries);

        // Full reset required. Without it, axis categories are
        // lost when the chart is rendered via async Push (see
        // DashboardChartControllerIT).
        chart.drawChart(true);
    }

    /**
     * Returns {@code true} if the new configuration specifies a different chart
     * type than the current one, indicating a full configuration reset is
     * needed.
     */
    private static boolean chartTypeChanged(Configuration current,
            Configuration incoming) {
        var currentType = current.getChart().getType();
        var incomingType = incoming.getChart().getType();
        return incomingType != null && !incomingType.equals(currentType);
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
        if (chartType == null || !NO_CATEGORY_AXIS_TYPES.contains(chartType)) {
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
     * Ensures any DataSeries containing FlagItem objects has PlotOptionsFlags
     * set, so flags render correctly even when the chart-level type is
     * different (e.g. column).
     */
    private static void applyFlagsPlotOptions(List<Series> allSeries) {
        for (var series : allSeries) {
            if (!(series instanceof DataSeries ds)) {
                continue;
            }
            if (ds.getPlotOptions() instanceof PlotOptionsFlags) {
                continue;
            }
            boolean hasFlags = ds.getData().stream()
                    .anyMatch(FlagItem.class::isInstance);
            if (hasFlags) {
                ds.setPlotOptions(new PlotOptionsFlags());
            }
        }
    }

    /**
     * Extracts per-series configuration (plot options, y-axis) from the
     * configuration's current series, keyed by series name. These are
     * "template" series set by the parser that carry config but no data.
     */
    private static Map<String, AbstractSeries> extractSeriesConfig(
            Configuration config) {
        var result = new LinkedHashMap<String, AbstractSeries>();
        for (var series : config.getSeries()) {
            if (series instanceof AbstractSeries as && as.getName() != null) {
                result.put(as.getName(), as);
            }
        }
        return result;
    }

    /**
     * Applies previously extracted series configuration to the data series.
     * Matches by name first, then falls back to positional matching for
     * unmatched series — copying the template's name, plot options, and y-axis
     * binding.
     */
    private static void applySeriesConfig(List<Series> allSeries,
            Map<String, AbstractSeries> seriesConfig) {
        // Pre-scan: which template names have a matching data series?
        var nameMatched = new HashSet<String>();
        for (var s : allSeries) {
            if (s instanceof AbstractSeries as
                    && seriesConfig.containsKey(as.getName())) {
                nameMatched.add(as.getName());
            }
        }

        // Templates without a name match feed the positional fallback.
        var positional = seriesConfig.values().stream()
                .filter(t -> !nameMatched.contains(t.getName())).iterator();

        for (var s : allSeries) {
            if (!(s instanceof AbstractSeries as)) {
                continue;
            }
            var tpl = seriesConfig.get(as.getName());
            if (tpl == null && positional.hasNext()) {
                tpl = positional.next();
                as.setName(tpl.getName());
            }
            if (tpl != null) {
                applyTemplate(as, tpl);
            }
        }
    }

    private static void applyTemplate(AbstractSeries target,
            AbstractSeries template) {
        if (template.getPlotOptions() != null) {
            target.setPlotOptions(template.getPlotOptions());
        }
        if (template.getyAxis() != null) {
            target.setyAxis(template.getyAxis());
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
