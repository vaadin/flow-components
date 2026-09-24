/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.example.chartredaction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.AITurnEvents;
import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class RedactingChartControllerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlag = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private Chart chart;
    private ChartAIController controller;

    @BeforeEach
    void setUp() {
        chart = new Chart();
        ui.add(chart);
        controller = new ChartAIController(chart, new SecretCustomers());
    }

    @Test
    void unwrappedController_sendsCategoriesToLlm() {
        render("column");

        Assertions.assertTrue(
                chartState(controller).contains("Secret Customer"),
                "25.3 is expected to send the categories without the wrapper");
    }

    @Test
    void wrappedController_leavesCategoriesOut() {
        render("column");

        var state = chartState(new RedactingChartController(controller));
        Assertions.assertFalse(state.contains("Secret Customer"), state);
        Assertions.assertTrue(state.contains("Revenue"), state);
        Assertions.assertTrue(state.contains("SELECT customer"), state);
        Assertions.assertArrayEquals(
                new String[] { "Secret Customer A", "Secret Customer B" },
                chart.getConfiguration().getxAxis().getCategories(),
                "the chart itself still shows the categories");
    }

    @Test
    void barChart_leavesCategoriesOut() {
        render("bar");

        var state = chartState(new RedactingChartController(controller));
        Assertions.assertFalse(state.contains("Secret Customer"), state);
    }

    @Test
    void severalXAxes_leavesCategoriesOutOfEach() {
        chart.getConfiguration().getChart().setType(ChartType.COLUMN);
        chart.getConfiguration().addxAxis(new XAxis());
        chart.getConfiguration().addxAxis(new XAxis());
        render("column");

        var unwrapped = chartState(controller);
        Assertions.assertTrue(unwrapped.contains("\"xAxis\":["),
                "several x-axes are serialized as an array: " + unwrapped);
        Assertions.assertTrue(unwrapped.contains("Secret Customer"), unwrapped);
        var state = chartState(new RedactingChartController(controller));
        Assertions.assertFalse(state.contains("Secret Customer"), state);
    }

    @Test
    void stateBeforeFirstRender_passesThrough() {
        Assertions.assertEquals(chartState(controller),
                chartState(new RedactingChartController(controller)));
    }

    @Test
    void errorMessage_passesThrough() {
        var error = "Error getting chart state: Unknown chart ID 'other'";
        Assertions.assertEquals(error,
                RedactingChartController.removeCategories(error));
    }

    @Test
    void orchestrator_givesLlmTheRedactedStateTool() {
        render("column");
        var seenByLlm = new AtomicReference<String>();
        LLMProvider llm = request -> {
            // What the LLM would get from calling get_chart_state
            var stateTool = request.explicitTools().stream()
                    .filter(tool -> tool.getName().equals("get_chart_state"))
                    .findFirst().orElseThrow();
            seenByLlm
                    .set(stateTool.execute(JsonMapper.shared().readTree("{}")));
            return Flux.just("Done");
        };
        var orchestrator = AIOrchestrator.builder(llm, "You chart data.")
                .withController(new RedactingChartController(controller))
                .build();

        orchestrator.prompt("What does the chart show?");

        Assertions.assertNotNull(seenByLlm.get(), "the LLM was not called");
        Assertions.assertFalse(seenByLlm.get().contains("Secret Customer"),
                seenByLlm.get());
        Assertions.assertTrue(seenByLlm.get().contains("Revenue"),
                seenByLlm.get());
    }

    /** Runs one successful turn that sets the chart type, title and query. */
    private void render(String chartType) {
        var tools = controller.getTools();
        tool(tools, "update_chart_configuration").execute(
                json("{\"configuration\": {\"chart\": {\"type\": \"" + chartType
                        + "\"}, \"title\": {\"text\": \"Revenue\"}}}"));
        tool(tools, "update_chart_data_source").execute(json(
                "{\"queries\": [\"SELECT customer AS category, revenue AS value FROM sales\"]}"));
        controller.onResponse(AITurnEvents.success());
    }

    private static String chartState(AIController controller) {
        return tool(controller.getTools(), "get_chart_state")
                .execute(json("{}"));
    }

    private static LLMProvider.ToolSpec tool(List<LLMProvider.ToolSpec> tools,
            String name) {
        return tools.stream().filter(tool -> tool.getName().equals(name))
                .findFirst().orElseThrow();
    }

    private static JsonNode json(String json) {
        return JsonMapper.shared().readTree(json);
    }

    private static class SecretCustomers implements DatabaseProvider {

        @Override
        public String getSchema() {
            return "sales(customer VARCHAR, revenue INT)";
        }

        @Override
        public List<Map<String, Object>> executeQuery(String sql) {
            return List.of(Map.of("category", "Secret Customer A", "value", 10),
                    Map.of("category", "Secret Customer B", "value", 20));
        }
    }
}
