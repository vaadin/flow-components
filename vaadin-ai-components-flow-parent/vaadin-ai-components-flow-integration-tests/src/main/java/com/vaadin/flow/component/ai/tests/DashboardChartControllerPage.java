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
package com.vaadin.flow.component.ai.tests;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;

/**
 * Test page for ChartAIController with Dashboard. Uses an async LLM provider
 * that streams from a background thread (like a real LLM) and invokes chart
 * tools, so the orchestrator's onResponseComplete() runs via ui.access() from
 * the background thread. This reproduces the real-world timing where the
 * dashboard widget addition and chart rendering happen across separate Push
 * updates, causing the connectedCallback/updateConfiguration microtask race.
 */
@Route("vaadin-ai/dashboard-chart-controller")
public class DashboardChartControllerPage extends Div {

    private static final DatabaseProvider DB = createDatabaseProvider();

    public DashboardChartControllerPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var dashboard = new Dashboard();
        dashboard.setId("dashboard");

        var chart = new Chart();
        chart.setId("test-chart");
        var chartController = new ChartAIController(chart, DB);

        var messageList = new MessageList();
        messageList.setId("message-list");
        var messageInput = new MessageInput();
        messageInput.setId("message-input");

        var orchestrator = AIOrchestrator
                .builder(new AsyncChartLLMProvider(), null)
                .withMessageList(messageList).withInput(messageInput)
                .withController(chartController).build();

        var renderBarChart = new NativeButton("Render Bar Chart", e -> {
            // Add fresh chart widget to dashboard, then prompt the
            // async LLM which will call chart tools from a background
            // thread and trigger onResponseComplete via ui.access().
            var widget = new DashboardWidget();
            widget.setTitle("Revenue");
            widget.setContent(chart);
            dashboard.add(widget);
            orchestrator.prompt("/render-bar");
        });
        renderBarChart.setId("render-bar-chart");

        add(renderBarChart, dashboard, messageList, messageInput);
    }

    /**
     * Async LLM provider that streams from a background thread and invokes
     * chart tools, mimicking real LLM behavior.
     */
    private static class AsyncChartLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var message = request.userMessage();
            var tools = request.explicitTools();

            if ("/render-bar".equals(message)) {
                return Flux.<String> create(sink -> {
                    new Thread(() -> {
                        executeTool(tools, "update_chart_data_source",
                                "{\"queries\":[\"SELECT * FROM sales\"]}");
                        executeTool(tools, "update_chart_configuration",
                                "{\"configuration\":{\"chart\":"
                                        + "{\"type\":\"bar\"},\"title\":"
                                        + "{\"text\":\"Monthly Revenue\"}}}");
                        sink.next("Rendered bar chart.");
                        sink.complete();
                    }).start();
                });
            }

            return Flux.just("Echo: " + message);
        }

        @Override
        public void setHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
        }

        private static void executeTool(List<ToolSpec> tools, String name,
                String args) {
            tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                    .orElseThrow().execute(JacksonUtils.readTree(args));
        }
    }

    private static DatabaseProvider createDatabaseProvider() {
        var rows = List.of(
                Map.<String, Object> of("_NAME", "January", "_Y", 45000),
                Map.<String, Object> of("_NAME", "February", "_Y", 52000),
                Map.<String, Object> of("_NAME", "March", "_Y", 48000));
        return new DatabaseProvider() {
            @Override
            public String getSchema() {
                return "sales(month VARCHAR, revenue INT)";
            }

            @Override
            public List<Map<String, Object>> executeQuery(String sql) {
                return rows;
            }
        };
    }
}
