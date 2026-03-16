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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.dashboard.DashboardAIController;
import com.vaadin.flow.component.ai.dashboard.DashboardAIController.DashboardState;
import com.vaadin.flow.component.ai.dashboard.DashboardAIController.WidgetState;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Demo page for AI-powered dashboard management using DashboardAIController.
 *
 * @author Vaadin Ltd
 */
@CssImport("@vaadin/aura/aura.css")
@Route("vaadin-ai/ai-dashboard-demo")
public class AIDashboardDemoPage extends HorizontalLayout {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AIDashboardDemoPage.class);

    private static final Path STATE_FILE = Paths.get("tmp",
            "dashboard-state.json");

    public AIDashboardDemoPage() {
        setSizeFull();

        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        // Dashboard section (left)
        var dashboard = new Dashboard();
        dashboard.setSizeFull();
        dashboard.setMinimumRowHeight("300px");
        dashboard.setMaximumColumnCount(4);
        dashboard.setEditable(true);

        var dashboardController = new DashboardAIController(dashboard,
                new InMemoryDatabaseProvider());

        // Restore state from file if available
        DashboardState fileState = loadStateFromFile();
        if (fileState != null) {
            dashboardController.restoreState(fileState);
        }

        // State management buttons
        var restoreStateButton = new NativeButton("Restore Saved State");
        restoreStateButton.setEnabled(fileState != null);
        restoreStateButton.addClickListener(e -> {
            DashboardState saved = loadStateFromFile();
            if (saved != null) {
                dashboardController.restoreState(saved);
            }
        });

        var saveStateButton = new NativeButton("Save Current State");
        saveStateButton.addClickListener(e -> {
            DashboardState state = dashboardController.getState();
            if (state != null) {
                saveStateToFile(state);
                restoreStateButton.setEnabled(true);
            }
        });

        var buttonBar = new HorizontalLayout(saveStateButton,
                restoreStateButton);

        var dashboardSection = new VerticalLayout(dashboard, buttonBar);
        dashboardSection.setWidth("60%");
        dashboardSection.setPadding(false);
        dashboardSection.setFlexGrow(1, dashboard);
        dashboardSection.setFlexGrow(0, buttonBar);

        // Chat section (right)
        var messageList = new MessageList();
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();
        var chatSection = new VerticalLayout(messageList, messageInput);
        chatSection.setWidth("40%");
        chatSection.setPadding(false);
        chatSection.setFlexGrow(1, messageList);

        // Create LLM provider via OpenRouter
        var apiKey = System.getenv("OPENROUTER_API_KEY");
        var model = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .baseUrl("https://openrouter.ai/api/v1")
                .modelName("anthropic/claude-sonnet-4").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create orchestrator with controller
        AIOrchestrator.builder(provider,
                DashboardAIController.getSystemPrompt())
                .withMessageList(messageList).withInput(messageInput)
                .withController(dashboardController).build();

        add(dashboardSection, chatSection);
    }

    private static void saveStateToFile(DashboardState state) {
        try {
            Files.createDirectories(STATE_FILE.getParent());

            ArrayNode widgetsArray = JacksonUtils.getMapper().createArrayNode();
            for (WidgetState ws : state.widgets()) {
                ObjectNode widgetNode = JacksonUtils.getMapper()
                        .createObjectNode();
                widgetNode.put("widgetId", ws.widgetId());
                widgetNode.put("title", ws.title());
                widgetNode.put("type", ws.type());
                widgetNode.put("colspan", ws.colspan());
                widgetNode.put("rowspan", ws.rowspan());
                widgetNode.put("sqlQuery", ws.sqlQuery());
                widgetNode.put("configuration", ws.configuration());
                widgetsArray.add(widgetNode);
            }

            ObjectNode root = JacksonUtils.getMapper().createObjectNode();
            root.set("widgets", widgetsArray);

            Files.writeString(STATE_FILE, root.toPrettyString());
            LOGGER.info("Dashboard state saved to {}", STATE_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to save dashboard state to file", e);
        }
    }

    private static DashboardState loadStateFromFile() {
        if (!Files.exists(STATE_FILE)) {
            return null;
        }
        try {
            String json = Files.readString(STATE_FILE);
            JsonNode root = JacksonUtils.readTree(json);
            JsonNode widgetsNode = root.get("widgets");
            if (widgetsNode == null || !widgetsNode.isArray()) {
                return null;
            }

            List<WidgetState> widgets = new ArrayList<>();
            for (JsonNode node : widgetsNode) {
                widgets.add(new WidgetState(
                        getStringOrNull(node, "widgetId"),
                        getStringOrNull(node, "title"),
                        getStringOrNull(node, "type"),
                        node.has("colspan") ? node.get("colspan").asInt() : 1,
                        node.has("rowspan") ? node.get("rowspan").asInt() : 1,
                        getStringOrNull(node, "sqlQuery"),
                        getStringOrNull(node, "configuration")));
            }

            LOGGER.info("Dashboard state loaded from {}", STATE_FILE);
            return new DashboardState(widgets);
        } catch (Exception e) {
            LOGGER.error("Failed to load dashboard state from file", e);
            return null;
        }
    }

    private static String getStringOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull()
                ? node.get(field).asString()
                : null;
    }
}
