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
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

/**
 * A chat on a {@link SpringAILLMProvider} whose chat client is customized
 * through the provider builder: an advisor that adds a fixed fact to every
 * turn, a tool callback provider standing in for MCP, and a guardrail advisor.
 * A controller adds a tool of its own next to the client's. The model is a
 * script that echoes the user message it received, calls the tool the message
 * asks for, or relays a tool result, so the tests can see what the pipeline
 * delivered to the model.
 */
@Route("vaadin-ai/pipeline/spring-ai")
public class SpringAIPipelinePage extends VerticalLayout {

    static final String SYSTEM_PROMPT = "You are a scripted assistant.";
    static final String FACT = "The internal codename of the ACME support "
            + "assistant project is Bluefin.";
    static final String BLOCKED_WORD = "password";
    static final String GUARDRAIL_REPLY = "I cannot help with that.";
    static final String WEATHER_TOOL = "getWeather";
    static final String ORDER_TOOL = "getOrderStatus";

    public SpringAIPipelinePage() {
        setHeightFull();
        // The response arrives from the model's thread in a real setup; the
        // scripted model answers on the request thread, but the page keeps
        // the push setup a real one needs
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var provider = SpringAILLMProvider.builder(new ScriptedChatModel())
                .customize(client -> client.defaultSystem(SYSTEM_PROMPT)
                        .defaultAdvisors(new FixedFactAdvisor(),
                                SafeGuardAdvisor.builder()
                                        .sensitiveWords(List.of(BLOCKED_WORD))
                                        .failureResponse(GUARDRAIL_REPLY)
                                        .build())
                        .defaultTools(ToolCallbackProvider
                                .from(new WeatherToolCallback())))
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
     * anything else is echoed as the model received it, added context included.
     */
    private static final class ScriptedChatModel implements ChatModel {
        @Override
        public ChatResponse call(Prompt prompt) {
            var last = prompt.getInstructions().getLast();
            if (last instanceof ToolResponseMessage toolResponse) {
                return answer("Tool said: " + toolResponse.getResponses()
                        .getFirst().responseData());
            }
            var userText = last instanceof UserMessage userMessage
                    ? userMessage.getText()
                    : "";
            var toolName = toolFor(userText);
            if (toolName != null) {
                var toolCall = AssistantMessage.builder().content("").toolCalls(
                        List.of(new AssistantMessage.ToolCall("call-1",
                                "function", toolName, "{}")))
                        .build();
                return new ChatResponse(List.of(new Generation(toolCall,
                        ChatGenerationMetadata.builder()
                                .finishReason("TOOL_CALLS").build())));
            }
            return answer("Echo: " + userText);
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            return Flux.just(call(prompt));
        }

        @Override
        public ChatOptions getOptions() {
            // The client attaches the tools of a request to the model's
            // options only when they are tool calling options, which the
            // plain default is not
            return ToolCallingChatOptions.builder().build();
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

        private static ChatResponse answer(String text) {
            return new ChatResponse(List.of(new Generation(
                    new AssistantMessage(text), ChatGenerationMetadata.builder()
                            .finishReason("STOP").build())));
        }
    }

    /**
     * An advisor that adds a fixed fact to every user message, standing in for
     * a retrieval advisor backed by a vector store.
     */
    private static final class FixedFactAdvisor implements BaseAdvisor {
        @Override
        public ChatClientRequest before(ChatClientRequest request,
                AdvisorChain chain) {
            var augmented = request.prompt().augmentUserMessage(
                    request.prompt().getUserMessage().getText()
                            + "\n\nContext: " + FACT);
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
     * A tool callback registered on the client, the way an MCP tool callback
     * provider's would be.
     */
    private static final class WeatherToolCallback implements ToolCallback {
        @Override
        public ToolDefinition getToolDefinition() {
            return DefaultToolDefinition.builder().name(WEATHER_TOOL)
                    .description("The current weather")
                    .inputSchema("{\"type\":\"object\",\"properties\":{}}")
                    .build();
        }

        @Override
        public String call(String arguments) {
            return "Sunny";
        }
    }

    /**
     * A controller whose tool must reach the model next to the tools of the
     * client.
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
}
