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
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Page for trying out the AI chat against OpenAI through either built-in
 * provider, the baseline for the LLM pipeline customization experiments. The
 * route parameter selects the provider: {@code vaadin-ai/chat/spring-ai} (the
 * default) or {@code vaadin-ai/chat/langchain4j}. The API key is read from the
 * {@code OPENAI_API_KEY} environment variable and the model name from
 * {@code OPENAI_MODEL}. Not covered by automated tests.
 */
@Route("vaadin-ai/chat")
public class ChatPage extends VerticalLayout
        implements HasUrlParameter<String> {

    static final String SPRING_AI = "spring-ai";
    static final String LANGCHAIN4J = "langchain4j";

    private static final String DEFAULT_MODEL_NAME = "gpt-5.4-mini";
    private static final String SYSTEM_PROMPT = "You are a helpful assistant. "
            + "Keep your answers short.";

    public ChatPage() {
        setHeightFull();
    }

    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String providerName) {
        // The same page instance serves both providers, so a navigation from
        // one to the other starts from an empty page
        removeAll();
        // Both providers stream, and the response arrives on the LLM client's
        // threads, so it needs push to reach the browser
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            add(new Span("Set the OPENAI_API_KEY environment variable and "
                    + "restart the server to use this page."));
            return;
        }
        var name = providerName == null ? SPRING_AI : providerName;
        var modelName = System.getenv("OPENAI_MODEL");
        if (modelName == null || modelName.isBlank()) {
            modelName = DEFAULT_MODEL_NAME;
        }
        LLMProvider provider;
        switch (name) {
        case SPRING_AI -> provider = createSpringAIProvider(apiKey, modelName);
        case LANGCHAIN4J ->
            provider = createLangChain4JProvider(apiKey, modelName);
        default -> {
            add(new Span("Unknown provider '" + name + "'. Use " + SPRING_AI
                    + " or " + LANGCHAIN4J + " as the route parameter."));
            return;
        }
        }

        var messageList = new MessageList();
        messageList.setId("message-list");
        messageList.setMarkdown(true);
        messageList.setSizeFull();

        var messageInput = new MessageInput();
        messageInput.setId("message-input");
        messageInput.setWidthFull();

        AIOrchestrator.builder(provider, SYSTEM_PROMPT)
                .withMessageList(messageList).withInput(messageInput).build();

        var providerInfo = new Span(
                "Provider: " + name + ", model: " + modelName);
        providerInfo.setId("provider-info");

        add(providerInfo, messageList, messageInput);
    }

    private static LLMProvider createSpringAIProvider(String apiKey,
            String modelName) {
        var options = OpenAiChatOptions.builder().apiKey(apiKey)
                .model(modelName).build();
        var chatModel = OpenAiChatModel.builder().options(options).build();
        return new SpringAILLMProvider(chatModel);
    }

    private static LLMProvider createLangChain4JProvider(String apiKey,
            String modelName) {
        var chatModel = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .modelName(modelName).build();
        return new LangChain4JLLMProvider(chatModel);
    }
}
