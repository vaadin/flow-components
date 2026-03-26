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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.tests.MockUIExtension;

/**
 * Tests chart entry state management behavior through the
 * {@link ChartAIController} public API. Covers state creation, pending state
 * tracking, query storage, and state serialization.
 */
class ChartStateTest {

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

    private String getChartState() {
        return findTool("get_chart_state").execute("{}");
    }

    private void updateConfiguration(String configJson) {
        findTool("update_chart_configuration")
                .execute("{\"configuration\":" + configJson + "}");
    }

    private void updateData(String... queries) {
        StringBuilder sb = new StringBuilder("{\"queries\":[");
        for (int i = 0; i < queries.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(queries[i]).append("\"");
        }
        sb.append("]}");
        findTool("update_chart_data_source").execute(sb.toString());
    }

    @Nested
    class GetAndGetOrCreate {

        @Test
        void get_returnsNullWhenNoEntry() {
            // Before any tool call, get_chart_state returns only chartId
            String state = getChartState();
            Assertions.assertTrue(state.contains("\"chartId\""));
            Assertions.assertFalse(state.contains("\"configuration\""));
            Assertions.assertFalse(state.contains("\"queries\""));
        }

        @Test
        void getOrCreate_createsNewEntry() {
            // After a tool call, state is created and applied to the chart
            updateConfiguration("{\"chart\":{\"type\":\"bar\"}}");
            controller.onRequestCompleted();

            // Verify state was created by observing its effect on the chart
            Assertions.assertNotNull(
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void getOrCreate_returnsSameEntry() {
            // Multiple tool calls operate on the same chart state
            updateConfiguration("{\"chart\":{\"type\":\"bar\"}}");
            controller.onRequestCompleted();

            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));
            updateData("SELECT x, y FROM t");
            controller.onRequestCompleted();

            // State should have both configuration and queries from the
            // combined tool calls
            String state = getChartState();
            Assertions.assertTrue(state.contains("\"configuration\""));
            Assertions.assertTrue(state.contains("\"queries\""));
        }

        @Test
        void get_returnsEntryAfterGetOrCreate() {
            // After tool calls, state is consistently retrievable
            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            controller.onRequestCompleted();

            String state1 = getChartState();
            String state2 = getChartState();

            // Both calls should return the same chart state
            Assertions.assertEquals(state1, state2);
        }

        @Test
        void constructor_nullChart_throws() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new ChartAIController(null, databaseProvider));
        }
    }

    @Nested
    class PendingState {

        @Test
        void noPendingStateByDefault() {
            // onRequestCompleted on fresh controller is no-op
            controller.onRequestCompleted();
            // Chart configuration should remain in default state
            Assertions
                    .assertTrue(chart.getConfiguration().getSeries().isEmpty());
        }

        @Test
        void pendingConfigurationJson_makesPending() {
            updateConfiguration("{\"chart\":{\"type\":\"bar\"}}");
            // Pending state is applied by onRequestCompleted
            controller.onRequestCompleted();
            Assertions.assertNotNull(
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void pendingDataUpdate_makesPending() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT category, value FROM t");
            // Pending state is applied by onRequestCompleted
            controller.onRequestCompleted();
            Assertions.assertFalse(
                    chart.getConfiguration().getSeries().isEmpty());
        }

        @Test
        void clearPendingState_resetsBoth() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            updateConfiguration("{\"chart\":{\"type\":\"bar\"}}");
            updateData("SELECT category, value FROM t");
            controller.onRequestCompleted();

            int seriesCount = chart.getConfiguration().getSeries().size();

            // After onRequestCompleted, calling it again should be no-op
            // (pending state was cleared)
            controller.onRequestCompleted();
            Assertions.assertEquals(seriesCount,
                    chart.getConfiguration().getSeries().size());
        }
    }

    @Nested
    class Queries {

        @Test
        void emptyByDefault() {
            // get_chart_state on fresh controller has no queries
            String state = getChartState();
            Assertions.assertFalse(state.contains("\"queries\""));
        }

        @Test
        void setQueries_returnsInState() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));
            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT 1", "SELECT 2");
            controller.onRequestCompleted();

            String state = getChartState();
            Assertions.assertTrue(state.contains("\"queries\""));
            Assertions.assertTrue(state.contains("SELECT 1"));
            Assertions.assertTrue(state.contains("SELECT 2"));
        }
    }

    @Nested
    class GetStateAsJson {

        @Test
        void noEntry_returnsChartIdOnly() {
            String state = getChartState();
            Assertions.assertTrue(state.contains("\"chartId\""));
            // Should not contain configuration or queries
            Assertions.assertFalse(state.contains("\"configuration\""));
            Assertions.assertFalse(state.contains("\"queries\""));
        }

        @Test
        void entryWithNoQueries_returnsChartIdOnly() {
            // Config-only update does not set queries
            updateConfiguration("{\"chart\":{\"type\":\"bar\"}}");
            controller.onRequestCompleted();

            String state = getChartState();
            Assertions.assertTrue(state.contains("\"chartId\""));
            // Configuration is present (chart has been configured)
            // but queries should not be present
            Assertions.assertFalse(state.contains("\"queries\""));
        }

        @Test
        void entryWithQueries_returnsConfigurationAndQueries() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));
            updateConfiguration("{\"chart\":{\"type\":\"line\"}}");
            updateData("SELECT x FROM t");
            controller.onRequestCompleted();

            String state = getChartState();
            Assertions.assertTrue(state.contains("\"chartId\""));
            Assertions.assertTrue(state.contains("\"configuration\""));
            Assertions.assertTrue(state.contains("\"queries\""));
            Assertions.assertTrue(state.contains("SELECT x FROM t"));
        }
    }

    @Nested
    class GetState {

        @Test
        void noEntry_returnsNull() {
            // No tool calls: get_chart_state returns only chartId
            String state = getChartState();
            Assertions.assertFalse(state.contains("\"configuration\""));
        }

        @Test
        void entryWithNoQueries_returnsNull() {
            // Config-only update — state has no queries, so getState
            // returns null (reflected as no configuration in tool output)
            updateConfiguration("{\"chart\":{\"type\":\"bar\"}}");
            controller.onRequestCompleted();

            String state = getChartState();
            // getState returns null when no queries, so getStateAsJson
            // does not include configuration
            Assertions.assertFalse(state.contains("\"queries\""));
        }

        @Test
        void entryWithQueries_returnsState() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            updateConfiguration("{\"chart\":{\"type\":\"column\"}}");
            updateData("SELECT 1");
            controller.onRequestCompleted();

            String state = getChartState();
            Assertions.assertTrue(state.contains("\"queries\""));
            Assertions.assertTrue(state.contains("\"configuration\""));
            // Configuration should not contain series
            // (getState excludes series from the persisted configuration)
            Assertions.assertFalse(state.contains("\"series\""),
                    "State configuration should not contain series data");
        }
    }

    // --- Helpers ---

    private static class TestDatabaseProvider implements DatabaseProvider {

        List<Map<String, Object>> results = new ArrayList<>();

        @Override
        public String getSchema() {
            return "test schema";
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            return results;
        }
    }
}
