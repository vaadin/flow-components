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
import java.util.Objects;

import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.RequestListener;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.ai.provider.LLMProvider;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Wraps a {@link ChartAIController} so that the chart state sent to the LLM
 * does not contain the x-axis categories.
 * <pre>
 * var controller = new ChartAIController(chart, databaseProvider);
 * AIOrchestrator.builder(llmProvider, systemPrompt)
 *         .withController(new RedactingChartController(controller))
 *         .withMessageList(messageList).withInput(messageInput).build();
 * </pre>
 * <p>
 * Written for Vaadin 25.3.
 */
public class RedactingChartController implements AIController {

    private final ChartAIController delegate;

    public RedactingChartController(ChartAIController delegate) {
        this.delegate = Objects.requireNonNull(delegate,
                "Delegate cannot be null");
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        return delegate.getTools().stream()
                .map(tool -> "get_chart_state".equals(tool.getName())
                        ? new StateTool(tool)
                        : tool)
                .toList();
    }

    @Override
    public void onRequest(RequestListener.RequestEvent event) {
        delegate.onRequest(event);
    }

    @Override
    public void onResponse(ResponseListener.ResponseEvent event) {
        delegate.onResponse(event);
    }

    static String removeCategories(String state) {
        JsonNode root;
        try {
            root = JsonMapper.shared().readTree(state);
        } catch (JacksonException e) {
            // An error message, not a chart state
            return state;
        }
        JsonNode axes = root.path("configuration").path("xAxis");
        if (axes instanceof ObjectNode axis) {
            axis.remove("categories");
        } else {
            // Several x-axes are an array
            for (JsonNode axis : axes) {
                if (axis instanceof ObjectNode axisObject) {
                    axisObject.remove("categories");
                }
            }
        }
        return root.toString();
    }

    private record StateTool(
            LLMProvider.ToolSpec tool) implements LLMProvider.ToolSpec {

        @Override
        public String getName() {
            return tool.getName();
        }

        @Override
        public String getDescription() {
            return tool.getDescription();
        }

        @Override
        public String getParametersSchema() {
            return tool.getParametersSchema();
        }

        @Override
        public String execute(JsonNode arguments) {
            return removeCategories(tool.execute(arguments));
        }
    }
}
