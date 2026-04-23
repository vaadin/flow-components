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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Stacking;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

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
        void toolNamesIncludeExpected() {
            var names = controller.getTools().stream().map(t -> t.getName())
                    .toList();
            Assertions.assertTrue(names.contains("get_chart_instructions"));
            Assertions.assertTrue(names.contains("get_database_schema"));
            Assertions.assertTrue(names.contains("get_chart_state"));
            Assertions.assertTrue(names.contains("update_chart_configuration"));
            Assertions.assertTrue(names.contains("update_chart_data_source"));
        }

        @Test
        void instructionsToolIsFirst() {
            Assertions.assertEquals("get_chart_instructions",
                    controller.getTools().get(0).getName());
        }
    }

    @Nested
    class GetChartInstructions {

        @Test
        void descriptionContainsWorkflow() {
            var tool = findTool(controller.getTools(),
                    "get_chart_instructions");
            Assertions.assertTrue(
                    tool.getDescription().contains("get_chart_state"));
            Assertions.assertTrue(
                    tool.getDescription().contains("get_database_schema"));
        }

        @Test
        void executeReturnsWorkflow() {
            var result = findTool(controller.getTools(),
                    "get_chart_instructions").execute(null);
            Assertions.assertFalse(result.isEmpty());
            Assertions.assertTrue(result.contains("get_chart_state"));
        }
    }

    @Nested
    class ToolCallbacks {

        @Test
        void getChartState_excludesSeriesDataFromConfiguration() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"column\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            String state = findTool(tools, "get_chart_state")
                    .execute(json("{}"));
            Assertions.assertTrue(state.contains("\"configuration\""));
            Assertions.assertTrue(state.contains("\"series\""),
                    "State should include series configuration");
            Assertions.assertFalse(state.contains("\"data\""),
                    "State should not contain series data");
        }

        @Test
        void getOrCreate_withMismatchedChartId_throws() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            ChartEntry entry = ChartEntry.get(chart);
            Assertions.assertNotNull(entry);

            Assertions.assertThrows(IllegalStateException.class,
                    () -> ChartEntry.getOrCreate(chart, "wrong-id"));
        }

        @Test
        void updateConfiguration_validatesEagerly() {
            var tool = findTool(controller.getTools(),
                    "update_chart_configuration");

            String result = tool.execute(
                    json("{\"configuration\": \"not a json object\"}"));
            Assertions.assertTrue(result.contains("Error"),
                    "Invalid config should return error: " + result);
        }

        @Test
        void updateData_validatesQueriesEagerly() {
            databaseProvider.throwOnExecute = new RuntimeException("Bad SQL");

            var tool = controller.getTools().stream()
                    .filter(t -> t.getName().equals("update_chart_data_source"))
                    .findFirst().get();

            String result = tool
                    .execute(json("{\"queries\": [\"SELECT invalid\"]}"));
            Assertions.assertTrue(result.contains("Error"));
        }

        @Test
        void onRequestCompleted_renderFails_propagates() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            databaseProvider.throwOnExecute = new RuntimeException(
                    "Render failure");

            var ex = Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onRequestCompleted());
            Assertions.assertEquals("Render failure", ex.getMessage());
        }

        @Test
        void updateConfiguration_appliesColumnPlotOptions() {
            var tools = controller.getTools();

            // Build a configuration with column plot options
            var configuration = new Configuration();
            configuration.getChart().setType(ChartType.COLUMN);
            var plotOptions = new PlotOptionsColumn();
            plotOptions.setStacking(Stacking.NORMAL);
            plotOptions.setBorderRadius(5);
            plotOptions.setColorByPoint(true);
            plotOptions.getDataLabels().setEnabled(true);
            configuration.setPlotOptions(plotOptions);

            // Apply and render
            findTool(tools, "update_chart_configuration")
                    .execute(json("{\"configuration\":"
                            + ChartSerialization.toJSON(configuration) + "}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            // Verify plot options were applied to the chart
            var applied = (PlotOptionsColumn) chart.getConfiguration()
                    .getPlotOptions(ChartType.COLUMN);
            Assertions.assertNotNull(applied);
            Assertions.assertEquals(Stacking.NORMAL, applied.getStacking());
            Assertions.assertEquals(5, applied.getBorderRadius().intValue());
            Assertions.assertTrue(applied.getColorByPoint());
            Assertions.assertTrue(applied.getDataLabels().getEnabled());
        }

        @Test
        void updateData_setsPendingDataUpdate() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));

            var tools = controller.getTools();
            var dataTool = tools.stream()
                    .filter(t -> t.getName().equals("update_chart_data_source"))
                    .findFirst().get();

            String result = dataTool
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            Assertions.assertTrue(result.contains("updated"));

            // Verify queries are stored via get_chart_state
            var stateTool = tools.stream()
                    .filter(t -> t.getName().equals("get_chart_state"))
                    .findFirst().get();
            String state = stateTool.execute(json("{}"));
            Assertions.assertTrue(state.contains("SELECT 1"));
        }
    }

    @Nested
    class SetDataConverter {

        @Test
        void customConverter_isUsedDuringRendering() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));

            controller.setDataConverter(data -> {
                DataSeries series = new DataSeries("custom");
                series.add(new DataSeriesItem("A", 42));
                return List.of(series);
            });

            var tools = controller.getTools();

            tools.stream()
                    .filter(t -> t.getName()
                            .equals("update_chart_configuration"))
                    .findFirst().get().execute(json(
                            "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));

            tools.stream()
                    .filter(t -> t.getName().equals("update_chart_data_source"))
                    .findFirst().get()
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            controller.onRequestCompleted();

            var series = chart.getConfiguration().getSeries();
            Assertions.assertEquals(1, series.size());
            Assertions.assertEquals("custom", series.get(0).getName());
            var items = ((DataSeries) series.get(0)).getData();
            Assertions.assertEquals(1, items.size());
            Assertions.assertEquals("A", items.get(0).getName());
            Assertions.assertEquals(42, items.get(0).getY().intValue());
        }
    }

    @Nested
    class GetState {

        @Test
        void noEntry_returnsNull() {
            Assertions.assertNull(controller.getState());
        }

        @Test
        void afterRender_returnsQueriesAndConfiguration() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"column\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            ChartState state = controller.getState();
            Assertions.assertNotNull(state);
            Assertions.assertEquals(List.of("SELECT 1"), state.queries());
            Assertions.assertNotSame(chart.getConfiguration(),
                    state.configuration(),
                    "State should contain a copy, not the live configuration");
            Assertions.assertEquals(
                    chart.getConfiguration().getChart().getType(),
                    state.configuration().getChart().getType());
        }

        @Test
        void configurationIsIsolatedFromChartMutations() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"column\"}, \"title\": {\"text\": \"Original\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            ChartState savedState = controller.getState();

            chart.getConfiguration().setTitle("Mutated");

            Assertions.assertNotEquals("Mutated",
                    savedState.configuration().getTitle().getText(),
                    "State configuration should be a snapshot isolated "
                            + "from later chart mutations");
        }
    }

    @Nested
    class ChartStateSerialization {

        @Test
        void chartState_isSerializable() throws Exception {
            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);
            var state = new ChartState(List.of("SELECT 1"), config);
            var baos = new ByteArrayOutputStream();
            try (var oos = new ObjectOutputStream(baos)) {
                oos.writeObject(state);
            }
            try (var ois = new ObjectInputStream(
                    new ByteArrayInputStream(baos.toByteArray()))) {
                var deserialized = (ChartState) ois.readObject();
                Assertions.assertEquals(List.of("SELECT 1"),
                        deserialized.queries());
                Assertions.assertEquals(ChartType.COLUMN,
                        deserialized.configuration().getChart().getType());
            }
        }
    }

    @Nested
    class RestoreState {

        @Test
        void appliesConfigurationAndQueries() {
            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);
            ChartState state = new ChartState(List.of("SELECT 1"), config);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            controller.restoreState(state);

            Assertions.assertNotSame(config, chart.getConfiguration(),
                    "restoreState should copy the configuration");
            Assertions.assertEquals(ChartType.COLUMN,
                    chart.getConfiguration().getChart().getType());

            ChartEntry entry = ChartEntry.get(chart);
            Assertions.assertNotNull(entry);
            Assertions.assertEquals(List.of("SELECT 1"), entry.getQueries());
        }

        @Test
        void nullState_throws() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.restoreState(null));
        }

        @Test
        void doesNotFireListeners() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            controller
                    .restoreState(new ChartState(List.of("SELECT 1"), config));

            Assertions.assertNull(captured.get());
        }

        @Test
        void rendersChart() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);

            controller
                    .restoreState(new ChartState(List.of("SELECT 1"), config));

            Assertions.assertFalse(
                    chart.getConfiguration().getSeries().isEmpty(),
                    "restoreState should render the chart");
        }

        @Test
        void doesNotMutateInputConfiguration() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);
            ChartState state = new ChartState(List.of("SELECT 1"), config);

            Assertions.assertTrue(config.getSeries().isEmpty());

            controller.restoreState(state);

            Assertions.assertTrue(state.configuration().getSeries().isEmpty(),
                    "restoreState should not mutate the input State's "
                            + "Configuration");
        }

        @Test
        void clearsPendingState() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            // Create pending state via tool calls
            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            ChartEntry entry = ChartEntry.get(chart);
            Assertions.assertTrue(entry.hasPendingState(),
                    "Precondition: should have pending state");

            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);
            controller
                    .restoreState(new ChartState(List.of("SELECT 2"), config));

            entry = ChartEntry.get(chart);
            Assertions.assertFalse(entry.hasPendingState(),
                    "restoreState should clear pending state");
        }
    }

    @Nested
    class StateChangeListeners {

        @Test
        void firesAfterOnRequestCompleted() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            Assertions.assertNotNull(captured.get());
            Assertions.assertEquals(List.of("SELECT 1"),
                    captured.get().queries());
            Assertions.assertNotSame(chart.getConfiguration(),
                    captured.get().configuration(),
                    "Listener state should contain a copy");
            Assertions.assertEquals(
                    chart.getConfiguration().getChart().getType(),
                    captured.get().configuration().getChart().getType());
        }

        @Test
        void doesNotFireOnRenderFailure() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            databaseProvider.throwOnExecute = new RuntimeException(
                    "Render failure");
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onRequestCompleted());

            Assertions.assertNull(captured.get());
        }

        @Test
        void registration_removesListener() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            var registration = controller.addStateChangeListener(captured::set);
            registration.remove();

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            Assertions.assertNull(captured.get());
        }

        @Test
        void doesNotFireOnSecondCall() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            List<ChartState> states = new ArrayList<>();
            controller.addStateChangeListener(states::add);

            controller.onRequestCompleted();

            Assertions.assertTrue(states.isEmpty(),
                    "Second onRequestCompleted should not fire listeners "
                            + "because pending state was already cleared");
        }

        @Test
        void throwingListenerDoesNotPreventOtherListeners() {
            AtomicReference<ChartState> secondListenerState = new AtomicReference<>();

            controller.addStateChangeListener(state -> {
                throw new RuntimeException("Listener failure");
            });
            controller.addStateChangeListener(secondListenerState::set);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            Assertions.assertNotNull(secondListenerState.get(),
                    "Second listener should still fire even if the "
                            + "first listener throws an exception");
        }

        @Test
        void configOnlyUpdate_doesNotFire() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"pie\"}}}"));
            controller.onRequestCompleted();

            Assertions.assertNull(captured.get());
        }
    }

    @Nested
    class DetachedChart {

        @BeforeEach
        void detach() {
            ui.remove(chart);
        }

        @Test
        void onRequestCompleted_appliesStateAndFiresListenerImmediately() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onRequestCompleted();

            // Attachment does not gate the controller: configuration
            // lives on the server side and Flow queues any JS calls
            // until attach, so a state change is a state change even
            // when the chart is not currently visible.
            ChartEntry entry = ChartEntry.get(chart);
            Assertions.assertFalse(entry.hasPendingState());
            Assertions.assertNotNull(captured.get());
        }

        @Test
        void onRequestCompleted_renderFails_propagates() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            databaseProvider.throwOnExecute = new RuntimeException("DB error");

            // Errors propagate regardless of attach state so the
            // orchestrator can still surface them in the chat UI.
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onRequestCompleted());
        }

        @Test
        void restoreState_renderFailure_doesNotThrow() {
            databaseProvider.throwOnExecute = new RuntimeException("DB error");

            Configuration config = new Configuration();
            config.getChart().setType(ChartType.COLUMN);

            // restoreState catches render failures so a corrupted
            // persisted state does not break the caller (typically
            // view init code).
            Assertions.assertDoesNotThrow(() -> controller
                    .restoreState(new ChartState(List.of("SELECT 1"), config)));
        }
    }

    // --- Helpers ---

    private static LLMProvider.ToolSpec findTool(
            List<LLMProvider.ToolSpec> tools, String name) {
        return tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    private static JsonNode json(String json) {
        return JacksonUtils.readTree(json);
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
