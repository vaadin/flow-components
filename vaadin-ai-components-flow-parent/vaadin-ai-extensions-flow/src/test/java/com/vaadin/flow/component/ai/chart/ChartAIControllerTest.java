/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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

import com.vaadin.flow.component.ai.AITurnEvents;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.NodeSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsSpline;
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
            Assertions.assertTrue(names.contains("get_plot_options_schema"));
        }

        @Test
        void instructionsToolIsFirst() {
            Assertions.assertEquals("get_chart_instructions",
                    controller.getTools().get(0).getName());
        }

        @Test
        void plotOptionsSchemaTool_returnsGeneratedSchema() {
            var tool = findTool(controller.getTools(),
                    "get_plot_options_schema");
            var result = tool.execute(json("{\"chartType\":\"column\"}"));
            Assertions.assertFalse(result.startsWith("Error"), result);
            var schema = json(result);
            Assertions.assertEquals("object", schema.get("type").asString());
            Assertions.assertTrue(schema.get("properties").has("stacking"),
                    "Column schema should expose plot option properties");
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

        @Test
        void declaresNoParameters() {
            // A null schema tells the provider the tool takes no parameters;
            // the provider substitutes its placeholder schema in the LLM
            // request.
            Assertions.assertNull(
                    findTool(controller.getTools(), "get_chart_instructions")
                            .getParametersSchema());
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
            controller.onResponse(AITurnEvents.success());

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
            controller.onResponse(AITurnEvents.success());

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
        void updateConfiguration_invalidConfigJson_relaysParseError() {
            var tool = findTool(controller.getTools(),
                    "update_chart_configuration");

            String result = tool.execute(
                    json("{\"configuration\": \"not a json object\"}"));
            Assertions.assertTrue(
                    result.contains("Invalid chart configuration JSON"),
                    "The parse failure reason should reach the model: "
                            + result);
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
        void updateData_providerThrowsToolException_relaysMessage() {
            databaseProvider.throwOnExecute = new ToolException(
                    "Unknown column 'foo'");

            var tool = findTool(controller.getTools(),
                    "update_chart_data_source");

            String result = tool
                    .execute(json("{\"queries\": [\"SELECT foo\"]}"));
            Assertions.assertEquals(
                    "Error updating chart data: Unknown column 'foo'", result);
        }

        @Test
        void updateData_providerThrowsUnexpectedException_returnsGenericError() {
            databaseProvider.throwOnExecute = new RuntimeException(
                    "internal detail");

            var tool = findTool(controller.getTools(),
                    "update_chart_data_source");

            String result = tool
                    .execute(json("{\"queries\": [\"SELECT foo\"]}"));
            Assertions.assertTrue(result.startsWith("Error"), "Got: " + result);
            Assertions.assertFalse(result.contains("internal detail"),
                    "The cause must not reach the LLM, got: " + result);
        }

        @Test
        void onResponse_renderFails_propagates() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            databaseProvider.throwOnExecute = new RuntimeException(
                    "Render failure");

            var event = AITurnEvents.success();
            var ex = Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onResponse(event));
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
            controller.onResponse(AITurnEvents.success());

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
        void configurationUpdatesInOneTurn_allApply() {
            databaseProvider.results = List.of(
                    Map.of("_series", "North", "category", "Jan", "value", 10));
            completeTurn("{\"chart\": {\"type\": \"column\"}}");

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration")
                    .execute(json("{\"configuration\": {\"series\":"
                            + " [{\"name\": \"North\", \"type\": \"spline\"}]}}"));
            findTool(tools, "update_chart_configuration")
                    .execute(json("{\"configuration\": {\"chart\":"
                            + " {\"type\": \"column\"},"
                            + " \"title\": {\"text\": \"Revenue\"}}}"));
            controller.onResponse(AITurnEvents.success());

            var north = (DataSeries) chart.getConfiguration().getSeries()
                    .getFirst();
            Assertions.assertEquals(ChartType.SPLINE,
                    north.getPlotOptions().getChartType());
            Assertions.assertEquals("Revenue",
                    chart.getConfiguration().getTitle().getText());
            var state = chartState();
            Assertions.assertTrue(state.contains("spline"), state);
            Assertions.assertTrue(state.contains("Revenue"), state);
        }

        @Test
        void updateData_stagesQueriesAppliedOnResponseComplete() {
            databaseProvider.results = List.of(Map.of("x", 1, "y", 2));

            var tools = controller.getTools();
            String result = findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            Assertions.assertTrue(result.contains("updated"));

            // Queries are committed only after onResponseComplete; before
            // that, get_chart_state returns the previously-committed view.
            controller.onResponse(AITurnEvents.success());

            String state = findTool(tools, "get_chart_state")
                    .execute(json("{}"));
            Assertions.assertTrue(state.contains("SELECT 1"));
        }
    }

    @Nested
    class ChartStateContent {

        @Test
        void excludesCategoriesFromQueryResults() {
            databaseProvider.results = List
                    .of(Map.of("category", "Secret Customer", "value", 10));

            completeTurn("{\"chart\": {\"type\": \"bar\"},"
                    + " \"title\": {\"text\": \"Revenue\"}}");

            Assertions.assertArrayEquals(new String[] { "Secret Customer" },
                    chart.getConfiguration().getxAxis().getCategories());
            var state = chartState();
            Assertions.assertTrue(state.contains("Revenue"), state);
            Assertions.assertFalse(state.contains("Secret Customer"), state);
        }

        @Test
        void excludesSeriesNamesFromQueryResults() {
            databaseProvider.results = List.of(Map.of("_series", "Secret Rep",
                    "category", "Q1", "value", 10));

            completeTurn("{\"chart\": {\"type\": \"line\"}}");

            Assertions.assertEquals("Secret Rep",
                    chart.getConfiguration().getSeries().getFirst().getName());
            Assertions.assertFalse(chartState().contains("Secret Rep"));
        }

        @Test
        void excludesOrganizationNodesFromQueryResults() {
            databaseProvider.results = List.of(
                    Map.of("_id", "1", "_name", "Secret Boss", "_parent", "0",
                            "_title", "Secret Title"),
                    Map.of("_id", "2", "_name", "Secret Report", "_parent", "1",
                            "_title", "Secret Role"));

            completeTurn("{\"chart\": {\"type\": \"organization\"}}");

            var series = (NodeSeries) chart.getConfiguration().getSeries()
                    .getFirst();
            Assertions.assertEquals(2, series.getNodes().size());
            Assertions.assertFalse(chartState().contains("Secret"));
        }

        @Test
        void keepsConfigurationFromEarlierTurns() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"title\": {\"text\": \"First\"}}");
            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"subtitle\": {\"text\": \"Second\"}}");

            var state = chartState();
            Assertions.assertTrue(state.contains("First"), state);
            Assertions.assertTrue(state.contains("Second"), state);
        }

        @Test
        void chartTypeChange_startsOverLikeTheChart() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"title\": {\"text\": \"First\"}}");
            completeTurn("{\"chart\": {\"type\": \"bar\"}}");

            Assertions
                    .assertNull(chart.getConfiguration().getTitle().getText());
            var state = chartState();
            Assertions.assertTrue(state.contains("\"bar\""), state);
            Assertions.assertFalse(state.contains("First"), state);
        }

        @Test
        void chartTypeSetByApplication_keepsEarlierLlmConfiguration() {
            chart.getConfiguration().getChart().setType(ChartType.COLUMN);
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            completeTurn("{\"title\": {\"text\": \"Revenue\"}}");
            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"subtitle\": {\"text\": \"2025\"}}");

            Assertions.assertEquals("Revenue",
                    chart.getConfiguration().getTitle().getText());
            var state = chartState();
            Assertions.assertTrue(state.contains("Revenue"), state);
            Assertions.assertTrue(state.contains("2025"), state);
        }

        @Test
        void seriesSettingsSentTwice_keepsTheLatest() {
            databaseProvider.results = List.of(
                    Map.of("_series", "North", "category", "Jan", "value", 10));

            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"series\": [{\"name\": \"North\", \"type\": \"spline\"}]}");
            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"series\": [{\"name\": \"North\", \"type\": \"line\"}]}");

            var series = JacksonUtils.readTree(chartState())
                    .get("configuration").get("series");
            Assertions.assertEquals(1, series.size(), series.toString());
            Assertions.assertEquals("North",
                    series.get(0).get("name").asString());
            Assertions.assertEquals("line",
                    series.get(0).get("type").asString());
        }

        @Test
        void seriesEntryWithoutAxis_keepsTheAxisSetBefore() {
            databaseProvider.results = List.of(
                    Map.of("_series", "North", "category", "Jan", "value", 10));
            completeTurn("{\"chart\": {\"type\": \"column\"}, \"series\":"
                    + " [{\"name\": \"North\", \"type\": \"area\", \"yAxis\": 1}]}");
            completeTurn("{\"series\": [{\"name\": \"North\","
                    + " \"type\": \"column\"}]}");

            var north = (DataSeries) chart.getConfiguration().getSeries()
                    .getFirst();
            Assertions.assertEquals(ChartType.COLUMN,
                    north.getPlotOptions().getChartType());
            Assertions.assertEquals(1, north.getyAxis(),
                    "the axis binding was not mentioned, so it must stay");
            var state = chartState();
            Assertions.assertTrue(state.contains("\"yAxis\" : 1")
                    || state.contains("\"yAxis\":1"), state);
        }

        @Test
        void failedRender_keepsPreviousConfiguration() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"title\": {\"text\": \"Kept\"}}");

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration")
                    .execute(json("{\"configuration\": {\"chart\":"
                            + " {\"type\": \"column\"},"
                            + " \"title\": {\"text\": \"Dropped\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 2\"]}"));
            databaseProvider.throwOnExecute = new RuntimeException("DB error");
            var event = AITurnEvents.success();
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onResponse(event));

            var state = chartState();
            Assertions.assertTrue(state.contains("Kept"), state);
            Assertions.assertFalse(state.contains("Dropped"), state);
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

            controller.onResponse(AITurnEvents.success());

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
            controller.onResponse(AITurnEvents.success());

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
        void afterFailedRender_returnsNull() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            var tools = controller.getTools();
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            databaseProvider.throwOnExecute = new RuntimeException("DB error");
            var event = AITurnEvents.success();
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onResponse(event));

            // Render threw before setQueries could commit, so the chart
            // stays in its previous (uninitialized) state.
            Assertions.assertNull(controller.getState());
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
            controller.onResponse(AITurnEvents.success());

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
            Configuration llmConfig = new Configuration();
            llmConfig.setTitle("Revenue");
            var state = new ChartState(List.of("SELECT 1"), config, llmConfig);
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
                Assertions.assertEquals("Revenue",
                        deserialized.llmConfiguration().getTitle().getText());
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
        void savedState_keepsWhatTheLlmSees() {
            databaseProvider.results = List.of(Map.of("_series", "North",
                    "category", "Secret Customer", "value", 10));
            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"title\": {\"text\": \"Revenue\"}, \"series\":"
                    + " [{\"name\": \"North\", \"type\": \"spline\"}]}");
            var saved = controller.getState();

            chart = new Chart();
            ui.add(chart);
            controller = new ChartAIController(chart, databaseProvider);
            controller.restoreState(saved);

            var state = chartState();
            Assertions.assertTrue(state.contains("Revenue"), state);
            Assertions.assertTrue(state.contains("spline"), state);
            Assertions.assertFalse(state.contains("Secret"), state);
        }

        @Test
        void stateWithoutLlmConfiguration_hidesSeriesNamesAndCategoriesFromLlm() {
            databaseProvider.results = List.of(Map.of("_series", "Secret Rep",
                    "category", "Secret Customer", "value", 10));
            completeTurn("{\"chart\": {\"type\": \"column\"},"
                    + " \"title\": {\"text\": \"Revenue\"}}");
            var saved = controller.getState();

            chart = new Chart();
            ui.add(chart);
            controller = new ChartAIController(chart, databaseProvider);
            controller.restoreState(
                    new ChartState(saved.queries(), saved.configuration()));

            Assertions.assertEquals("Secret Rep",
                    chart.getConfiguration().getSeries().getFirst().getName());
            Assertions.assertArrayEquals(new String[] { "Secret Customer" },
                    chart.getConfiguration().getxAxis().getCategories());
            var state = chartState();
            Assertions.assertTrue(state.contains("Revenue"), state);
            Assertions.assertFalse(state.contains("Secret"), state);
            Assertions.assertFalse(state.contains("categories"), state);
        }

        @Test
        void perSeriesSettings_surviveRepeatedSaveAndRestore() {
            databaseProvider.results = List.of(
                    Map.of("_series", "North", "category", "Jan", "value", 10));
            completeTurn("{\"chart\": {\"type\": \"column\"}, \"series\":"
                    + " [{\"name\": \"North\", \"type\": \"spline\","
                    + " \"plotOptions\": {\"dataLabels\": {\"enabled\": true}}}]}");

            controller.restoreState(controller.getState());
            controller.restoreState(controller.getState());

            var north = (DataSeries) chart.getConfiguration().getSeries()
                    .getFirst();
            Assertions.assertEquals(ChartType.SPLINE,
                    north.getPlotOptions().getChartType());
            Assertions.assertTrue(((PlotOptionsSpline) north.getPlotOptions())
                    .getDataLabels().getEnabled());
            var state = chartState();
            Assertions.assertTrue(state.contains("dataLabels"), state);
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
        void firesAfterOnResponseSuccess() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onResponse(AITurnEvents.success());

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
            var event = AITurnEvents.success();
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onResponse(event));

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
            controller.onResponse(AITurnEvents.success());

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
            controller.onResponse(AITurnEvents.success());

            List<ChartState> states = new ArrayList<>();
            controller.addStateChangeListener(states::add);

            controller.onResponse(AITurnEvents.success());

            Assertions.assertTrue(states.isEmpty(),
                    "Second onResponseComplete should not fire listeners "
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
            controller.onResponse(AITurnEvents.success());

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
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(captured.get());
        }
    }

    @Nested
    class OnResponseFailure {

        @Test
        void failedFirstTurnDoesNotEstablishState() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            var tools = controller.getTools();

            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onResponse(
                    AITurnEvents.failure(new RuntimeException("stream error")));

            // Subsequent successful turn with no tool calls must not pick
            // up the failed turn's staged configuration or queries.
            controller.onResponse(AITurnEvents.success());

            Assertions.assertNull(controller.getState());
        }

        @Test
        void failedTurnStagedConfigurationDoesNotLeakIntoNextTurn() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onResponse(AITurnEvents.success());

            var baselineType = chart.getConfiguration().getChart().getType();

            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"pie\"}}}"));
            controller.onResponse(
                    AITurnEvents.failure(new RuntimeException("stream error")));

            // Subsequent successful turn with no tool calls — the failed
            // turn's pending chart type must not be applied.
            controller.onResponse(AITurnEvents.success());

            Assertions.assertEquals(baselineType,
                    chart.getConfiguration().getChart().getType());
        }

        @Test
        void failedTurnDoesNotLeakPendingQueriesIntoNextOnResponse() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));
            var tools = controller.getTools();

            findTool(tools, "update_chart_data_source").execute(
                    json("{\"queries\": [\"SELECT good FROM baseline\"]}"));
            controller.onResponse(AITurnEvents.success());

            findTool(tools, "update_chart_data_source").execute(
                    json("{\"queries\": [\"SELECT bad FROM half_baked\"]}"));
            controller.onResponse(
                    AITurnEvents.failure(new RuntimeException("stream error")));

            // Subsequent successful turn with no tool calls — the failed
            // turn's staged queries must not bleed through.
            controller.onResponse(AITurnEvents.success());

            var state = controller.getState();
            Assertions.assertNotNull(state);
            Assertions.assertEquals(List.of("SELECT good FROM baseline"),
                    state.queries());
        }
    }

    @Nested
    class DetachedChart {

        @BeforeEach
        void detach() {
            ui.remove(chart);
        }

        @Test
        void onResponse_appliesStateAndFiresListenerImmediately() {
            AtomicReference<ChartState> captured = new AtomicReference<>();
            controller.addStateChangeListener(captured::set);

            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_configuration").execute(json(
                    "{\"configuration\": {\"chart\": {\"type\": \"bar\"}}}"));
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));
            controller.onResponse(AITurnEvents.success());

            // Attachment does not gate the controller: configuration
            // lives on the server side and Flow queues any JS calls
            // until attach, so a state change is a state change even
            // when the chart is not currently visible.
            ChartEntry entry = ChartEntry.get(chart);
            Assertions.assertFalse(entry.hasPendingState());
            Assertions.assertNotNull(captured.get());
        }

        @Test
        void onResponse_renderFails_propagates() {
            databaseProvider.results = List
                    .of(Map.of("category", "A", "value", 10));

            var tools = controller.getTools();
            findTool(tools, "update_chart_data_source")
                    .execute(json("{\"queries\": [\"SELECT 1\"]}"));

            databaseProvider.throwOnExecute = new RuntimeException("DB error");

            // Errors propagate regardless of attach state so the
            // orchestrator can still surface them in the chat UI.
            var event = AITurnEvents.success();
            Assertions.assertThrows(RuntimeException.class,
                    () -> controller.onResponse(event));
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

    /** Runs one successful turn that sets the configuration and a query. */
    private void completeTurn(String configuration) {
        var tools = controller.getTools();
        findTool(tools, "update_chart_configuration")
                .execute(json("{\"configuration\": " + configuration + "}"));
        findTool(tools, "update_chart_data_source")
                .execute(json("{\"queries\": [\"SELECT 1\"]}"));
        controller.onResponse(AITurnEvents.success());
    }

    private String chartState() {
        return findTool(controller.getTools(), "get_chart_state")
                .execute(json("{}"));
    }

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
