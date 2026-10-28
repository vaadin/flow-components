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
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import tools.jackson.databind.JsonNode;

/**
 * A chat on a {@link LangChain4JLLMProvider} whose AI service is customized
 * through the provider builder: a retriever that injects a fixed fact into
 * every turn, a tool provider standing in for MCP, and an input guardrail. A
 * controller adds a tool of its own next to the provider's. The model is a
 * script that echoes the user message it received, calls the tool the message
 * asks for, or relays a tool result, so the tests can see what the pipeline
 * delivered to the model.
 */
@Route("vaadin-ai/pipeline/langchain4j")
public class LangChain4JPipelinePage extends VerticalLayout {

    static final String SYSTEM_PROMPT = "You are a scripted assistant.";
    static final String FACT = "The internal codename of the ACME support "
            + "assistant project is Bluefin.";
    static final String BLOCKED_WORD = "password";
    static final String WEATHER_TOOL = "getWeather";
    static final String ORDER_TOOL = "getOrderStatus";

    public LangChain4JPipelinePage() {
        setHeightFull();
        // The response arrives from the model's thread in a real setup; the
        // scripted model answers on the request thread, but the page keeps
        // the push setup a real one needs
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var provider = LangChain4JLLMProvider
                .builder(new ScriptedStreamingChatModel())
                .systemPrompt(SYSTEM_PROMPT)
                .customize(service -> service
                        .contentRetriever(query -> List.of(Content.from(FACT)))
                        .toolProvider(new WeatherToolProvider())
                        .inputGuardrails(new BlockedWordGuardrail()))
                .build();

        var messageList = new MessageList();
        messageList.setId("message-list");
        messageList.setSizeFull();
        var messageInput = new MessageInput();
        messageInput.setId("message-input");
        messageInput.setWidthFull();

        AIOrchestrator.builder(provider).withMessageList(messageList)
                .withInput(messageInput)
                .withController(new OrderStatusController()).build();
        add(messageList, messageInput);
    }

    /**
     * Answers from a script instead of a model: a tool result is relayed, a
     * message starting with "weather" or "order" calls the matching tool, and
     * anything else is echoed as the model received it, retrieved content
     * included.
     */
    private static final class ScriptedStreamingChatModel
            implements StreamingChatModel {
        @Override
        public void doChat(ChatRequest request,
                StreamingChatResponseHandler handler) {
            var last = request.messages().getLast();
            if (last instanceof ToolExecutionResultMessage result) {
                answer(handler, "Tool said: " + result.text());
                return;
            }
            var userText = last instanceof UserMessage userMessage
                    ? userMessage.contents().stream()
                            .filter(TextContent.class::isInstance)
                            .map(TextContent.class::cast).map(TextContent::text)
                            .collect(Collectors.joining())
                    : "";
            var toolName = toolFor(userText);
            if (toolName != null) {
                handler.onCompleteResponse(ChatResponse.builder()
                        .aiMessage(AiMessage.from(ToolExecutionRequest.builder()
                                .id("call-1").name(toolName).arguments("{}")
                                .build()))
                        .finishReason(FinishReason.TOOL_EXECUTION).build());
                return;
            }
            answer(handler, "Echo: " + userText);
        }

        private static String toolFor(String userText) {
            var lower = userText.toLowerCase();
            if (lower.startsWith("weather")) {
                return WEATHER_TOOL;
            }
            if (lower.startsWith("order")) {
                return ORDER_TOOL;
            }
            return null;
        }

        private static void answer(StreamingChatResponseHandler handler,
                String text) {
            handler.onPartialResponse(text);
            handler.onCompleteResponse(
                    ChatResponse.builder().aiMessage(AiMessage.from(text))
                            .finishReason(FinishReason.STOP).build());
        }
    }

    /**
     * A tool provider registered in the customizer, the way an MCP tool
     * provider would be.
     */
    private static final class WeatherToolProvider implements ToolProvider {
        @Override
        public ToolProviderResult provideTools(ToolProviderRequest request) {
            return ToolProviderResult.builder()
                    .add(ToolSpecification.builder().name(WEATHER_TOOL)
                            .description("The current weather").build(),
                            (toolRequest, memoryId) -> "Sunny")
                    .build();
        }
    }

    /**
     * A controller whose tool must reach the model next to the tools of the
     * customizer.
     */
    private static final class OrderStatusController implements AIController {
        @Override
        public List<LLMProvider.ToolSpec> getTools() {
            return List.of(new LLMProvider.ToolSpec() {
                @Override
                public String getName() {
                    return ORDER_TOOL;
                }

                @Override
                public String getDescription() {
                    return "The status of the user's order";
                }

                @Override
                public String getParametersSchema() {
                    return null;
                }

                @Override
                public String execute(JsonNode arguments) {
                    return "Shipped";
                }
            });
        }
    }

    /**
     * Refuses a user message mentioning the blocked word, which ends the turn
     * with an error before the model is called.
     */
    private static final class BlockedWordGuardrail implements InputGuardrail {
        @Override
        public InputGuardrailResult validate(UserMessage userMessage) {
            if (userMessage.singleText().toLowerCase().contains(BLOCKED_WORD)) {
                return failure("The message mentions '" + BLOCKED_WORD + "'");
            }
            return success();
        }
    }
}
