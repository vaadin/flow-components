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

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.annotation.ToolParam;

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

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.Content;

/**
 * Page for trying out the AI chat against OpenAI through either built-in
 * provider, with the LLM pipeline customized through the provider builders: the
 * system prompt, a retriever that injects a fixed fact into every turn, an
 * input guardrail that refuses a message mentioning a blocked word, and a tool
 * method of this page that carries both vendors' tool annotations. The route
 * parameter selects the provider: {@code vaadin-ai/chat/spring-ai} (the
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
    private static final String FACT = "The internal codename of the ACME "
            + "support assistant project is Bluefin.";
    private static final String BLOCKED_WORD = "password";
    private static final String GUARDRAIL_REPLY = "I cannot help with that.";
    private static final String ORDER_TOOL_DESCRIPTION = "Returns the "
            + "delivery status of a customer order";

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

        AIOrchestrator.builder(provider).withMessageList(messageList)
                .withInput(messageInput).build();

        var providerInfo = new Span("Provider: " + name + ", model: "
                + modelName + ". Pipeline: fixed-fact retriever (ask for the "
                + "project codename), an input guardrail (mention '"
                + BLOCKED_WORD + "') and an order status tool (ask about "
                + "order 1234).");
        providerInfo.setId("provider-info");

        add(providerInfo, messageList, messageInput);
    }

    private LLMProvider createSpringAIProvider(String apiKey,
            String modelName) {
        var options = OpenAiChatOptions.builder().apiKey(apiKey)
                .model(modelName).build();
        var chatModel = OpenAiChatModel.builder().options(options).build();
        return SpringAILLMProvider.builder(chatModel).customize(client -> client
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new FixedFactAdvisor(FACT),
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of(BLOCKED_WORD))
                                .failureResponse(GUARDRAIL_REPLY).build())
                .defaultTools(this)).build();
    }

    private LLMProvider createLangChain4JProvider(String apiKey,
            String modelName) {
        var chatModel = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .modelName(modelName).build();
        return LangChain4JLLMProvider.builder(chatModel)
                .customize(service -> service.systemMessage(SYSTEM_PROMPT)
                        .contentRetriever(query -> List.of(Content.from(FACT)))
                        .inputGuardrails(new BlockedWordGuardrail(BLOCKED_WORD))
                        .tools(this))
                .build();
    }

    /**
     * A tool the model may call, registered on both pipelines through the
     * vendor API of each: the method carries the LangChain4j and the Spring AI
     * tool annotations, which replace the orchestrator's deprecated
     * {@code withTools(...)}.
     *
     * @param orderNumber
     *            the order number the user asked about
     * @return the delivery status of the order
     */
    @dev.langchain4j.agent.tool.Tool(ORDER_TOOL_DESCRIPTION)
    @org.springframework.ai.tool.annotation.Tool(description = ORDER_TOOL_DESCRIPTION)
    public String getOrderStatus(
            @P("The order number") @ToolParam(description = "The order number") String orderNumber) {
        return "Order " + orderNumber
                + " was shipped yesterday and arrives tomorrow.";
    }
    /**
     * A Spring AI advisor that adds a fixed fact to every user message,
     * standing in for a retrieval advisor backed by a vector store.
     */
    private static final class FixedFactAdvisor implements BaseAdvisor {
        private final String fact;

        FixedFactAdvisor(String fact) {
            this.fact = fact;
        }

        @Override
        public ChatClientRequest before(ChatClientRequest request,
                AdvisorChain chain) {
            var augmented = request.prompt().augmentUserMessage(
                    request.prompt().getUserMessage().getText()
                            + "\n\nContext: " + fact);
            return request.mutate().prompt(augmented).build();
        }

        @Override
        public ChatClientResponse after(ChatClientResponse response,
                AdvisorChain chain) {
            return response;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    /**
     * A LangChain4j input guardrail that refuses a user message mentioning the
     * blocked word. The refusal ends the turn with an error before the model is
     * called.
     */
    private static final class BlockedWordGuardrail implements InputGuardrail {
        private final String blockedWord;

        BlockedWordGuardrail(String blockedWord) {
            this.blockedWord = blockedWord;
        }

        @Override
        public InputGuardrailResult validate(UserMessage userMessage) {
            if (userMessage.singleText().toLowerCase().contains(blockedWord)) {
                return failure("The message mentions '" + blockedWord + "'");
            }
            return success();
        }
    }
}
