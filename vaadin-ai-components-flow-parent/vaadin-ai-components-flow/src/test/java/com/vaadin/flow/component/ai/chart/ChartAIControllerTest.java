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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.tests.MockUIExtension;

class ChartAIControllerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Chart chart;
    private TestDatabaseProvider databaseProvider;
    private ChartAIController controller;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
        databaseProvider = new TestDatabaseProvider();
        controller = new ChartAIController(chart, databaseProvider);
    }

    @Nested
    class Constructor {

        @Test
        void nullChart_throws() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new ChartAIController(null, databaseProvider));
        }

        @Test
        void nullDatabaseProvider_throws() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new ChartAIController(chart, null));
        }
    }

    @Nested
    class GetTools {

        @Test
        void returnsToolsIncludingDatabaseAndChartTools() {
            var tools = controller.getTools();
            // DatabaseProviderAITools provides 1 tool (get_database_schema)
            // ChartAITools provides 3 tools
            Assertions.assertEquals(4, tools.size());
        }

        @Test
        void toolNamesIncludeExpected() {
            var names = controller.getTools().stream().map(t -> t.getName())
                    .toList();
            Assertions.assertTrue(names.contains("get_database_schema"));
            Assertions.assertTrue(names.contains("get_chart_state"));
            Assertions.assertTrue(names.contains("update_chart_configuration"));
            Assertions.assertTrue(names.contains("update_chart_data_source"));
        }
    }

    @Nested
    class GetSystemPrompt {

        @Test
        void isNotEmpty() {
            Assertions
                    .assertFalse(ChartAIController.getSystemPrompt().isEmpty());
        }

        @Test
        void mentionsWorkflow() {
            Assertions.assertTrue(ChartAIController.getSystemPrompt()
                    .contains("get_chart_state"));
        }
    }

    @Nested
    class ChartIdIsUniquePerInstance {

        @Test
        void differentControllers_haveDifferentChartIds() {
            var controller2 = new ChartAIController(new Chart(),
                    databaseProvider);
            var ids1 = controller.getTools().stream()
                    .filter(t -> t.getName().equals("get_chart_state"))
                    .findFirst().get().execute("{}");
            var ids2 = controller2.getTools().stream()
                    .filter(t -> t.getName().equals("get_chart_state"))
                    .findFirst().get().execute("{}");
            // The chartId in the JSON responses should differ
            Assertions.assertNotEquals(ids1, ids2);
        }
    }

    @Nested
    class ToolCallbacks {

        @Test
        void getChartState_returnsStateJson() {
            var tool = controller.getTools().stream()
                    .filter(t -> t.getName().equals("get_chart_state"))
                    .findFirst().get();

            String result = tool.execute("{}");
            Assertions.assertTrue(result.contains("chartId"));
        }

        @Test
        void updateConfiguration_setsPendingConfig() {
            var tool = controller.getTools().stream().filter(
                    t -> t.getName().equals("update_chart_configuration"))
                    .findFirst().get();

            String result = tool.execute(
                    "{\"configuration\":{\"chart\":{\"type\":\"bar\"}}}");
            Assertions.assertTrue(result.contains("updated"));

            // Verify pending config is applied by onRequestCompleted
            controller.onRequestCompleted();
            Assertions.assertNotNull(
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void updateData_validatesQueriesEagerly() {
            databaseProvider.throwOnExecute = new RuntimeException("Bad SQL");

            var tool = controller.getTools().stream()
                    .filter(t -> t.getName().equals("update_chart_data_source"))
                    .findFirst().get();

            String result = tool.execute("{\"queries\":[\"SELECT invalid\"]}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("Bad SQL"));
        }

        @Test
        void updateData_setsPendingDataUpdate() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));

            var tools = controller.getTools();
            var dataTool = tools.stream()
                    .filter(t -> t.getName().equals("update_chart_data_source"))
                    .findFirst().get();

            String result = dataTool.execute("{\"queries\":[\"SELECT 1\"]}");
            Assertions.assertTrue(result.contains("updated"));

            // Verify queries are stored via get_chart_state
            var stateTool = tools.stream()
                    .filter(t -> t.getName().equals("get_chart_state"))
                    .findFirst().get();
            String state = stateTool.execute("{}");
            Assertions.assertTrue(state.contains("SELECT 1"));
        }
    }

    @Nested
    class OnRequestCompleted {

        @Test
        void noPendingState_doesNothing() {
            controller.onRequestCompleted();
            // No exception
        }

        @Test
        void appliesPendingConfiguration() {
            var configTool = controller.getTools().stream().filter(
                    t -> t.getName().equals("update_chart_configuration"))
                    .findFirst().get();

            configTool.execute(
                    "{\"configuration\":{\"title\":{\"text\":\"Applied\"}}}");

            controller.onRequestCompleted();

            Assertions.assertEquals("Applied",
                    chart.getConfiguration().getTitle().getText());
        }
    }

    @Nested
    class SetDataConverter {

        @Test
        void customConverter_isUsedDuringRendering() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));

            AtomicBoolean converterCalled = new AtomicBoolean(false);
            controller.setDataConverter(data -> {
                converterCalled.set(true);
                DataSeries series = new DataSeries();
                series.add(new DataSeriesItem("custom", 42));
                return List.of(series);
            });

            var tools = controller.getTools();

            // Set a chart type first (required for rendering)
            tools.stream()
                    .filter(t -> t.getName()
                            .equals("update_chart_configuration"))
                    .findFirst().get().execute(
                            "{\"configuration\":{\"chart\":{\"type\":\"bar\"}}}");

            // Trigger a data update
            tools.stream()
                    .filter(t -> t.getName().equals("update_chart_data_source"))
                    .findFirst().get().execute("{\"queries\":[\"SELECT 1\"]}");

            controller.onRequestCompleted();

            Assertions.assertTrue(converterCalled.get(),
                    "Custom DataConverter should have been called");
        }
    }

    // --- Helpers ---

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
