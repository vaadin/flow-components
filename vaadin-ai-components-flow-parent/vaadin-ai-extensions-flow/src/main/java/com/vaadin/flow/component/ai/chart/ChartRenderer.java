/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
 * @since 25.3
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
        renderChart(chart, databaseProvider, dataConverter, queries,
                configJson == null ? List.of() : List.of(configJson));
    }

    /**
     * Renders a chart like
     * {@link #renderChart(Chart, DatabaseProvider, DataConverter, List, String)},
     * applying each of the configuration JSON strings in order.
     */
    static void renderChart(Chart chart, DatabaseProvider databaseProvider,
            DataConverter dataConverter, List<String> queries,
            List<String> configJsons) {
        List<Series> allSeries = new ArrayList<>();
        for (String query : queries) {
            var results = databaseProvider.executeQuery(query);
            allSeries.addAll(dataConverter.convertToSeries(results));
        }

        // A JSON that changes the chart type replaces the configuration, so
        // the JSONs before the last such change have no effect and are
        // skipped. This also leaves the previous configuration untouched for
        // the rollback below.
        Configuration previous = chart.getConfiguration();
        int lastTypeChange = lastTypeChange(previous, configJsons);
        Configuration config = lastTypeChange < 0 ? previous
                : ChartConfigurationParser
                        .parse(configJsons.get(lastTypeChange));
        for (var configJson : configJsons.subList(lastTypeChange + 1,
                configJsons.size())) {
            ChartConfigurationParser.merge(configJson, config);
        }
        if (config != previous) {
            chart.setConfiguration(config);
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
        try {
            chart.drawChart(true);
        } catch (RuntimeException e) {
            // The chart rejects some configurations, such as an unsupported
            // type in timeline mode. Keep the one it last accepted.
            if (config != previous) {
                chart.setConfiguration(previous);
            }
            throw e;
        }
    }

    /**
     * Applies configuration JSON from the LLM and returns the result: a new
     * chart type replaces the configuration, anything else is merged into it.
     */
    static Configuration applyConfiguration(Configuration current,
            String configJson) {
        var parsed = ChartConfigurationParser.parse(configJson);
        if (chartTypeChanged(current, parsed)) {
            return parsed;
        }
        ChartConfigurationParser.merge(configJson, current);
        return current;
    }

    /**
     * Returns the index of the last configuration JSON that changes the chart
     * type when the JSONs are applied in order to the given configuration, or
     * {@code -1} if none does.
     */
    private static int lastTypeChange(Configuration current,
            List<String> configJsons) {
        var type = current.getChart().getType();
        var last = -1;
        for (var i = 0; i < configJsons.size(); i++) {
            var newType = ChartConfigurationParser.parse(configJsons.get(i))
                    .getChart().getType();
            if (newType != null && !newType.equals(type)) {
                last = i;
            }
            if (newType != null) {
                type = newType;
            }
        }
        return last;
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
        var dataSeries = allSeries.stream().filter(DataSeries.class::isInstance)
                .map(DataSeries.class::cast).toList();
        var allItems = dataSeries.stream()
                .flatMap(series -> series.getData().stream()).toList();
        if (allItems.isEmpty()) {
            return;
        }

        // Extract category names if items have names and the chart type
        // uses a category axis (not pie, sankey, etc.).
        // Collects from all series to handle multi-series with different
        // categories (e.g. stacked by region where each region has
        // different months).
        if ((chartType == null || !NO_CATEGORY_AXIS_TYPES.contains(chartType))
                && allItems.stream().allMatch(item -> item.getName() != null)) {
            var categories = placeItemsInCategories(dataSeries);
            xAxis.setCategories(categories.toArray(new String[0]));
        }

        // Detect datetime X values (epoch ms from year 2000 on).
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
     * A category on the X-axis: the n-th item with the given name in a series.
     * Repeated names within a series get their own categories so that their
     * items are not drawn on top of each other.
     */
    private record CategoryKey(String name,
            int occurrence) implements Serializable {
    }

    /**
     * Returns the X-axis categories for the named items of the given series.
     * The categories keep the item order of every series where the series
     * agree, so that, for example, a waterfall sum stays after the items it
     * adds up.
     * <p>
     * Highcharts places named items by their position in their own series when
     * the categories are given explicitly, so a series that lacks some of the
     * categories would draw its items under the wrong ones. In each series
     * whose items have no X values of their own, the X value of each item is
     * set to the index of its category, and the items are sorted by X so that
     * lines are drawn from left to right.
     */
    private static List<String> placeItemsInCategories(
            List<DataSeries> allSeries) {
        var keysBySeries = allSeries.stream()
                .map(series -> toCategoryKeys(series.getData())).toList();
        var categories = mergeCategoryKeys(keysBySeries);
        var indexes = new HashMap<CategoryKey, Integer>();
        for (var i = 0; i < categories.size(); i++) {
            indexes.put(categories.get(i), i);
        }
        for (var i = 0; i < allSeries.size(); i++) {
            var keys = keysBySeries.get(i);
            var items = new ArrayList<>(allSeries.get(i).getData());
            if (items.stream().anyMatch(item -> item.getX() != null)) {
                continue;
            }
            for (var j = 0; j < items.size(); j++) {
                items.get(j).setX(indexes.get(keys.get(j)));
            }
            items.sort(Comparator.comparingInt(item -> item.getX().intValue()));
            allSeries.get(i).setData(items);
        }
        return categories.stream().map(CategoryKey::name).toList();
    }

    private static List<CategoryKey> toCategoryKeys(
            List<DataSeriesItem> items) {
        var counts = new HashMap<String, Integer>();
        var keys = new ArrayList<CategoryKey>();
        for (var item : items) {
            var occurrence = counts.merge(item.getName(), 1, Integer::sum) - 1;
            keys.add(new CategoryKey(item.getName(), occurrence));
        }
        return keys;
    }

    /**
     * Merges the category keys of all series into one list that keeps the order
     * of each series. Categories that no series orders against each other keep
     * the order in which they are first seen. Where the series disagree on the
     * order, the category seen first comes first.
     */
    private static List<CategoryKey> mergeCategoryKeys(
            List<List<CategoryKey>> keysBySeries) {
        var indexes = new LinkedHashMap<CategoryKey, Integer>();
        keysBySeries.forEach(keys -> keys
                .forEach(key -> indexes.putIfAbsent(key, indexes.size())));
        var successors = new ArrayList<Set<Integer>>();
        indexes.forEach((key, index) -> successors.add(new HashSet<>()));
        var predecessorCounts = new int[indexes.size()];
        for (var keys : keysBySeries) {
            for (var i = 1; i < keys.size(); i++) {
                var next = indexes.get(keys.get(i));
                if (successors.get(indexes.get(keys.get(i - 1))).add(next)) {
                    predecessorCounts[next]++;
                }
            }
        }

        var remaining = new TreeSet<>(indexes.values());
        var ready = new TreeSet<Integer>();
        remaining.stream().filter(index -> predecessorCounts[index] == 0)
                .forEach(ready::add);
        var keys = new ArrayList<>(indexes.keySet());
        var merged = new ArrayList<CategoryKey>();
        while (!remaining.isEmpty()) {
            var index = ready.isEmpty() ? remaining.first() : ready.pollFirst();
            remaining.remove(index);
            merged.add(keys.get(index));
            for (var successor : successors.get(index)) {
                if (--predecessorCounts[successor] == 0
                        && remaining.contains(successor)) {
                    ready.add(successor);
                }
            }
        }
        return merged;
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
    static Map<String, AbstractSeries> extractSeriesConfig(
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
     * binding. When the templates of this turn cover every unmatched data
     * series, the series of the previous render take no position, so they
     * cannot shift the templates onto the wrong series.
     */
    private static void applySeriesConfig(List<Series> allSeries,
            Map<String, AbstractSeries> seriesConfig) {
        // Pre-scan: which template names have a matching data series?
        var nameMatched = new HashSet<String>();
        var unmatchedSeries = 0;
        for (var s : allSeries) {
            if (!(s instanceof AbstractSeries as)) {
                continue;
            }
            if (seriesConfig.containsKey(as.getName())) {
                nameMatched.add(as.getName());
            } else {
                unmatchedSeries++;
            }
        }

        // Templates without a name match feed the positional fallback.
        var unmatched = seriesConfig.values().stream()
                .filter(t -> !nameMatched.contains(t.getName())).toList();
        var fresh = unmatched.stream().filter(ChartRenderer::isTemplate)
                .toList();
        var positional = (fresh.size() >= unmatchedSeries ? fresh : unmatched)
                .iterator();

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

    /**
     * A series the parser created from this turn's configuration JSON, as
     * opposed to a data series of the previous render.
     */
    private static boolean isTemplate(AbstractSeries series) {
        return series instanceof DataSeries data && data.getData().isEmpty();
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
     * millisecond timestamps (year 2000 or later). Checks per-series to handle
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
                    if (x.longValue() < EPOCH_MS_YEAR_2000) {
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
