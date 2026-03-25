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

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
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
    void setDataConverter_customConverter_isUsed() {
        DataConverter custom = data -> List.of(new DataSeries("custom-series"));
        renderer.setDataConverter(custom);
        Assertions.assertSame(custom, renderer.getDataConverter());
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
