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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import reactor.core.publisher.Flux;

/**
 * Test page for AIOrchestrator.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/orchestrator")
public class AIOrchestratorPage extends UploadDropZone {

    private static final String HISTORY_SESSION_KEY = "ai-orchestrator-history";
    private static final String ATTACHMENTS_SESSION_KEY = "ai-orchestrator-attachments";

    private AIOrchestrator orchestrator;

    // Attachment storage keyed by message ID
    private final Map<String, List<AIAttachment>> attachmentStorage = new HashMap<>();

    // Displays info about the last clicked attachment
    private final Span clickedAttachmentInfo = new Span();

    @SuppressWarnings("unchecked")
    public AIOrchestratorPage() {
        setHeightFull();

        var messageList = new MessageList();
        messageList.setId("message-list");
        messageList.setSizeFull();

        var uploadManager = new UploadManager(this);
        setUploadManager(uploadManager);

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.UPLOAD.create());
        var fileList = new UploadFileList(uploadManager);
        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        var messageInput = new MessageInput();
        messageInput.setId("message-input");
        messageInput.getStyle().set("flexGrow", "1");

        clickedAttachmentInfo.setId("clicked-attachment-info");

        // Chart section for ChartAIController integration testing
        var chart = new Chart();
        chart.setId("test-chart");
        chart.setSizeFull();
        var chartController = new ChartAIController(chart,
                createTestDatabaseProvider());

        var builder = AIOrchestrator.builder(new EchoLLMProvider(), null)
                .withMessageList(messageList).withInput(messageInput)
                .withFileReceiver(uploadManager).withController(chartController)
                .withAttachmentSubmitListener(event -> {
                    attachmentStorage.put(event.getMessageId(),
                            event.getAttachments());
                }).withAttachmentClickListener(event -> {
                    var attachments = attachmentStorage
                            .get(event.getMessageId());
                    if (attachments != null) {
                        var attachment = attachments
                                .get(event.getAttachmentIndex());
                        clickedAttachmentInfo.setText(attachment.name() + " | "
                                + attachment.mimeType());
                    }
                }).withResponseCompleteListener(event -> {
                    VaadinSession.getCurrent().setAttribute(HISTORY_SESSION_KEY,
                            orchestrator.getHistory());
                    VaadinSession.getCurrent().setAttribute(
                            ATTACHMENTS_SESSION_KEY,
                            new HashMap<>(attachmentStorage));
                });

        var savedHistory = (List<ChatMessage>) VaadinSession.getCurrent()
                .getAttribute(HISTORY_SESSION_KEY);
        if (savedHistory != null) {
            var savedAttachments = (Map<String, List<AIAttachment>>) VaadinSession
                    .getCurrent().getAttribute(ATTACHMENTS_SESSION_KEY);
            if (savedAttachments != null) {
                attachmentStorage.putAll(savedAttachments);
            }
            builder.withHistory(savedHistory,
                    savedAttachments != null ? savedAttachments
                            : Collections.emptyMap());
        }

        orchestrator = builder.build();

        var promptButton = new NativeButton("Send Hello",
                e -> orchestrator.prompt("Hello from button"));
        promptButton.setId("prompt-button");

        var renderBarChart = new NativeButton("Render Bar Chart",
                e -> orchestrator.prompt("/render-bar"));
        renderBarChart.setId("render-bar-chart");

        var renderLineChart = new NativeButton("Render Line Chart",
                e -> orchestrator.prompt("/render-line"));
        renderLineChart.setId("render-line-chart");

        var renderScatterChart = new NativeButton("Render Scatter Chart",
                e -> orchestrator.prompt("/render-scatter"));
        renderScatterChart.setId("render-scatter-chart");

        var buttonBar = new Div(promptButton, renderBarChart, renderLineChart,
                renderScatterChart);
        buttonBar.getStyle().set("display", "flex").set("gap", "4px");

        var inputLayout = new Div(uploadButton, messageInput);
        inputLayout.getStyle().set("display", "flex");
        inputLayout.setWidthFull();

        var chatLayout = new Div(messageList, fileList, inputLayout, buttonBar,
                clickedAttachmentInfo);
        chatLayout.getStyle().set("display", "flex");
        chatLayout.getStyle().set("flexDirection", "column");
        chatLayout.setSizeFull();

        var mainLayout = new HorizontalLayout(chatLayout, chart);
        mainLayout.setSizeFull();

        setContent(mainLayout);
    }

    private static DatabaseProvider createTestDatabaseProvider() {
        var barRows = List.of(
                Map.<String, Object> of("_NAME", "January", "_Y", 45000),
                Map.<String, Object> of("_NAME", "February", "_Y", 52000),
                Map.<String, Object> of("_NAME", "March", "_Y", 48000));
        var scatterRows = List.of(
                Map.<String, Object> of("_X", 10, "_Y", 20),
                Map.<String, Object> of("_X", 30, "_Y", 40),
                Map.<String, Object> of("_X", 50, "_Y", 60));
        return new DatabaseProvider() {
            @Override
            public String getSchema() {
                return "sales(month VARCHAR, revenue INT); points(x INT, y INT)";
            }

            @Override
            public List<Map<String, Object>> executeQuery(String sql) {
                if (sql.contains("points")) {
                    return scatterRows;
                }
                return barRows;
            }
        };
    }

    /**
     * A test LLM provider that echoes messages back and executes chart tool
     * commands when it receives specific prompts.
     */
    private static class EchoLLMProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var message = request.userMessage();
            var tools = request.explicitTools();

            if ("/render-bar".equals(message)) {
                executeTool(tools, "update_chart_data_source",
                        "{\"queries\":[\"SELECT * FROM sales\"]}");
                executeTool(tools, "update_chart_configuration",
                        "{\"configuration\":{\"chart\":{\"type\":\"bar\"},"
                                + "\"title\":{\"text\":\"Monthly Revenue\"}}}");
                return Flux.just("Rendered bar chart.");
            }
            if ("/render-line".equals(message)) {
                executeTool(tools, "update_chart_configuration",
                        "{\"configuration\":{\"chart\":{\"type\":\"line\"},"
                                + "\"title\":{\"text\":\"Monthly Revenue\"}}}");
                return Flux.just("Rendered line chart.");
            }
            if ("/render-scatter".equals(message)) {
                executeTool(tools, "update_chart_data_source",
                        "{\"queries\":[\"SELECT * FROM points\"]}");
                executeTool(tools, "update_chart_configuration",
                        "{\"configuration\":{\"chart\":{\"type\":\"scatter\"},"
                                + "\"title\":{\"text\":\"Points\"}}}");
                return Flux.just("Rendered scatter chart.");
            }

            var response = "Echo: " + message;
            return Flux.fromArray(response.split(" ")).map(word -> word + " ");
        }

        @Override
        public void setHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
            // No-op for testing
        }

        private static void executeTool(List<ToolSpec> tools, String name,
                String args) {
            tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                    .orElseThrow().execute(args);
        }
    }
}
