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

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.chart.ChartState;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Page for trying out {@link ChartAIController} with a real AI chat against
 * OpenAI, rendered with the Aura theme. The chart on the left shows whatever
 * the chat on the right asks for, built from the sample data of
 * {@link InMemoryDatabaseProvider}. The API key is read from the
 * {@code OPENAI_API_KEY} environment variable. Not covered by automated tests.
 * <p>
 * The buttons under the chart exercise {@link ChartAIController#getState()} and
 * {@link ChartAIController#restoreState(ChartState)}: save the current state
 * and restore it into the running chat. The saved queries and the configuration
 * the LLM sees after a restore are shown as text.
 * </p>
 * <p>
 * Open at {@code http://localhost:8080/vaadin-ai/chart-ai-chat}.
 * </p>
 */
@Route("vaadin-ai/chart-ai-chat")
@StyleSheet(Aura.STYLESHEET)
public class ChartAIChatPage extends HorizontalLayout {

    private static final String MODEL_NAME = "gpt-5.4-mini";

    private static final String SYSTEM_PROMPT = """
            You are a data visualization assistant. Answer the user's \
            questions about the data in the database by drawing them in \
            the chart next to the chat, and keep your text answers short.""";

    // One shared instance, so concurrent page loads can't fill the database
    // twice
    private static final DatabaseProvider DATABASE = new InMemoryDatabaseProvider();

    private final VerticalLayout chartPane = new VerticalLayout();
    private final VerticalLayout chatPane = new VerticalLayout();
    private final Span status = new Span();
    private final Pre savedStateView = new Pre();

    // Kept across page loads, like the database, so a restore can be tried in
    // a fresh page
    private static ChartState savedState;

    private transient OpenAiChatModel chatModel;
    private Chart chart;
    private ChartAIController controller;

    public ChartAIChatPage() {
        setSizeFull();
        // The response arrives on the LLM client's threads, so it needs push
        // to reach the browser
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            add(new Span("Set the OPENAI_API_KEY environment variable and "
                    + "restart the server to use this page."));
            return;
        }

        var options = OpenAiChatOptions.builder().apiKey(apiKey)
                .model(MODEL_NAME).build();
        chatModel = OpenAiChatModel.builder().options(options).build();
        startChat();

        var controls = new HorizontalLayout(
                new NativeButton("Save state", e -> saveState()),
                new NativeButton("Restore state", e -> restoreState()));
        savedStateView.setWidthFull();
        // Wrap the long query and JSON lines, or the chart pane grows to fit
        // them and squeezes the chat pane
        savedStateView.getStyle().set("max-height", "12em")
                .set("overflow", "auto").set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere");

        chartPane.setSizeFull();
        chartPane.add(controls, status, savedStateView);
        if (savedState != null) {
            status.setText("A state saved earlier is available for restore:");
            showSavedState();
        }

        chatPane.setSizeFull();

        add(chartPane, chatPane);
        expand(chartPane, chatPane);
    }

    /**
     * Creates a chart with its controller and a chat with a new orchestrator,
     * replacing the current ones in the page.
     */
    private void startChat() {
        var newChart = new Chart();
        newChart.setSizeFull();
        if (chart == null) {
            chartPane.add(newChart);
        } else {
            chartPane.replace(chart, newChart);
        }
        chart = newChart;
        chartPane.expand(chart);
        controller = new ChartAIController(chart, DATABASE);

        var messageList = new MessageList();
        messageList.setMarkdown(true);
        messageList.setWidthFull();

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        AIOrchestrator
                .builder(new SpringAILLMProvider(chatModel), SYSTEM_PROMPT)
                .withMessageList(messageList).withInput(messageInput)
                .withController(controller).build();

        chatPane.removeAll();
        chatPane.add(messageList, messageInput);
        chatPane.expand(messageList);
    }

    private void saveState() {
        var state = controller.getState();
        if (state == null) {
            status.setText(
                    "Nothing to save: the chart has no data queries yet.");
            return;
        }
        savedState = state;
        status.setText("Saved the chart state. Its queries and the "
                + "configuration the LLM sees after a restore:");
        showSavedState();
    }

    private void showSavedState() {
        savedStateView.setText(String.join("\n", savedState.queries()) + "\n\n"
                + JacksonUtils
                        .readTree(ChartSerialization
                                .toJSON(savedState.llmConfiguration()))
                        .toPrettyString());
    }

    private void restoreState() {
        if (savedState == null) {
            status.setText("Save a state first.");
            return;
        }
        controller.restoreState(savedState);
        status.setText("Restored the saved state.");
    }
}
