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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * Test page for trying out ChartAIController with a real AI chat. Supports
 * multiple LLM providers via the {@code model} query parameter.
 * <p>
 * Example: {@code http://localhost:8080/vaadin-ai/chart-ai-chat?model=gpt-5.4}
 */
@Route("vaadin-ai/chart-ai-chat")
public class ChartAIChatPage extends Div implements BeforeEnterObserver {

    private static final List<String> OPENAI_MODELS = List.of("gpt-5.4",
            "gpt-5.4-mini");

    private static final Map<String, String> OPENROUTER_MODELS = Map.ofEntries(
            Map.entry("claude-opus-4-6", "anthropic/claude-opus-4-6"),
            Map.entry("claude-sonnet-4-6", "anthropic/claude-sonnet-4-6"),
            Map.entry("claude-haiku-4-5", "anthropic/claude-haiku-4-5"),
            Map.entry("deepseek-v3.2", "deepseek/deepseek-v3.2"),
            Map.entry("minimax-m2.7", "minimax/minimax-m2.7"),
            Map.entry("gemini-3-flash-preview",
                    "google/gemini-3-flash-preview"),
            Map.entry("glm-5-turbo", "z-ai/glm-5-turbo"),
            Map.entry("grok-4.1-fast", "x-ai/grok-4.1-fast"),
            Map.entry("qwen3-8b", "qwen/qwen3-8b"));

    private static final String DEFAULT_MODEL = OPENAI_MODELS.get(1);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var queryParams = event.getLocation().getQueryParameters()
                .getParameters();

        String modelName = queryParams.getOrDefault("model", List.of()).stream()
                .findFirst().orElse(DEFAULT_MODEL);

        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        setSizeFull();
        getStyle().set("display", "flex");

        ChatModel chatModel;
        try {
            chatModel = createModel(modelName);
        } catch (IllegalArgumentException e) {
            add(new Div(e.getMessage()));
            return;
        }

        var modelLabel = new Span("Model: " + modelName);
        modelLabel.getStyle().set("padding", "8px").set("font-weight", "bold");

        var chart = new Chart();
        chart.setSizeFull();

        var chartController = new ChartAIController(chart,
                new InMemoryDatabaseProvider());

        var messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("flex", "1");

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var llmProvider = new SpringAILLMProvider(chatModel);

        AIOrchestrator.builder(llmProvider, "").withMessageList(messageList)
                .withInput(messageInput).withController(chartController)
                .build();

        var chatLayout = new Div(modelLabel, messageList, messageInput);
        chatLayout.getStyle().set("display", "flex");
        chatLayout.getStyle().set("flex-direction", "column");
        chatLayout.getStyle().set("flex", "1");
        chatLayout.getStyle().set("min-width", "0");

        var chartWrapper = new Div(chart);
        chartWrapper.getStyle().set("flex", "1");
        chartWrapper.getStyle().set("min-width", "0");

        add(chartWrapper, chatLayout);
    }

    private static ChatModel createModel(String modelName) {
        if (OPENAI_MODELS.contains(modelName)) {
            String apiKey = requireEnv("OPENAI_API_KEY");
            return buildOpenAiChatModel(apiKey, null, modelName);
        }
        if (OPENROUTER_MODELS.containsKey(modelName)) {
            String apiKey = requireEnv("OPENROUTER_API_KEY");
            return buildOpenAiChatModel(apiKey, "https://openrouter.ai/api",
                    OPENROUTER_MODELS.get(modelName));
        }
        var supported = Stream
                .concat(OPENAI_MODELS.stream(),
                        OPENROUTER_MODELS.keySet().stream())
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException(
                "Unknown model: " + modelName + ". Supported: " + supported);
    }

    private static OpenAiChatModel buildOpenAiChatModel(String apiKey,
            String baseUrl, String modelName) {
        var clientBuilder = OpenAIOkHttpClient.builder().apiKey(apiKey);
        if (baseUrl != null) {
            clientBuilder.baseUrl(baseUrl);
        }
        OpenAIClient openAiClient = clientBuilder.build();
        return OpenAiChatModel.builder().openAiClient(openAiClient)
                .options((OpenAiChatOptions) OpenAiChatOptions.builder()
                        .model(modelName).build())
                .build();
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Set the " + name
                    + " environment variable to use this model.");
        }
        return value;
    }
}
