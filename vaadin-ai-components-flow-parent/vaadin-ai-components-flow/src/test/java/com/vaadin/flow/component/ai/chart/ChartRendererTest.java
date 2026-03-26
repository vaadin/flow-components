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
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.OhlcItem;
import com.vaadin.tests.MockUIExtension;

class ChartRendererTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;
    private TestDatabaseProvider databaseProvider;
    private ChartRenderer renderer;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
        databaseProvider = new TestDatabaseProvider();
        renderer = new ChartRenderer(databaseProvider);
    }

    @Test
    void constructor_nullProvider_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new ChartRenderer(null));
    }

    @Test
    void setDataConverter_nullConverter_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> renderer.setDataConverter(null));
    }

    @Test
    void setDataConverter_customConverter_isUsedDuringRendering() {
        databaseProvider.results = List.of(row("x", 1, "y", 10));

        AtomicBoolean called = new AtomicBoolean(false);
        renderer.setDataConverter(data -> {
            called.set(true);
            DataSeries series = new DataSeries("custom");
            series.add(new DataSeriesItem("A", 42));
            return List.of(series);
        });

        renderer.renderChart(chart, List.of("SELECT 1"),
                "{\"chart\":{\"type\":\"column\"}}");

        Assertions.assertTrue(called.get(),
                "Custom DataConverter should have been called");
        Assertions.assertEquals("custom",
                chart.getConfiguration().getSeries().get(0).getName());
    }

    @Nested
    class ApplyPendingState {

        @Test
        void noEntry_doesNothing() {
            renderer.applyPendingState(chart);
            // No exception, no configuration change
        }

        @Test
        void noPendingState_doesNothing() {
            ChartEntry.getOrCreate(chart, "test");
            renderer.applyPendingState(chart);
        }

        @Test
        void pendingConfigOnly_appliesConfiguration() {
            ChartEntry entry = ChartEntry.getOrCreate(chart, "test");
            entry.setPendingConfigurationJson(
                    "{\"title\":{\"text\":\"My Title\"}}");

            renderer.applyPendingState(chart);

            Assertions.assertFalse(entry.hasPendingState());
            Assertions.assertEquals("My Title",
                    chart.getConfiguration().getTitle().getText());
        }

        @Test
        void pendingDataAndConfig_rendersChart() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            ChartEntry entry = ChartEntry.getOrCreate(chart, "test");
            entry.setQueries(List.of("SELECT category, value FROM t"));
            entry.setPendingDataUpdate(true);
            entry.setPendingConfigurationJson(
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Sales\"}}");

            renderer.applyPendingState(chart);

            Assertions.assertFalse(entry.hasPendingState());
            Configuration config = chart.getConfiguration();
            Assertions.assertEquals(ChartType.COLUMN,
                    config.getChart().getType());
            Assertions.assertEquals("Sales", config.getTitle().getText());
            Assertions.assertFalse(config.getSeries().isEmpty());
        }

        @Test
        void pendingDataOnly_usesExistingConfig() {
            chart.getConfiguration().setTitle("Existing Title");
            chart.getConfiguration().getChart().setType(ChartType.COLUMN);
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            ChartEntry entry = ChartEntry.getOrCreate(chart, "test");
            entry.setQueries(List.of("SELECT category, value FROM t"));
            entry.setPendingDataUpdate(true);

            renderer.applyPendingState(chart);

            Assertions.assertFalse(entry.hasPendingState());
            Assertions.assertEquals("Existing Title",
                    chart.getConfiguration().getTitle().getText());
            Assertions.assertFalse(
                    chart.getConfiguration().getSeries().isEmpty());
        }

        @Test
        void clearsPendingStateEvenOnError() {
            databaseProvider.throwOnExecute = new RuntimeException("DB error");

            ChartEntry entry = ChartEntry.getOrCreate(chart, "test");
            entry.setQueries(List.of("SELECT 1"));
            entry.setPendingDataUpdate(true);

            Assertions.assertThrows(RuntimeException.class,
                    () -> renderer.applyPendingState(chart));

            Assertions.assertFalse(entry.hasPendingState());
        }
    }

    @Nested
    class RenderChart {

        @Test
        void singleQuery_createsSeries() {
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");

            Configuration config = chart.getConfiguration();
            Assertions.assertEquals(ChartType.COLUMN,
                    config.getChart().getType());
            Assertions.assertFalse(config.getSeries().isEmpty());
        }

        @Test
        void multipleQueries_createsMultipleSeries() {
            databaseProvider.results = List.of(row("x", 1, "y", 10));

            renderer.renderChart(chart,
                    List.of("SELECT x, y FROM t1", "SELECT x, y FROM t2"),
                    "{\"chart\":{\"type\":\"line\"}}");

            Assertions.assertEquals(2,
                    chart.getConfiguration().getSeries().size());
        }

        @Test
        void nullConfig_keepsCurrentConfiguration() {
            chart.getConfiguration().setTitle("Keep Me");
            chart.getConfiguration().getChart().setType(ChartType.LINE);

            databaseProvider.results = List
                    .of(row(ColumnNames.X, 1, ColumnNames.Y, 10));

            renderer.renderChart(chart, List.of("SELECT x, y FROM t"), null);

            Assertions.assertEquals("Keep Me",
                    chart.getConfiguration().getTitle().getText());
        }

        @Test
        void callsDrawChart() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");

            // drawChart(true) triggers a full redraw — verify by checking
            // that series data is present on the configuration (drawChart
            // would fail if configuration was inconsistent)
            Assertions.assertFalse(
                    chart.getConfiguration().getSeries().isEmpty());
        }
    }

    @Nested
    class AxisDefaults {

        @Test
        void itemsWithNames_setsCategoriesOnXAxis() {
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertNotNull(categories);
            Assertions.assertArrayEquals(new String[] { "Jan", "Feb" },
                    categories);
        }

        @Test
        void itemsWithoutNames_doesNotSetCategories() {
            // Items with X/Y numeric data (no names) should not set categories
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 10),
                    row(ColumnNames.X, 2, ColumnNames.Y, 20));

            renderer.renderChart(chart, List.of("SELECT x, y FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0);
        }

        @Test
        void mixedNamesAndNull_doesNotSetCategories() {
            // When some items have names and some don't, categories should
            // not be set (extractCategories returns null)
            databaseProvider.results = List.of(row("x", 1, "y", 10));

            renderer.setDataConverter(data -> {
                DataSeries series = new DataSeries();
                series.add(new DataSeriesItem("Jan", 100));
                series.add(new DataSeriesItem(1, 200)); // no name
                return List.of(series);
            });

            renderer.renderChart(chart, List.of("SELECT 1"),
                    "{\"chart\":{\"type\":\"column\"}}");

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0);
        }

        @Test
        void pieChart_doesNotSetCategories() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"pie\"}}");

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0);
        }

        @Test
        void reRenderWithoutNames_clearsPreviousCategories() {
            // First render: items with names → categories set
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));
            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");
            Assertions.assertNotNull(
                    chart.getConfiguration().getxAxis().getCategories());

            // Second render: numeric X/Y data without names → categories
            // should be cleared
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 10),
                    row(ColumnNames.X, 2, ColumnNames.Y, 20));
            renderer.renderChart(chart, List.of("SELECT x, y FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            String[] categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0,
                    "Categories should be cleared when re-rendering with "
                            + "numeric data");
        }

        @Test
        void emptySeries_resetsAxisType() {
            databaseProvider.results = List.of();

            // Use a custom converter that returns an empty list
            renderer.setDataConverter(data -> List.of(new DataSeries()));

            chart.getConfiguration().getxAxis().setType(AxisType.LINEAR);

            renderer.renderChart(chart, List.of("SELECT 1"),
                    "{\"chart\":{\"type\":\"line\"}}");

            // Configuration reset clears stale axis type when config
            // doesn't specify one, so Highcharts auto-detects
            Assertions
                    .assertNull(chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void ganttSeries_setsDatetimeAxis() {
            // Gantt series need datetime X-axis — but we can't easily create
            // GanttSeries from the converter, so test via the default converter
            // with Gantt column names
            databaseProvider.results = List
                    .of(row(ColumnNames.NAME, "Task 1", ColumnNames.START,
                            1704067200000L, ColumnNames.END, 1704153600000L));

            renderer.renderChart(chart, List.of("SELECT name, start, end"),
                    "{\"chart\":{\"type\":\"gantt\"}}");

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void ohlcChart_doesNotSetCategoryAxis() {
            // OHLC/candlestick items have names (dates) and datetime X values;
            // categories should NOT be set from names, so the datetime axis
            // can render formatted dates instead of raw epoch strings.
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            renderer.renderChart(chart,
                    List.of("SELECT trade_date, open, high, low, close"),
                    "{\"chart\":{\"type\":\"candlestick\"}}");

            // Should have datetime axis, not categories
            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
            var categories = chart.getConfiguration().getxAxis()
                    .getCategories();
            Assertions.assertTrue(categories == null || categories.length == 0,
                    "OHLC chart should not have category axis");
        }

        @Test
        void multipleQueries_withMixedXValues_detectsDatetime() {
            // First query: OHLC with epoch X values
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            // Use a converter that returns both a datetime series and a
            // volume series with row-index X values
            renderer.setDataConverter(data -> {
                DataSeries ohlcSeries = new DataSeries("OHLC");
                ohlcSeries.add(new OhlcItem(1704067200000L, 142.5, 148.2, 141.0,
                        147.8));
                DataSeries volumeSeries = new DataSeries("Volume");
                volumeSeries.add(new DataSeriesItem(0, 1200000));
                return List.of(ohlcSeries, volumeSeries);
            });

            renderer.renderChart(chart, List.of("SELECT 1"),
                    "{\"chart\":{\"type\":\"candlestick\"}}");

            // Even though volumeSeries has X=0, the OHLC series with epoch
            // X values should still cause datetime detection
            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void datetimeXValues_setsDatetimeAxisType() {
            // Epoch ms for 2024-01-01: 1704067200000
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1704067200000L, ColumnNames.Y, 10),
                    row(ColumnNames.X, 1704153600000L, ColumnNames.Y, 20));

            renderer.renderChart(chart, List.of("SELECT x, y FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void smallXValues_doesNotSetDatetimeAxisType() {
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1, ColumnNames.Y, 10),
                    row(ColumnNames.X, 2, ColumnNames.Y, 20));

            renderer.renderChart(chart, List.of("SELECT x, y FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void mixedXValues_smallAndLarge_doesNotSetDatetime() {
            // First value is a timestamp, second is small — should NOT be
            // detected as datetime since not all values qualify
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1704067200000L, ColumnNames.Y, 10),
                    row(ColumnNames.X, 5, ColumnNames.Y, 20));

            renderer.renderChart(chart, List.of("SELECT x, y FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void allXValuesAreDatetime_setsDatetimeAxisType() {
            databaseProvider.results = List.of(
                    row(ColumnNames.X, 1704067200000L, ColumnNames.Y, 10),
                    row(ColumnNames.X, 1704153600000L, ColumnNames.Y, 20),
                    row(ColumnNames.X, 1704240000000L, ColumnNames.Y, 30));

            renderer.renderChart(chart, List.of("SELECT x, y FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }

        @Test
        void noXValues_doesNotSetDatetimeAxisType() {
            // Items with no X values — hasDatetimeXValues should return false
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");

            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
        }
    }

    @Nested
    class NameUnnamedSeries {

        @Test
        void singleUnnamedSeries_usesChartTitle() {
            databaseProvider.results = List
                    .of(row("category", "A", "value", 10));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Revenue\"}}");

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());
            Assertions.assertEquals("Revenue", series.get(0).getName());
        }

        @Test
        void multipleSeriesWithSeriesColumn_keepsOriginalNames() {
            databaseProvider.results = List.of(
                    row(ColumnNames.SERIES, "Series A", "category", "Jan",
                            "value", 100),
                    row(ColumnNames.SERIES, "Series B", "category", "Jan",
                            "value", 200));

            renderer.renderChart(chart,
                    List.of("SELECT series_name, category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"},\"title\":{\"text\":\"Revenue\"}}");

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(2, series.size());
            Assertions.assertEquals("Series A", series.get(0).getName());
            Assertions.assertEquals("Series B", series.get(1).getName());
        }
    }

    @Nested
    class ConfigurationReset {

        @Test
        void tooltipFromHeatmap_doesNotLeakToCandlestick() {
            // First render: heatmap with custom tooltip
            databaseProvider.results = List.of(row(ColumnNames.X, 9,
                    ColumnNames.Y, 0, ColumnNames.VALUE, 120));

            renderer.renderChart(chart, List.of("SELECT x, y, value"),
                    "{\"chart\":{\"type\":\"heatmap\"},"
                            + "\"tooltip\":{\"pointFormat\":\"Day: {point.y}<br>Hour: {point.x}<br>Visitors: {point.value}\"}}");

            Assertions.assertNotNull(
                    chart.getConfiguration().getTooltip().getPointFormat());

            // Second render: candlestick without tooltip config
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            renderer.renderChart(chart, List.of("SELECT trade_date, open"),
                    "{\"chart\":{\"type\":\"candlestick\"},"
                            + "\"title\":{\"text\":\"Stock Prices\"}}");

            // Tooltip should be reset, not carry heatmap format
            Assertions.assertNull(
                    chart.getConfiguration().getTooltip().getPointFormat());
        }

        @Test
        void axisTypeFromDatetime_doesNotLeakToCategory() {
            // First render: candlestick with datetime axis
            databaseProvider.results = List.of(row(ColumnNames.X,
                    1704067200000L, ColumnNames.OPEN, 142.5, ColumnNames.HIGH,
                    148.2, ColumnNames.LOW, 141.0, ColumnNames.CLOSE, 147.8));

            renderer.renderChart(chart,
                    List.of("SELECT trade_date, open, high, low, close"),
                    "{\"chart\":{\"type\":\"candlestick\"}}");

            Assertions.assertEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());

            // Second render: column chart with categories
            databaseProvider.results = List.of(
                    row("category", "Jan", "value", 100),
                    row("category", "Feb", "value", 200));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"},"
                            + "\"title\":{\"text\":\"Revenue\"}}");

            // Datetime axis type should be cleared, categories used instead
            Assertions.assertNotEquals(AxisType.DATETIME,
                    chart.getConfiguration().getxAxis().getType());
            Assertions.assertNotNull(
                    chart.getConfiguration().getxAxis().getCategories());
        }

        @Test
        void axisMinMaxFromGauge_doesNotLeakToColumn() {
            // First render: gauge with explicit min/max
            databaseProvider.results = List.of(row(ColumnNames.Y, 78));

            renderer.renderChart(chart, List.of("SELECT current_val"),
                    "{\"chart\":{\"type\":\"gauge\"},"
                            + "\"yAxis\":{\"min\":0,\"max\":100}}");

            Assertions.assertEquals(0.0,
                    chart.getConfiguration().getyAxis().getMin().doubleValue());
            Assertions.assertEquals(100.0,
                    chart.getConfiguration().getyAxis().getMax().doubleValue());

            // Second render: column chart without min/max
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");

            // Gauge min/max should be cleared
            Assertions.assertNull(chart.getConfiguration().getyAxis().getMin());
            Assertions.assertNull(chart.getConfiguration().getyAxis().getMax());
        }

        @Test
        void plotOptionsFromStacked_doesNotLeakToLine() {
            // First render: stacked column
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"},"
                            + "\"plotOptions\":{\"column\":{\"stacking\":\"normal\"}}}");

            Assertions.assertNotNull(
                    chart.getConfiguration().getPlotOptions(ChartType.COLUMN));

            // Second render: plain line
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            // Stacked column plot options should be cleared
            Assertions.assertNull(
                    chart.getConfiguration().getPlotOptions(ChartType.COLUMN));
        }

        @Test
        void colorAxisFromHeatmap_doesNotLeakToColumn() {
            // First render: heatmap with color axis
            databaseProvider.results = List.of(row(ColumnNames.X, 0,
                    ColumnNames.Y, 0, ColumnNames.VALUE, 100));

            renderer.renderChart(chart, List.of("SELECT x, y, value"),
                    "{\"chart\":{\"type\":\"heatmap\"},"
                            + "\"colorAxis\":{\"min\":0,\"max\":300}}");

            Assertions.assertEquals(1,
                    chart.getConfiguration().getNumberOfColorAxes());

            // Second render: column chart
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"}}");

            // Color axis should be cleared
            Assertions.assertEquals(0,
                    chart.getConfiguration().getNumberOfColorAxes());
        }

        @Test
        void subtitleFromPrevious_doesNotLeak() {
            // First render with subtitle
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"column\"},"
                            + "\"subtitle\":{\"text\":\"Q1 2024\"}}");

            Assertions.assertEquals("Q1 2024",
                    chart.getConfiguration().getSubTitle().getText());

            // Second render without subtitle
            databaseProvider.results = List
                    .of(row("category", "A", "value", 50));

            renderer.renderChart(chart,
                    List.of("SELECT category, value FROM t"),
                    "{\"chart\":{\"type\":\"line\"}}");

            // Subtitle should be cleared
            Assertions.assertEquals("",
                    chart.getConfiguration().getSubTitle().getText());
        }
    }

    // --- Helpers ---

    private static Map<String, Object> row(Object... kvPairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            map.put((String) kvPairs[i], kvPairs[i + 1]);
        }
        return map;
    }

    private static class TestDatabaseProvider
            implements com.vaadin.flow.component.ai.provider.DatabaseProvider {

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
