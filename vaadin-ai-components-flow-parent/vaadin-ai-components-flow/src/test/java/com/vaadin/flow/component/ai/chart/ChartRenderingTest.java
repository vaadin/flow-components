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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.flow.component.charts.model.PlotOptionsFlags;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests chart rendering behavior through the {@link ChartAIController} public
 * API. Covers data conversion, pending state application, axis defaults, series
 * naming, and configuration reset across chart type switches.
 */
class ChartRenderingTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;
    private TestDatabaseProvider databaseProvider;
    private ChartAIController controller;
    private List<LLMProvider.ToolSpec> tools;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
        databaseProvider = new TestDatabaseProvider();
        controller = new ChartAIController(chart, databaseProvider);
        tools = controller.getTools();
    }

    private LLMProvider.ToolSpec findTool(String name) {
        return tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    private void updateConfiguration(String configJson) {
        findTool("update_chart_configuration").execute(JacksonUtils
                .readTree("{\"configuration\":" + configJson + "}"));
    }

    private void updateData(String... queries) {
        var sb = new StringBuilder("{\"queries\":[");
        for (int i = 0; i < queries.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(queries[i]).append("\"");
        }
        sb.append("]}");
        findTool("update_chart_data_source")
                .execute(JacksonUtils.readTree(sb.toString()));
    }

    @Nested
    class ApplyPendingState {

        @Test
        void pendingDataAndConfigRendersChart() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            updateConfiguration(
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Sales\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Configuration config = chart.getConfiguration();
            Assertions.assertEquals(ChartType.COLUMN,
                    config.getChart().getType());
            Assertions.assertEquals("Sales", config.getTitle().getText());
            Assertions.assertFalse(config.getSeries().isEmpty());
        }

        @Test
        void configThenDataInSeparateRequestsRetainsConfig() {
            // First request: config only
            updateConfiguration(
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Revenue\"}}");
            controller.onResponseComplete();

            // Second request: data only
            databaseProvider.results = List
                    .of(row("category", "Q1", "value", 100));
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Configuration config = chart.getConfiguration();
            Assertions.assertEquals(ChartType.COLUMN,
                    config.getChart().getType());
            Assertions.assertEquals("Revenue", config.getTitle().getText());
            Assertions.assertFalse(config.getSeries().isEmpty());
        }

        @Test
        void pendingDataOnlyUsesExistingConfig() {
            // First render: establish config and data
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));
            updateConfiguration(
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Existing Title\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Second update: only data, no config change
            databaseProvider.results = List
                    .of(row("category", "B", "value", 20));
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals("Existing Title",
                    chart.getConfiguration().getTitle().getText());
            Assertions.assertFalse(
                    chart.getConfiguration().getSeries().isEmpty());
        }

        @Test
        void sameChartTypeMergesConfigIntoExisting() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            // First render: line chart with title
            updateConfiguration(
                    "{\"chart\":{\"type\":\"line\"},\"title\":{\"text\":\"Original\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals("Original",
                    chart.getConfiguration().getTitle().getText());

            // Second render: same type, only update title (merge path)
            updateConfiguration("{\"title\":{\"text\":\"Updated\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals("Updated",
                    chart.getConfiguration().getTitle().getText());
            Assertions.assertEquals(ChartType.LINE,
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void configOnlyAfterGaugeResetsPane() {
            // First: full render as gauge
            databaseProvider.results = List.of(row(ColumnNames.Y, 78));
            updateConfiguration("{\"chart\":{\"type\":\"gauge\"},"
                    + "\"pane\":{\"startAngle\":-150,\"endAngle\":150},"
                    + "\"yAxis\":{\"min\":0,\"max\":100}}");
            updateData("SELECT current_val");
            controller.onResponseComplete();

            // Verify pane was set
            String json1 = ChartSerialization.toJSON(chart.getConfiguration());
            Assertions.assertTrue(json1.contains("\"startAngle\""));

            // Second: config-only update to column (no data change)
            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"title\":{\"text\":\"Revenue\"}}");
            controller.onResponseComplete();

            // Pane should be cleared
            String json2 = ChartSerialization.toJSON(chart.getConfiguration());
            Assertions.assertFalse(json2.contains("\"startAngle\""),
                    "Pane from gauge should be cleared on config-only "
                            + "update: " + json2);
        }

        @Test
        void clearsPendingStateEvenOnError() {
            // Set up data successfully (eager validation passes)
            databaseProvider.results = List.of(row("x", 1));
            updateData("SELECT 1");

            // Make DB throw for the render phase
            databaseProvider.throwOnExecute = new RuntimeException("DB error");

            // Exception propagates so the orchestrator can surface it to
            // the user, but pending state must still be cleared.
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onResponseComplete());

            // Pending state should be cleared despite the error: a
            // subsequent call with no pending state is a no-op and
            // must not re-trigger the DB (which would still throw).
            databaseProvider.throwOnExecute = null;
            Assertions
                    .assertDoesNotThrow(() -> controller.onResponseComplete());
        }
    }

    @Nested
    class RenderChart {

        @Test
        void multipleQueriesCreatesMultipleSeries() {
            databaseProvider.results = List.of(row("x", 1, "y", 10));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x, y FROM t1", "SELECT x, y FROM t2");
            controller.onResponseComplete();

            Assertions.assertEquals(2,
                    chart.getConfiguration().getSeries().size());
        }
    }

    @Nested
    class AxisDefaults {

        @Test
        void itemsWithNamesSetsCategoriesOnXAxis() {
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertNotNull(categories);
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb" },
                    categories);
        }

        @Test
        void itemsWithoutNamesDoesNotSetCategories() {
            // Items with X/Y numeric data (no names) should not set categories
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 10),
                    row(ColumnNames.X, 2, ColumnNames.Y, 20));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x, y FROM t");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0);
        }

        @Test
        void multiSeriesCategoriesCollectsFromAllSeries() {
            // Multi-series with _series column where each series has
            // different category names — categories should be the union
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "North", "category", "Jan", "value",
                            100),
                    row(ColumnNames.SERIES, "North", "category", "Feb", "value",
                            200),
                    row(ColumnNames.SERIES, "South", "category", "Jan", "value",
                            150),
                    row(ColumnNames.SERIES, "South", "category", "Mar", "value",
                            180));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertNotNull(categories);
            // Should contain Jan, Feb, Mar (union of both series)
            Assertions.assertEquals(3, categories.length,
                    "Categories should be union of all series: "
                            + java.util.Arrays.toString(categories));
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb", "Mar" },
                    categories);
        }

        @Test
        void mixedNamesAndNullDoesNotSetCategories() {
            // When some items have names and some don't, categories should
            // not be set (extractCategories returns null)
            databaseProvider.results = List.of(row("x", 1, "y", 10));

            controller.setDataConverter(data -> {
                DataSeries series = new DataSeries();
                series.add(new DataSeriesItem("Jan", 100));
                series.add(new DataSeriesItem(1, 200)); // no name
                return List.of(series);
            });

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT 1");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0);
        }

        @Test
        void pieChartDoesNotSetCategories() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            updateConfiguration("{\"chart\":{\"type\":\"pie\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0);
        }

        @Test
        void reRenderWithoutNamesClearsPreviousCategories() {
            // First render: items with names → categories set
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));
            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();
            Assertions.assertNotNull(
                    chart.getConfiguration().getxAxis().getCategories());

            // Second render: numeric X/Y data without names → categories
            // should be cleared
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 10),
                    row(ColumnNames.X, 2, ColumnNames.Y, 20));
            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x, y FROM t");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0,
                    "Categories should be cleared when re-rendering with "
                            + "numeric data");
        }

        @Test
        void emptySeriesPreservesAxisType() {
            controller.setDataConverter(data -> List.of(new DataSeries()));
            databaseProvider.results = List.of();

            // First render: establish a LINEAR axis type via config
            updateConfiguration(
                    "{\"chart\":{\"type\":\"line\"},\"xAxis\":{\"type\":\"linear\"}}");
            updateData("SELECT 1");
            controller.onResponseComplete();

            Assertions.assertEquals(AxisType.LINEAR,
                    chart.getConfiguration().getxAxis().getType());

            // Second render: same chart type preserves existing config
            // (merge behavior). Axis type remains LINEAR since config
            // doesn't override it and there is no series data to
            // trigger auto-detection.
            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT 1");
            controller.onResponseComplete();

            Assertions.assertEquals(AxisType.LINEAR,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void ganttSeriesSetsDatetimeAxis() {
            // Gantt series need datetime X-axis — test via the default
            // converter with Gantt column names
            databaseProvider.results = List
                    .of(row(ColumnNames.NAME, "Task 1", ColumnNames.START,
                            1704067200000L, ColumnNames.END, 1704153600000L));

            updateConfiguration("{\"chart\":{\"type\":\"gantt\"}}");
            updateData("SELECT name, start, end");
            controller.onResponseComplete();

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void ohlcChartDoesNotSetCategoryAxis() {
            // OHLC/candlestick items have names (dates) and datetime X
            // values; categories should NOT be set from names, so the
            // datetime axis can render formatted dates instead of raw epoch
            // strings.
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            updateConfiguration("{\"chart\":{\"type\":\"candlestick\"}}");
            updateData("SELECT trade_date, open, high, low, close");
            controller.onResponseComplete();

            // Should have datetime axis, not categories
            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
            var categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0,
                    "OHLC chart should not have category axis");
        }

        @Test
        void multipleQueriesWithMixedXValuesDetectsDatetime() {
            // Use a converter that returns both a datetime series and a
            // volume series with row-index X values
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            controller.setDataConverter(data -> {
                DataSeries ohlcSeries = new DataSeries("OHLC");
                ohlcSeries.add(new OhlcItem(1704067200000L, 142.5, 148.2, 141.0,
                        147.8));
                DataSeries volumeSeries = new DataSeries("Volume");
                volumeSeries.add(new DataSeriesItem(0, 1200000));
                return List.of(ohlcSeries, volumeSeries);
            });

            updateConfiguration("{\"chart\":{\"type\":\"candlestick\"}}");
            updateData("SELECT 1");
            controller.onResponseComplete();

            // Even though volumeSeries has X=0, the OHLC series with epoch
            // X values should still cause datetime detection
            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void datetimeXValuesSetsDatetimeAxisType() {
            // Epoch ms for 2024-01-01: 1704067200000
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1704067200000L, ColumnNames.Y, 10),
                    row(ColumnNames.X, 1704153600000L, ColumnNames.Y, 20));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x, y FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void smallXValuesDoesNotSetDatetimeAxisType() {
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 10),
                    row(ColumnNames.X, 2, ColumnNames.Y, 20));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x, y FROM t");
            controller.onResponseComplete();

            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void nullChartTypeSetsCategoriesFromNames() {
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));

            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertNotNull(categories,
                    "Categories should be set when chart type is null");
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb" },
                    categories);
        }

        @Test
        void mixedXValuesSmallAndLargeDoesNotSetDatetime() {
            // First value is a timestamp, second is small — should NOT be
            // detected as datetime since not all values qualify
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1704067200000L, ColumnNames.Y, 10),
                    row(ColumnNames.X, 5, ColumnNames.Y, 20));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x, y FROM t");
            controller.onResponseComplete();

            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

    }

    @Nested
    class NameUnnamedSeries {

        @Test
        void singleUnnamedSeriesUsesChartTitle() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            updateConfiguration(
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Revenue\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());
            Assertions.assertEquals("Revenue", series.get(0).getName());
        }

        @Test
        void singleUnnamedSeriesWithNoTitleKeepsNull() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());
            Assertions.assertNull(series.get(0).getName());
        }

        @Test
        void perSeriesPlotOptions_appliedToSingleUnnamedSeries() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            updateConfiguration("{\"chart\":{\"type\":\"areaspline\"},"
                    + "\"title\":{\"text\":\"Revenue\"},"
                    + "\"series\":[{\"name\":\"Revenue\","
                    + "\"type\":\"areaspline\","
                    + "\"plotOptions\":{\"fillColor\":\"green\"}}]}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());
            // Series should be named from title AND have plotOptions applied
            Assertions.assertEquals("Revenue", series.get(0).getName());
            var plotOptions = ((AbstractSeries) series.get(0)).getPlotOptions();
            Assertions.assertNotNull(plotOptions,
                    "Per-series plotOptions should be applied to "
                            + "single unnamed series after title-based naming");
        }

        @Test
        void multiQueryUnnamedSeriesReceiveNamesFromConfig() {
            // When two separate queries produce unnamed series (e.g.
            // candlestick OHLC + volume) and the LLM config provides a
            // series array with names, the names should be applied
            // positionally to the unnamed data series.
            int[] callCount = { 0 };
            controller.setDataConverter(data -> {
                callCount[0]++;
                if (callCount[0] == 1) {
                    DataSeries ohlcSeries = new DataSeries();
                    ohlcSeries.add(new OhlcItem(1704067200000L, 142.5, 148.2,
                            141.0, 147.8));
                    return List.of(ohlcSeries);
                } else {
                    DataSeries volumeSeries = new DataSeries();
                    volumeSeries.add(new DataSeriesItem(1704067200000L, 52000));
                    return List.of(volumeSeries);
                }
            });

            updateConfiguration("""
                    {"chart":{"type":"candlestick"},
                     "yAxis":[{"title":{"text":"Price"}},
                              {"title":{"text":"Volume"},"opposite":true}],
                     "series":[{"name":"Prices","type":"candlestick"},
                               {"name":"Vol","type":"column","yAxis":1}]}
                    """);
            updateData("SELECT 1", "SELECT 2");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            Assertions.assertEquals("Prices", series.get(0).getName(),
                    "First unnamed series should receive name from config");
            Assertions.assertEquals("Vol", series.get(1).getName(),
                    "Second unnamed series should receive name from config");
        }

        @Test
        void configSeriesNamesOverrideSeriesColumnNames() {
            // When _SERIES column produces named series but the config
            // also provides explicit series names, the config names
            // should win (the user asked for specific legend labels).
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "North", "category", "Jan", "value",
                            45000),
                    row(ColumnNames.SERIES, "South", "category", "Jan", "value",
                            38000));

            updateConfiguration("""
                    {"chart":{"type":"column"},
                     "series":[{"name":"Revenue","yAxis":0},
                               {"name":"Costs","yAxis":1}]}
                    """);
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            Assertions.assertEquals("Revenue", series.get(0).getName(),
                    "Config name should override _SERIES name");
            Assertions.assertEquals("Costs", series.get(1).getName(),
                    "Config name should override _SERIES name");
        }

        @Test
        void nameMatchedSeriesNotOverwrittenByPositionalFallback() {
            // When one data series matches a config template by name and
            // another doesn't, the name-matched series must not be
            // re-processed by positional fallback. The name-matched
            // series ("Revenue") is deliberately second in data order so
            // that positional fallback — if it ignored matched tracking —
            // would assign it the wrong template.
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "Other", "category", "Jan", "value",
                            38000),
                    row(ColumnNames.SERIES, "Revenue", "category", "Jan",
                            "value", 45000));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"series\":[" + "{\"name\":\"Revenue\",\"yAxis\":0},"
                    + "{\"name\":\"Costs\",\"yAxis\":1}" + "]}");
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            // "Other" did not match — positional fallback renames to "Costs"
            Assertions.assertEquals("Costs", series.get(0).getName());
            Assertions.assertEquals(1,
                    ((AbstractSeries) series.get(0)).getyAxis(),
                    "Positionally matched series should get template yAxis");
            // "Revenue" matched by name — must keep its name and yAxis
            Assertions.assertEquals("Revenue", series.get(1).getName());
            Assertions.assertEquals(0,
                    ((AbstractSeries) series.get(1)).getyAxis(),
                    "Name-matched series should keep its yAxis");
        }

        @Test
        void positionalMatchAppliesPlotOptionsAndYAxis() {
            // Positional fallback must apply plotOptions and yAxis from
            // the config template, not just the name.
            int[] callCount = { 0 };
            controller.setDataConverter(data -> {
                callCount[0]++;
                if (callCount[0] == 1) {
                    DataSeries s = new DataSeries();
                    s.add(new DataSeriesItem("Jan", 100));
                    return List.of(s);
                } else {
                    DataSeries s = new DataSeries();
                    s.add(new DataSeriesItem("Jan", 200));
                    return List.of(s);
                }
            });

            updateConfiguration("""
                    {"chart":{"type":"column"},
                     "series":[{"name":"Revenue","type":"column","yAxis":0},
                               {"name":"Count","type":"line","yAxis":1}]}
                    """);
            updateData("SELECT 1", "SELECT 2");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            var countSeries = (AbstractSeries) series.get(1);
            Assertions.assertEquals("Count", countSeries.getName());
            Assertions.assertEquals(1, countSeries.getyAxis(),
                    "Positional match should apply yAxis from template");
            Assertions.assertInstanceOf(PlotOptionsLine.class,
                    countSeries.getPlotOptions(),
                    "Positional match should apply plotOptions from template");
        }

        @Test
        void dataOrderDifferentFromConfigOrderPreservesDataLabels() {
            // The LLM writes the config series[] array without any
            // guarantee that the SQL data will come back in that order
            // (GROUP BY without ORDER BY, DB-side hash ordering, etc.).
            // When data order differs from config order, matching by
            // position renames data series to the WRONG labels —
            // effectively swapping data points under their legend
            // labels. Matching by name (the prior behavior) avoids this.
            controller.setDataConverter(data -> {
                // Simulate DB returning "South" before "North"
                DataSeries south = new DataSeries("South");
                south.add(new DataSeriesItem("Jan", 38000));
                DataSeries north = new DataSeries("North");
                north.add(new DataSeriesItem("Jan", 45000));
                return List.of(south, north);
            });

            updateConfiguration("""
                    {"chart":{"type":"column"},
                     "yAxis":[{"title":{"text":"Primary"}},
                              {"title":{"text":"Secondary"},"opposite":true}],
                     "series":[{"name":"North","yAxis":0},
                               {"name":"South","yAxis":1}]}
                    """);
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            // The first data series is South (value 38000); its label
            // must remain "South", not be overwritten to "North".
            Assertions.assertEquals("South", series.get(0).getName(),
                    "Data series labels must not be swapped when data "
                            + "order differs from config order");
            Assertions.assertEquals("North", series.get(1).getName());
            // Similarly the yAxis assignment should follow the name,
            // so that "South" sits on the Secondary axis (yAxis=1).
            Assertions.assertEquals(1,
                    ((AbstractSeries) series.get(0)).getyAxis(),
                    "South should end up on Secondary y-axis (yAxis=1), "
                            + "not Primary");
            Assertions.assertEquals(0,
                    ((AbstractSeries) series.get(1)).getyAxis(),
                    "North should end up on Primary y-axis (yAxis=0)");
        }

        @Test
        void multipleSeriesWithSeriesColumnKeepsOriginalNames() {
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "Series A", "category", "Jan",
                            "value", 100),
                    row(ColumnNames.SERIES, "Series B", "category", "Jan",
                            "value", 200));

            updateConfiguration(
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Revenue\"}}");
            updateData("SELECT series_name, category, value FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            Assertions.assertEquals("Series A", series.get(0).getName());
            Assertions.assertEquals("Series B", series.get(1).getName());
        }
    }

    @Nested
    class ConfigurationReset {

        @Test
        void resetDoesNotSetEmptyCategoriesArray() {
            // If reset sets categories to an empty ArrayList instead of
            // null, Highcharts treats the axis as a category axis (creates
            // a tick for every unique data value). Verify via serialization
            // that categories property is not emitted after reset.
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"xAxis\":{\"categories\":[\"A\",\"B\"]}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Categories were set on X-axis
            Assertions.assertTrue(chart.getConfiguration().getxAxis()
                    .getCategories().length > 0);

            // Second render: scatter chart without categories
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 100),
                    row(ColumnNames.X, 2, ColumnNames.Y, 200));

            updateConfiguration("{\"chart\":{\"type\":\"scatter\"}}");
            updateData("SELECT x, y FROM t");
            controller.onResponseComplete();

            // Y-axis serialization must NOT contain "categories" — check
            // via JSON to distinguish null field from empty ArrayList
            String json = ChartSerialization.toJSON(chart.getConfiguration());
            // Parse and check the yAxis doesn't have categories
            Assertions.assertFalse(json.contains("\"categories\":[]"),
                    "Reset axes should not emit empty categories array: "
                            + json);
        }

        @Test
        void tooltipFromHeatmapDoesNotLeakToCandlestick() {
            // First render: heatmap with custom tooltip
            databaseProvider.results = List.of(row(ColumnNames.X, 9,
                    ColumnNames.Y, 0, ColumnNames.VALUE, 120));

            updateConfiguration("{\"chart\":{\"type\":\"heatmap\"},"
                    + "\"tooltip\":{\"pointFormat\":\"Day: {point.y}<br>Hour: {point.x}<br>Visitors: {point.value}\"}}");
            updateData("SELECT x, y, value");
            controller.onResponseComplete();

            Assertions.assertNotNull(
                    chart.getConfiguration().getTooltip().getPointFormat());

            // Second render: candlestick without tooltip config
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            updateConfiguration("{\"chart\":{\"type\":\"candlestick\"},"
                    + "\"title\":{\"text\":\"Stock Prices\"}}");
            updateData("SELECT trade_date, open");
            controller.onResponseComplete();

            // Tooltip should be reset, not carry heatmap format
            Assertions.assertNull(
                    chart.getConfiguration().getTooltip().getPointFormat());
        }

        @Test
        void axisTypeFromDatetimeDoesNotLeakToCategory() {
            // First render: candlestick with datetime axis
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            updateConfiguration("{\"chart\":{\"type\":\"candlestick\"}}");
            updateData("SELECT trade_date, open, high, low, close");
            controller.onResponseComplete();

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());

            // Second render: column chart with categories
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"title\":{\"text\":\"Revenue\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Datetime axis type should be cleared, categories used instead
            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
            Assertions.assertNotNull(
                    chart.getConfiguration().getxAxis().getCategories());
        }

        @Test
        void axisMinMaxFromGaugeDoesNotLeakToColumn() {
            // First render: gauge with explicit min/max
            databaseProvider.results = List.of(row(ColumnNames.Y, 78));

            updateConfiguration("{\"chart\":{\"type\":\"gauge\"},"
                    + "\"yAxis\":{\"min\":0,\"max\":100}}");
            updateData("SELECT current_val");
            controller.onResponseComplete();

            Assertions.assertEquals(0.0,
                    chart.getConfiguration().getyAxis().getMin().doubleValue());
            Assertions.assertEquals(100.0,
                    chart.getConfiguration().getyAxis().getMax().doubleValue());

            // Second render: column chart without min/max
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Gauge min/max should be cleared
            Assertions.assertNull(chart.getConfiguration().getyAxis().getMin());
            Assertions.assertNull(chart.getConfiguration().getyAxis().getMax());
        }

        @Test
        void plotOptionsFromStackedDoesNotLeakToLine() {
            // First render: stacked column
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"plotOptions\":{\"column\":{\"stacking\":\"normal\"}}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions.assertNotNull(
                    chart.getConfiguration().getPlotOptions(ChartType.COLUMN));

            // Second render: plain line
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Stacked column plot options should be cleared
            Assertions.assertNull(
                    chart.getConfiguration().getPlotOptions(ChartType.COLUMN));
        }

        @Test
        void colorAxisFromHeatmapDoesNotLeakToColumn() {
            // First render: heatmap with color axis
            databaseProvider.results = List.of(row(ColumnNames.X, 0,
                    ColumnNames.Y, 0, ColumnNames.VALUE, 100));

            updateConfiguration("{\"chart\":{\"type\":\"heatmap\"},"
                    + "\"colorAxis\":{\"min\":0,\"max\":300}}");
            updateData("SELECT x, y, value");
            controller.onResponseComplete();

            Assertions.assertEquals(1,
                    chart.getConfiguration().getNumberOfColorAxes());

            // Second render: column chart
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Color axis should be cleared
            Assertions.assertEquals(0,
                    chart.getConfiguration().getNumberOfColorAxes());
        }

        @Test
        void paneFromGaugeDoesNotLeakToColumn() {
            // First render: gauge with pane config
            databaseProvider.results = List.of(row(ColumnNames.Y, 78));

            updateConfiguration("{\"chart\":{\"type\":\"gauge\"},"
                    + "\"pane\":{\"startAngle\":-150,\"endAngle\":150,"
                    + "\"center\":[\"50%\",\"50%\"],\"size\":\"80%\"},"
                    + "\"yAxis\":{\"min\":0,\"max\":100}}");
            updateData("SELECT current_val");
            controller.onResponseComplete();

            // Pane should be set
            String json1 = ChartSerialization.toJSON(chart.getConfiguration());
            Assertions.assertTrue(json1.contains("\"pane\""),
                    "Gauge should have pane config");

            // Second render: column chart without pane
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Pane should be cleared
            String json2 = ChartSerialization.toJSON(chart.getConfiguration());
            Assertions.assertFalse(json2.contains("\"startAngle\""),
                    "Pane startAngle from gauge should not leak to column: "
                            + json2);
        }

        @Test
        void legendFromPieChartDoesNotLeakToColumn() {
            // First render: pie chart with legend disabled
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"pie\"},"
                    + "\"legend\":{\"enabled\":false}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions.assertFalse(
                    chart.getConfiguration().getLegend().getEnabled());

            // Second render: column chart without legend config
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Legend should be reset to default (enabled=true or null)
            Boolean legendEnabled = chart.getConfiguration().getLegend()
                    .getEnabled();
            Assertions.assertTrue(legendEnabled == null || legendEnabled,
                    "Legend enabled=false from pie should not leak to column");
        }

        @Test
        void subtitleFromPreviousDoesNotLeak() {
            // First render with subtitle
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"subtitle\":{\"text\":\"Q1 2024\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals("Q1 2024",
                    chart.getConfiguration().getSubTitle().getText());

            // Second render without subtitle
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            // Subtitle should be cleared
            Assertions.assertNull(
                    chart.getConfiguration().getSubTitle().getText());
        }

        @Test
        void polarFromGaugeDoesNotLeakToColumn() {
            // First render: polar chart
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration(
                    "{\"chart\":{\"type\":\"line\",\"polar\":true}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions
                    .assertTrue(chart.getConfiguration().getChart().getPolar());

            // Second render: column chart (not polar)
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            controller.onResponseComplete();

            Assertions
                    .assertNull(chart.getConfiguration().getChart().getPolar());
        }

        @Test
        void repeatedGaugeRendersDoNotAccumulatePanes() {
            databaseProvider.results = List.of(row(ColumnNames.Y, 78));

            // First gauge render
            updateConfiguration("{\"chart\":{\"type\":\"gauge\"},"
                    + "\"pane\":{\"startAngle\":-150,\"endAngle\":150},"
                    + "\"yAxis\":{\"min\":0,\"max\":100}}");
            updateData("SELECT current_val");
            controller.onResponseComplete();

            // Second gauge render (e.g. user changes the gauge value)
            databaseProvider.results = List.of(row(ColumnNames.Y, 85));
            updateConfiguration("{\"chart\":{\"type\":\"gauge\"},"
                    + "\"pane\":{\"startAngle\":-150,\"endAngle\":150},"
                    + "\"yAxis\":{\"min\":0,\"max\":100}}");
            updateData("SELECT current_val");
            controller.onResponseComplete();

            // Should have exactly 1 pane, not 2
            String json = ChartSerialization.toJSON(chart.getConfiguration());
            int paneCount = json.split("\"startAngle\"").length - 1;
            Assertions.assertEquals(1, paneCount,
                    "Repeated gauge renders should not accumulate panes: "
                            + json);
        }

    }

    @Nested
    class DualYAxis {

        @Test
        void secondaryYAxisDoesNotAccumulateOnReRender() {
            // First render: column chart with dual y-axes
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "North", "category", "Jan", "value",
                            45000),
                    row(ColumnNames.SERIES, "South", "category", "Jan", "value",
                            38000));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"yAxis\":[" + "{\"title\":{\"text\":\"Revenue\"}},"
                    + "{\"title\":{\"text\":\"Volume\"},\"opposite\":true}"
                    + "]," + "\"series\":["
                    + "{\"name\":\"North\",\"yAxis\":0},"
                    + "{\"name\":\"South\",\"yAxis\":1}" + "]}");
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals(2,
                    chart.getConfiguration().getNumberOfyAxes(),
                    "First render should have 2 y-axes");

            // Second render: same chart type with same dual y-axes
            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"yAxis\":[" + "{\"title\":{\"text\":\"Revenue\"}},"
                    + "{\"title\":{\"text\":\"Volume\"},\"opposite\":true}"
                    + "]," + "\"series\":["
                    + "{\"name\":\"North\",\"yAxis\":0},"
                    + "{\"name\":\"South\",\"yAxis\":1}" + "]}");
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            Assertions.assertEquals(2,
                    chart.getConfiguration().getNumberOfyAxes(),
                    "Re-render should still have exactly 2 y-axes, "
                            + "not accumulate");
        }

        @Test
        void perSeriesYAxisAppliedToDataSeries() {
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "Revenue", "category", "Jan",
                            "value", 45000),
                    row(ColumnNames.SERIES, "Volume", "category", "Jan",
                            "value", 52000));

            updateConfiguration("{\"chart\":{\"type\":\"column\"},"
                    + "\"yAxis\":[" + "{\"title\":{\"text\":\"Revenue\"}},"
                    + "{\"title\":{\"text\":\"Volume\"},\"opposite\":true}"
                    + "]," + "\"series\":["
                    + "{\"name\":\"Revenue\",\"yAxis\":0},"
                    + "{\"name\":\"Volume\",\"yAxis\":1}" + "]}");
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());

            var revSeries = (com.vaadin.flow.component.charts.model.AbstractSeries) series
                    .stream().filter(s -> "Revenue".equals(s.getName()))
                    .findFirst().orElseThrow();
            var volSeries = (com.vaadin.flow.component.charts.model.AbstractSeries) series
                    .stream().filter(s -> "Volume".equals(s.getName()))
                    .findFirst().orElseThrow();

            Assertions.assertEquals(0, revSeries.getyAxis(),
                    "Revenue should be on primary y-axis");
            Assertions.assertEquals(1, volSeries.getyAxis(),
                    "Volume should be on secondary y-axis");
        }
    }

    @Nested
    class PerSeriesTypeOverride {

        @Test
        void perSeriesTypeWithoutPlotOptions_appliesType() {
            // LLM sends type override without explicit plotOptions
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "Revenue", "category", "Jan",
                            "value", 45000),
                    row(ColumnNames.SERIES, "Count", "category", "Jan", "value",
                            120));

            updateConfiguration(
                    "{\"chart\":{\"type\":\"column\"}," + "\"series\":["
                            + "{\"name\":\"Revenue\",\"type\":\"column\"},"
                            + "{\"name\":\"Count\",\"type\":\"line\"}" + "]}");
            updateData("SELECT s, c, v FROM t");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            var countSeries = (com.vaadin.flow.component.charts.model.AbstractSeries) series
                    .stream().filter(s -> "Count".equals(s.getName()))
                    .findFirst().orElseThrow();

            Assertions.assertNotNull(countSeries.getPlotOptions(),
                    "Per-series type override should create plotOptions "
                            + "even without explicit plotOptions in config");
            Assertions.assertInstanceOf(
                    com.vaadin.flow.component.charts.model.PlotOptionsLine.class,
                    countSeries.getPlotOptions(),
                    "Count series should have line plotOptions from "
                            + "type override");
        }
    }

    @Nested
    class WaterfallChart {

        @Test
        void waterfallWithSumItemsCategoriesIncludeSumNames() {
            // Waterfall data with regular items, intermediate sum, and
            // final sum — all should have names used as categories
            databaseProvider.results = List.of(
                    row(ColumnNames.NAME, "Revenue", ColumnNames.Y, 420000,
                            ColumnNames.WATERFALL_TYPE, null),
                    row(ColumnNames.NAME, "Cost of Goods", ColumnNames.Y,
                            -180000, ColumnNames.WATERFALL_TYPE, null),
                    row(ColumnNames.NAME, "Gross Profit", ColumnNames.Y, 0,
                            ColumnNames.WATERFALL_TYPE, "intermediate"),
                    row(ColumnNames.NAME, "Salaries", ColumnNames.Y, -120000,
                            ColumnNames.WATERFALL_TYPE, null),
                    row(ColumnNames.NAME, "Net Profit", ColumnNames.Y, 0,
                            ColumnNames.WATERFALL_TYPE, "sum"));

            updateConfiguration("{\"chart\":{\"type\":\"waterfall\"},"
                    + "\"title\":{\"text\":\"Budget\"}}");
            updateData("SELECT name, y, type");
            controller.onResponseComplete();

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertNotNull(categories,
                    "Waterfall should have categories from item names");
            Assertions.assertEquals(5, categories.length);
            Assertions.assertEquals("Revenue", categories[0]);
            Assertions.assertEquals("Gross Profit", categories[2]);
            Assertions.assertEquals("Net Profit", categories[4]);
        }
    }

    // --- Helpers ---

    @Nested
    class FlagsChart {

        @Test
        void flagsSeriesRendersAsFlagsOnColumnChart() {
            databaseProvider.results = List
                    .of(row("_title", "Launch", "_text", "Product launch"));

            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT title AS _title, text AS _text FROM flags");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());

            var flagsSeries = (DataSeries) series.getFirst();
            Assertions.assertInstanceOf(PlotOptionsFlags.class,
                    flagsSeries.getPlotOptions(),
                    "Flags series should have PlotOptionsFlags "
                            + "regardless of chart-level type");
        }

        @Test
        void flagsSeriesPreservesExistingPlotOptionsFlags() {
            databaseProvider.results = List
                    .of(row("_title", "Launch", "_text", "Product launch"));

            updateConfiguration("{\"chart\":{\"type\":\"line\"},"
                    + "\"title\":{\"text\":\"Events\"},"
                    + "\"series\":[{\"name\":\"Events\","
                    + "\"type\":\"flags\","
                    + "\"plotOptions\":{\"onSeries\":\"price\"}}]}");
            updateData("SELECT title AS _title, text AS _text FROM flags");
            controller.onResponseComplete();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());

            var flagsSeries = (DataSeries) series.getFirst();
            var plotOptions = flagsSeries.getPlotOptions();
            Assertions.assertInstanceOf(PlotOptionsFlags.class, plotOptions,
                    "Flags series should have PlotOptionsFlags");
            Assertions.assertEquals("price",
                    ((PlotOptionsFlags) plotOptions).getOnSeries(),
                    "Pre-configured onSeries should be preserved");
        }
    }

    private static Map<String, Object> row(Object... kvPairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            map.put((String) kvPairs[i], kvPairs[i + 1]);
        }
        return map;
    }

    private static class TestDatabaseProvider implements DatabaseProvider {

        List<Map<String, Object>> results = new ArrayList<>();
        RuntimeException throwOnExecute;

        @Override
        public String getSchema() {
            return "test schema";
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            if (throwOnExecute != null) {
                throw throwOnExecute;
            }
            return results;
        }
    }
}
