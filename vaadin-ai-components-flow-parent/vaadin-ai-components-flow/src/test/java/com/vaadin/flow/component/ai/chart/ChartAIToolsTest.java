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
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.LLMProvider;

class ChartAIToolsTest {

    private TestCallbacks callbacks;

    @BeforeEach
    void setUp() {
        callbacks = new TestCallbacks();
    }

    @Test
    void createAll_returnsThreeTools() {
        var tools = ChartAITools.createAll(callbacks);
        Assertions.assertEquals(3, tools.size());
    }

    @Test
    void createAll_nullCallbacks_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> ChartAITools.createAll(null));
    }

    @Test
    void createAll_containsExpectedToolNames() {
        var tools = ChartAITools.createAll(callbacks);
        var names = tools.stream().map(LLMProvider.ToolSpec::getName).toList();
        Assertions.assertTrue(names.contains("get_chart_state"));
        Assertions.assertTrue(names.contains("update_chart_configuration"));
        Assertions.assertTrue(names.contains("update_chart_data_source"));
    }

    @Nested
    class GetChartState {

        private LLMProvider.ToolSpec tool;

        @BeforeEach
        void setUp() {
            tool = ChartAITools.getChartState(callbacks);
        }

        @Test
        void name() {
            Assertions.assertEquals("get_chart_state", tool.getName());
        }

        @Test
        void description_isNotEmpty() {
            Assertions.assertFalse(tool.getDescription().isEmpty());
        }

        @Test
        void parametersSchema_isValidJson() {
            Assertions.assertTrue(tool.getParametersSchema()
                    .contains("\"type\": \"object\""));
        }

        @Test
        void execute_withExplicitChartId_returnsState() {
            callbacks.stateToReturn = "{\"chart\":{\"type\":\"line\"}}";
            var result = tool.execute("{\"chartId\": \"chart-1\"}");
            Assertions.assertEquals("{\"chart\":{\"type\":\"line\"}}", result);
            Assertions.assertEquals("chart-1", callbacks.lastGetStateId);
        }

        @Test
        void execute_withSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("only-chart");
            callbacks.stateToReturn = "state";
            var result = tool.execute("{}");
            Assertions.assertEquals("state", result);
            Assertions.assertEquals("only-chart", callbacks.lastGetStateId);
        }

        @Test
        void execute_withMultipleCharts_noChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool.execute("{}");
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_withNoCharts_returnsError() {
            callbacks.chartIds = Set.of();
            var result = tool.execute("{}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("No charts available"));
        }

        @Test
        void execute_withNullChartId_andSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("only-chart");
            callbacks.stateToReturn = "state";
            var result = tool.execute("{\"chartId\": null}");
            Assertions.assertEquals("state", result);
            Assertions.assertEquals("only-chart", callbacks.lastGetStateId);
        }

        @Test
        void execute_whenCallbackThrows_returnsError() {
            callbacks.getStateException = new RuntimeException(
                    "Chart not found");
            var result = tool.execute("{\"chartId\": \"chart-1\"}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("Chart not found"));
        }

        @Test
        void execute_withMalformedJson_returnsError() {
            var result = tool.execute("not json at all");
            Assertions.assertTrue(result.contains("Error"));
        }
    }

    @Nested
    class UpdateChartConfiguration {

        private LLMProvider.ToolSpec tool;

        @BeforeEach
        void setUp() {
            tool = ChartAITools.updateChartConfiguration(callbacks);
        }

        @Test
        void name() {
            Assertions.assertEquals("update_chart_configuration",
                    tool.getName());
        }

        @Test
        void description_mentionsChartType() {
            Assertions.assertTrue(tool.getDescription().contains("chart type"));
        }

        @Test
        void parametersSchema_requiresConfiguration() {
            Assertions.assertTrue(tool.getParametersSchema()
                    .contains("\"required\": [\"configuration\"]"));
        }

        @Test
        void execute_withExplicitChartId_updatesConfiguration() {
            var result = tool.execute(
                    "{\"chartId\": \"chart-1\", \"configuration\": {\"chart\": {\"type\": \"bar\"}}}");

            Assertions.assertEquals("chart-1", callbacks.lastUpdateConfigId);
            Assertions.assertEquals("{\"chart\":{\"type\":\"bar\"}}",
                    callbacks.lastUpdateConfigJson);
            Assertions.assertTrue(result.contains("chart-1"));
            Assertions.assertTrue(result.contains("updated"));
        }

        @Test
        void execute_withSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("my-chart");
            tool.execute("{\"configuration\": {\"title\": \"Test\"}}");

            Assertions.assertEquals("my-chart", callbacks.lastUpdateConfigId);
        }

        @Test
        void execute_withMultipleCharts_noChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool
                    .execute("{\"configuration\": {\"title\": \"Test\"}}");
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_withMissingConfiguration_returnsError() {
            var result = tool.execute("{\"chartId\": \"chart-1\"}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(
                    result.contains("'configuration' parameter is required"));
        }

        @Test
        void execute_whenCallbackThrows_returnsError() {
            callbacks.updateConfigException = new RuntimeException(
                    "Config rejected");
            var result = tool.execute(
                    "{\"chartId\": \"chart-1\", \"configuration\": {\"chart\": {\"type\": \"bar\"}}}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("Config rejected"));
        }

        @Test
        void execute_withMalformedJson_returnsError() {
            var result = tool.execute("not json at all");
            Assertions.assertTrue(result.contains("Error"));
        }
    }

    @Nested
    class UpdateChartDataSource {

        private LLMProvider.ToolSpec tool;

        @BeforeEach
        void setUp() {
            tool = ChartAITools.updateChartDataSource(callbacks);
        }

        @Test
        void name() {
            Assertions.assertEquals("update_chart_data_source", tool.getName());
        }

        @Test
        void description_containsColumnNameConstants() {
            var description = tool.getDescription();
            Assertions.assertTrue(description.contains(ColumnNames.PREFIX));
            Assertions.assertTrue(description.contains(ColumnNames.X));
            Assertions.assertTrue(description.contains(ColumnNames.Y));
            Assertions.assertTrue(description.contains(ColumnNames.SERIES));
        }

        @Test
        void parametersSchema_requiresQueries() {
            Assertions.assertTrue(tool.getParametersSchema()
                    .contains("\"required\": [\"queries\"]"));
        }

        @Test
        void execute_withExplicitChartId_updatesData() {
            var result = tool.execute(
                    "{\"chartId\": \"chart-1\", \"queries\": [\"SELECT * FROM t1\", \"SELECT * FROM t2\"]}");

            Assertions.assertEquals("chart-1", callbacks.lastUpdateDataId);
            Assertions.assertEquals(
                    List.of("SELECT * FROM t1", "SELECT * FROM t2"),
                    callbacks.lastUpdateDataQueries);
            Assertions.assertTrue(result.contains("chart-1"));
            Assertions.assertTrue(result.contains("updated"));
        }

        @Test
        void execute_withSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("my-chart");
            tool.execute("{\"queries\": [\"SELECT 1\"]}");

            Assertions.assertEquals("my-chart", callbacks.lastUpdateDataId);
            Assertions.assertEquals(List.of("SELECT 1"),
                    callbacks.lastUpdateDataQueries);
        }

        @Test
        void execute_withMultipleCharts_noChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool.execute("{\"queries\": [\"SELECT 1\"]}");
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_whenCallbackThrows_returnsError() {
            callbacks.updateDataException = new RuntimeException(
                    "Invalid query");
            var result = tool.execute(
                    "{\"chartId\": \"chart-1\", \"queries\": [\"INVALID\"]}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("Invalid query"));
        }

        @Test
        void execute_withEmptyQueries_updatesData() {
            tool.execute("{\"chartId\": \"chart-1\", \"queries\": []}");
            Assertions.assertEquals(List.of(), callbacks.lastUpdateDataQueries);
        }

        @Test
        void execute_withMissingQueries_returnsError() {
            var result = tool.execute("{\"chartId\": \"chart-1\"}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(
                    result.contains("'queries' parameter is required"));
        }

        @Test
        void execute_withNonArrayQueries_returnsError() {
            var result = tool.execute(
                    "{\"chartId\": \"chart-1\", \"queries\": \"SELECT 1\"}");
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("must be an array"));
        }

        @Test
        void execute_withMalformedJson_returnsError() {
            var result = tool.execute("not json at all");
            Assertions.assertTrue(result.contains("Error"));
        }
    }

    /**
     * Test implementation of {@link ChartAITools.Callbacks}.
     */
    private static class TestCallbacks implements ChartAITools.Callbacks {

        Set<String> chartIds = Set.of("chart-1");
        String stateToReturn = "{}";
        RuntimeException getStateException;
        RuntimeException updateConfigException;
        RuntimeException updateDataException;

        String lastGetStateId;
        String lastUpdateConfigId;
        String lastUpdateConfigJson;
        String lastUpdateDataId;
        List<String> lastUpdateDataQueries;

        @Override
        public String getState(String chartId) {
            if (getStateException != null) {
                throw getStateException;
            }
            lastGetStateId = chartId;
            return stateToReturn;
        }

        @Override
        public void updateConfiguration(String chartId, String configJson) {
            if (updateConfigException != null) {
                throw updateConfigException;
            }
            lastUpdateConfigId = chartId;
            lastUpdateConfigJson = configJson;
        }

        @Override
        public void updateData(String chartId, List<String> queries) {
            if (updateDataException != null) {
                throw updateDataException;
            }
            lastUpdateDataId = chartId;
            lastUpdateDataQueries = new ArrayList<>(queries);
        }

        @Override
        public Set<String> getChartIds() {
            return chartIds;
        }
    }
}
