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
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

class ChartAIToolsTest {

    private TestCallbacks callbacks;

    @BeforeEach
    void setUp() {
        callbacks = new TestCallbacks();
    }

    @Test
    void createAll_returnsFourTools() {
        var tools = ChartAITools.createAll(callbacks);
        Assertions.assertEquals(4, tools.size());
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
        Assertions.assertTrue(names.contains("get_plot_options_schema"));
    }

    private static JsonNode json(String json) {
        return JacksonUtils.readTree(json);
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
            var result = tool.execute(json("{\"chartId\": \"chart-1\"}"));
            Assertions.assertEquals("{\"chart\":{\"type\":\"line\"}}", result);
            Assertions.assertEquals("chart-1", callbacks.lastGetStateId);
        }

        @Test
        void execute_withSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("only-chart");
            callbacks.stateToReturn = "state";
            var result = tool.execute(json("{}"));
            Assertions.assertEquals("state", result);
            Assertions.assertEquals("only-chart", callbacks.lastGetStateId);
        }

        @Test
        void execute_withMultipleCharts_andExplicitChartId_resolvesCorrectChart() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            callbacks.stateToReturn = "state-of-chart-2";
            var result = tool.execute(json("{\"chartId\": \"chart-2\"}"));
            Assertions.assertEquals("state-of-chart-2", result);
            Assertions.assertEquals("chart-2", callbacks.lastGetStateId);
        }

        @Test
        void execute_withMultipleCharts_andUnrecognizedChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool.execute(json("{\"chartId\": \"bogus\"}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("chartId is required"));
        }

        @Test
        void execute_withMultipleCharts_noChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool.execute(json("{}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_withNoCharts_returnsError() {
            callbacks.chartIds = Set.of();
            var result = tool.execute(json("{}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("No charts available"));
        }

        @Test
        void execute_withNullChartId_andSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("only-chart");
            callbacks.stateToReturn = "state";
            var result = tool.execute(json("{\"chartId\": null}"));
            Assertions.assertEquals("state", result);
            Assertions.assertEquals("only-chart", callbacks.lastGetStateId);
        }

        @Test
        void execute_withUnrecognizedChartId_andSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("chart");
            callbacks.stateToReturn = "state";
            var result = tool.execute(json("{\"chartId\": \"1\"}"));
            Assertions.assertEquals("state", result);
            Assertions.assertEquals("chart", callbacks.lastGetStateId);
        }

        @Test
        void execute_whenCallbackThrows_returnsError() {
            callbacks.getStateException = new RuntimeException(
                    "Chart not found");
            var result = tool.execute(json("{\"chartId\": \"chart-1\"}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_whenCallbackThrows_doesNotLeakExceptionMessage() {
            // Exception messages can carry sensitive internal detail (SQL
            // fragments, schema names, file paths, credentials). The tool
            // result is fed to the LLM, which a user can prompt to repeat
            // it verbatim — so raw exception messages must not be included.
            callbacks.getStateException = new RuntimeException(
                    "SENSITIVE_INTERNAL_DETAIL_XYZ");
            var result = tool.execute(json("{\"chartId\": \"chart-1\"}"));
            Assertions.assertFalse(
                    result.contains("SENSITIVE_INTERNAL_DETAIL_XYZ"),
                    "Tool result leaks exception message to LLM: " + result);
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
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));

            Assertions.assertEquals("chart-1", callbacks.lastUpdateConfigId);
            Assertions.assertEquals("{\"chart\":{\"type\":\"bar\"}}",
                    callbacks.lastUpdateConfigJson);
            Assertions.assertTrue(result.contains("chart-1"));
            Assertions.assertTrue(result.contains("updated"));
        }

        @Test
        void execute_withSingleChart_defaultsToThatChart() {
            callbacks.chartIds = Set.of("my-chart");
            tool.execute(json("{\"configuration\": {\"title\": \"Test\"}}"));

            Assertions.assertEquals("my-chart", callbacks.lastUpdateConfigId);
        }

        @Test
        void execute_withMultipleCharts_noChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool.execute(
                    json("{\"configuration\": {\"title\": \"Test\"}}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_withMissingConfiguration_returnsError() {
            var result = tool.execute(json("{\"chartId\": \"chart-1\"}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(
                    result.contains("'configuration' parameter is required"));
        }

        @Test
        void execute_whenCallbackThrows_returnsError() {
            callbacks.updateConfigException = new RuntimeException(
                    "Config rejected");
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_whenCallbackThrows_doesNotLeakExceptionMessage() {
            callbacks.updateConfigException = new RuntimeException(
                    "SENSITIVE_INTERNAL_DETAIL_XYZ");
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            Assertions.assertFalse(
                    result.contains("SENSITIVE_INTERNAL_DETAIL_XYZ"),
                    "Tool result leaks exception message to LLM: " + result);
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
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"queries\": [\"SELECT * FROM t1\", \"SELECT * FROM t2\"]}"));

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
            tool.execute(json("{\"queries\": [\"SELECT 1\"]}"));

            Assertions.assertEquals("my-chart", callbacks.lastUpdateDataId);
            Assertions.assertEquals(List.of("SELECT 1"),
                    callbacks.lastUpdateDataQueries);
        }

        @Test
        void execute_withMultipleCharts_noChartId_returnsError() {
            callbacks.chartIds = Set.of("chart-1", "chart-2");
            var result = tool.execute(json("{\"queries\": [\"SELECT 1\"]}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_whenCallbackThrows_returnsError() {
            callbacks.updateDataException = new RuntimeException(
                    "Invalid query");
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"queries\": [\"SELECT invalid\"]}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void execute_whenCallbackThrows_doesNotLeakExceptionMessage() {
            callbacks.updateDataException = new RuntimeException(
                    "SENSITIVE_INTERNAL_DETAIL_XYZ");
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"queries\": [\"SELECT invalid\"]}"));
            Assertions.assertFalse(
                    result.contains("SENSITIVE_INTERNAL_DETAIL_XYZ"),
                    "Tool result leaks exception message to LLM: " + result);
        }

        @Test
        void execute_withEmptyQueries_updatesData() {
            tool.execute(json("{\"chartId\": \"chart-1\", \"queries\": []}"));
            Assertions.assertEquals(List.of(), callbacks.lastUpdateDataQueries);
        }

        @Test
        void execute_withMissingQueries_returnsError() {
            var result = tool.execute(json("{\"chartId\": \"chart-1\"}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(
                    result.contains("'queries' parameter is required"));
        }

        @Test
        void execute_withNonArrayQueries_returnsError() {
            var result = tool.execute(json(
                    "{\"chartId\": \"chart-1\", \"queries\": \"SELECT 1\"}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("must be an array"));
        }

        @Test
        void execute_withNullQueryElement_returnsError() {
            var result = tool.execute(
                    json("{\"chartId\": \"chart-1\", \"queries\": [null]}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(
                    result.contains("must not contain null elements"));
        }

        @Test
        void execute_withEmptyQueryString_returnsError() {
            var result = tool.execute(
                    json("{\"chartId\": \"chart-1\", \"queries\": [\"\"]}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(
                    result.contains("must not contain empty strings"));
        }

    }

    @Nested
    class GetPlotOptionsSchema {

        private LLMProvider.ToolSpec tool;

        @BeforeEach
        void setUp() {
            tool = ChartAITools.getPlotOptionsSchema();
        }

        @Test
        void name() {
            Assertions.assertEquals("get_plot_options_schema", tool.getName());
        }

        @Test
        void unknownType_returnsError() {
            String result = tool
                    .execute(json("{\"chartType\":\"nonexistent\"}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("unknown chart type"));
            Assertions.assertTrue(result.contains("Supported types:"),
                    "Error should list supported types");
            Assertions.assertTrue(result.contains("column"),
                    "Supported types should include 'column'");
        }

        @Test
        void missingParameter_returnsError() {
            String result = tool.execute(json("{}"));
            Assertions.assertTrue(result.contains("Error"));
            Assertions.assertTrue(result.contains("chartType"));
        }

        @Test
        void caseInsensitive() {
            String result = tool.execute(json("{\"chartType\":\"COLUMN\"}"));
            Assertions.assertFalse(result.contains("Error"), result);
            Assertions.assertTrue(result.contains("\"properties\""));
        }

        @Test
        void execute_whenExceptionThrown_doesNotLeakExceptionMessage() {
            // Calling asString() on a container node throws
            // JsonNodeException with a message that embeds the raw
            // node value — exactly the kind of detail the catch-all
            // handler must not pass through to the LLM.
            var result = tool.execute(json("{\"chartType\":[1,2,3]}"));
            Assertions.assertFalse(result.contains("coerce"),
                    "Tool result leaks exception message to LLM: " + result);
            Assertions.assertFalse(result.contains("ArrayNode"),
                    "Tool result leaks exception message to LLM: " + result);
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
