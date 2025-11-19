/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.provider.langchain4j;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * LangChain4j implementation of the LLMProvider interface.
 * <p>
 * This provider integrates with LangChain4j's streaming chat models to provide
 * LLM capabilities. It supports streaming responses for real-time UI updates.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
 *         .apiKey(System.getenv("OPENAI_API_KEY")).modelName("gpt-4")
 *         .build();
 * LLMProvider provider = new LangChain4jProvider(model);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class LangChain4jProvider implements LLMProvider {

    private final StreamingChatLanguageModel model;

    /**
     * Creates a new LangChain4j provider with the specified streaming chat
     * model.
     *
     * @param model
     *            the LangChain4j streaming chat model to use
     */
    public LangChain4jProvider(StreamingChatLanguageModel model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        this.model = model;
    }

    @Override
    public Flux<String> generateStream(List<Message> messages,
            String systemPrompt, List<Tool> tools) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException(
                    "Messages list cannot be null or empty");
        }

        return Flux.create(sink -> {
            List<ChatMessage> chatMessages = new ArrayList<>();

            // Add system prompt if provided
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                chatMessages.add(SystemMessage.from(systemPrompt));
            }

            // Convert messages to LangChain4j format
            for (Message message : messages) {
                ChatMessage chatMessage = convertMessage(message);
                if (chatMessage != null) {
                    chatMessages.add(chatMessage);
                }
            }

            // Convert tools to LangChain4j format
            List<ToolSpecification> toolSpecs = new ArrayList<>();
            Map<String, Tool> toolMap = new HashMap<>();
            if (tools != null && !tools.isEmpty()) {
                for (Tool tool : tools) {
                    // Build tool specification with JSON schema
                    ToolSpecification.Builder specBuilder = ToolSpecification.builder()
                            .name(tool.getName())
                            .description(tool.getDescription());

                    // Add parameters if provided
                    String paramsSchema = tool.getParametersSchema();
                    if (paramsSchema != null && !paramsSchema.trim().isEmpty() && !paramsSchema.equals("{}")) {
                        specBuilder.addParameter(paramsSchema);
                    }

                    ToolSpecification spec = specBuilder.build();
                    toolSpecs.add(spec);
                    toolMap.put(tool.getName(), tool);
                }
            }

            // Create streaming response handler with tool execution support
            StreamingResponseHandler<AiMessage> handler = new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    sink.next(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    AiMessage aiMessage = response.content();

                    // Check if the AI wants to call tools
                    if (aiMessage.hasToolExecutionRequests()) {
                        List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
                        List<ChatMessage> updatedMessages = new ArrayList<>(chatMessages);
                        updatedMessages.add(aiMessage);

                        // Execute each tool
                        for (ToolExecutionRequest request : toolRequests) {
                            String toolName = request.name();
                            String arguments = request.arguments();

                            Tool tool = toolMap.get(toolName);
                            if (tool != null) {
                                try {
                                    String result = tool.execute(arguments);
                                    ToolExecutionResultMessage resultMessage =
                                        ToolExecutionResultMessage.from(request, result);
                                    updatedMessages.add(resultMessage);
                                } catch (Exception e) {
                                    ToolExecutionResultMessage errorMessage =
                                        ToolExecutionResultMessage.from(request,
                                            "Error executing tool: " + e.getMessage());
                                    updatedMessages.add(errorMessage);
                                }
                            }
                        }

                        // Make another call with tool results
                        StreamingResponseHandler<AiMessage> followUpHandler =
                            new StreamingResponseHandler<AiMessage>() {
                                @Override
                                public void onNext(String token) {
                                    sink.next(token);
                                }

                                @Override
                                public void onComplete(Response<AiMessage> finalResponse) {
                                    sink.complete();
                                }

                                @Override
                                public void onError(Throwable error) {
                                    sink.error(error);
                                }
                            };

                        try {
                            if (toolSpecs.isEmpty()) {
                                model.generate(updatedMessages, followUpHandler);
                            } else {
                                model.generate(updatedMessages, toolSpecs, followUpHandler);
                            }
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    } else {
                        // No tools to execute, complete
                        sink.complete();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            };

            // Generate the streaming response
            try {
                if (toolSpecs.isEmpty()) {
                    model.generate(chatMessages, handler);
                } else {
                    model.generate(chatMessages, toolSpecs, handler);
                }
            } catch (Exception e) {
                sink.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }


    /**
     * Converts a generic Message to a LangChain4j ChatMessage.
     *
     * @param message
     *            the message to convert
     * @return the converted ChatMessage, or null if the role is not recognized
     */
    private ChatMessage convertMessage(Message message) {
        String role = message.getRole();
        String content = message.getContent();

        return switch (role.toLowerCase()) {
        case "user" -> UserMessage.from(content);
        case "assistant", "ai" -> AiMessage.from(content);
        case "system" -> SystemMessage.from(content);
        default -> {
            // Default to user message for unknown roles
            yield UserMessage.from(content);
        }
        };
    }

    /**
     * Gets the underlying LangChain4j streaming chat model.
     *
     * @return the streaming chat model
     */
    public StreamingChatLanguageModel getModel() {
        return model;
    }
}
