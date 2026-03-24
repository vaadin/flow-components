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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.dashboard.DashboardAIController;
import com.vaadin.flow.component.ai.dashboard.DashboardAIController.DashboardState;
import com.vaadin.flow.component.ai.dashboard.DashboardAIController.WidgetState;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
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
@Route("vaadin-ai/ai-dashboard-demo")
public class AIDashboardDemoPage extends Div {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AIDashboardDemoPage.class);

    private static final Path STATE_FILE = Paths.get("tmp",
            "dashboard-state.json");

    public AIDashboardDemoPage() {
        setSizeFull();

        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        // Upload support
        var dropZone = new UploadDropZone();
        var uploadManager = new UploadManager(dropZone);
        dropZone.setUploadManager(uploadManager);

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.UPLOAD.create());
        var fileList = new UploadFileList(uploadManager);
        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        // Dashboard section (left)
        var dashboard = new Dashboard();
        dashboard.setSizeFull();
        dashboard.setMinimumRowHeight("300px");
        dashboard.setMaximumColumnCount(4);
        dashboard.setEditable(true);
        dashboard.setDenseLayout(true);

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
            if (dashboard.getWidgets().isEmpty()) {
                saveStateButton.getElement()
                        .executeJs("window.alert('No widgets to save')");
                return;
            }
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
        var welcomeMessage = new MessageListItem("""
                Welcome! This dashboard is backed by sales, employees, \
                and products data. I can:
                - **Create widgets** (charts, grids)
                - **Update or remove** existing widgets
                - **Resize and rearrange** the layout

                Try these prompts:
                - `Show monthly revenue as a bar chart`
                - `Add a grid of all employees sorted by salary`
                - `Show total units sold per product category`

                You can also **select widgets** on the dashboard and ask \
                me to modify or remove them.""", "Assistant");
        var messageList = new MessageList(welcomeMessage);
        messageList.setMarkdown(true);
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.getStyle().set("flexGrow", "1");

        var inputLayout = new Div(uploadButton, messageInput);
        inputLayout.getStyle().set("display", "flex");
        inputLayout.setWidthFull();

        var chatSection = new VerticalLayout(messageList, fileList,
                inputLayout);
        chatSection.setSizeFull();
        chatSection.setPadding(false);
        chatSection.setFlexGrow(1, messageList);

        dropZone.setContent(chatSection);
        dropZone.setWidth("40%");
        dropZone.setHeightFull();

        // Create LLM provider via OpenAI
        var apiKey = System.getenv("OPENAI_API_KEY");
        var model = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .modelName("gpt-5.4-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create orchestrator with controller
        var orchestrator = AIOrchestrator
                .builder(provider, DashboardAIController.getSystemPrompt())
                .withMessageList(messageList).withInput(messageInput)
                .withFileReceiver(uploadManager)
                .withController(dashboardController).build();

        // Chart prompt buttons
        var chartButtonBar = createChartPromptButtons(orchestrator);
        dashboardSection.add(chartButtonBar);
        dashboardSection.setFlexGrow(0, chartButtonBar);

        var mainLayout = new HorizontalLayout(dashboardSection, dropZone);
        mainLayout.setSizeFull();
        add(mainLayout);
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
                if (ws.queries() != null) {
                    ArrayNode queriesArray = widgetNode.putArray("queries");
                    ws.queries().forEach(queriesArray::add);
                }
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
                List<String> queries = null;
                if (node.has("queries") && !node.get("queries").isNull()) {
                    queries = new ArrayList<>();
                    for (JsonNode q : node.get("queries")) {
                        queries.add(q.asString());
                    }
                }
                widgets.add(new WidgetState(getStringOrNull(node, "widgetId"),
                        getStringOrNull(node, "title"),
                        getStringOrNull(node, "type"),
                        node.has("colspan") ? node.get("colspan").asInt() : 1,
                        node.has("rowspan") ? node.get("rowspan").asInt() : 1,
                        queries, getStringOrNull(node, "configuration")));
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

    private static HorizontalLayout createChartPromptButtons(
            AIOrchestrator orchestrator) {
        var layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle().set("flexWrap", "wrap").set("gap", "8px")
                .set("padding", "8px").set("alignItems", "center");

        var label = new Span("Add chart:");
        label.getStyle().set("fontWeight", "bold");
        layout.add(label);

        Map.of("Line",
                "Show monthly revenue broken down by region as a smooth line chart with data labels and a legend at the bottom.",

                "Column",
                "Show a stacked column chart of units sold per product, grouped by category, with data labels on each segment.",

                "Pie",
                "Show revenue share by region as a donut chart with percentages and values on each slice.",

                "Bar",
                "Show employee salaries as a horizontal bar chart sorted highest to lowest, colored by department.",

                "Area",
                "Show a stacked area chart of monthly revenue by region with smooth curves.",

                "Scatter",
                "Plot employee age vs salary as a scatter chart, color-coded by department, with each point labeled by name.",

                "Bubble",
                "Show a bubble chart of products with price on one axis, units sold on the other, and total revenue as bubble size.",

                "Heatmap",
                "Show website traffic as a heatmap with days of the week vs hours of the day, using a yellow-to-red gradient.",

                "Candlestick",
                "Show ACME stock prices as a candlestick chart with a volume bar series on a secondary axis.",

                "Gauge",
                "Show the Customer Satisfaction score as a gauge with red/yellow/green bands and the target marked."

        ).forEach((name, prompt) -> {
            var button = new NativeButton(name,
                    e -> orchestrator.prompt(prompt));
            layout.add(button);
        });

        Map.of("Sankey",
                "Show energy flows from sources to consumption sectors as a Sankey diagram.",

                "Organization",
                "Show the company org chart with names, titles, and team colors.",

                "Treemap",
                "Show the expense breakdown as a treemap grouped by top-level category.",

                "Funnel",
                "Show the sales pipeline as a funnel chart with counts and conversion percentages.",

                "Waterfall",
                "Show the budget as a waterfall chart from revenue through costs to net profit, with subtotals.",

                "Gantt",
                "Show the project schedule as a Gantt chart with tasks colored by phase and progress bars.",

                "XRange",
                "Show task durations as horizontal bars on a timeline, colored by project phase.",

                "Timeline",
                "Show project milestones on a timeline with task names and phases."

        ).forEach((name, prompt) -> {
            var button = new NativeButton(name,
                    e -> orchestrator.prompt(prompt));
            layout.add(button);
        });

        return layout;
    }
}
