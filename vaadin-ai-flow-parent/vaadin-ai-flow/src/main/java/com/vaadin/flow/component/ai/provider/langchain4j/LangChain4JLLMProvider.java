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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LangChain4j implementation of LLMProvider.
 * Handles conversation memory internally using LangChain4j's ChatMemory.
 *
 * @author Vaadin Ltd
 */
public class LangChain4JLLMProvider implements LLMProvider {

    private final StreamingChatLanguageModel chatModel;
    private final ChatMemory chatMemory;
    private String defaultSystemPrompt;

    /**
     * Constructor with explicit chat memory.
     *
     * @param chatModel
     *            the streaming chat language model
     * @param chatMemory
     *            the chat memory for conversation history
     */
    public LangChain4JLLMProvider(StreamingChatLanguageModel chatModel, ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
    }

    /**
     * Constructor that creates a default MessageWindowChatMemory.
     *
     * @param chatModel
     *            the streaming chat language model
     */
    public LangChain4JLLMProvider(StreamingChatLanguageModel chatModel) {
        this(chatModel, MessageWindowChatMemory.withMaxMessages(10));
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        this.defaultSystemPrompt = systemPrompt;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        return Flux.create(sink -> {
            try {
                // Add user message with attachments to memory
                chatMemory.add(buildUserMessage(request));

                // Prepare tools once at the beginning
                Map<String, ToolExecutor> toolExecutors = prepareToolExecutors(request);
                List<ToolSpecification> toolSpecifications = prepareToolSpecifications(request);

                // Start the chat with tool execution loop
                executeChatWithTools(request, sink, toolExecutors, toolSpecifications);
            } catch (Exception e) {
                sink.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    private Map<String, ToolExecutor> prepareToolExecutors(LLMRequest request) {
        Map<String, ToolExecutor> toolExecutors = new HashMap<>();

        // Add tools from toolObjects (LangChain4j @Tool annotated methods)
        if (request.toolObjects() != null && request.toolObjects().length > 0) {
            for (Object toolObject : request.toolObjects()) {
                // Create executors for each @Tool annotated method
                // Use getDeclaredMethods() to also get private methods
                Method[] allMethods = toolObject.getClass().getDeclaredMethods();

                for (Method method : allMethods) {
                    if (method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                        // Make the method accessible if it's private
                        method.setAccessible(true);

                        // Generate the tool specification for this specific method
                        ToolSpecification methodSpec = ToolSpecifications.toolSpecificationFrom(method);

                        // Capture current UI if available
                        UI currentUI = UI.getCurrent();

                        // Wrap the executor to handle UI access if needed
                        ToolExecutor baseExecutor = new DefaultToolExecutor(toolObject, method);
                        ToolExecutor wrappedExecutor = (toolRequest, memoryId) -> {
                            if (currentUI != null) {
                                // Execute in UI context synchronously
                                String[] result = new String[1];
                                Exception[] error = new Exception[1];

                                try {
                                    currentUI.access(() -> {
                                        try {
                                            result[0] = baseExecutor.execute(toolRequest, memoryId);
                                        } catch (Exception e) {
                                            error[0] = e;
                                        }
                                    }).get(); // Wait for completion
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to execute tool in UI context", e);
                                }

                                if (error[0] != null) {
                                    throw new RuntimeException(error[0]);
                                }
                                return result[0];
                            } else {
                                // No UI context, execute directly
                                return baseExecutor.execute(toolRequest, memoryId);
                            }
                        };

                        // Use the specification name as the key
                        toolExecutors.put(methodSpec.name(), wrappedExecutor);
                    }
                }
            }
        }

        // Add tools from LLMProvider.Tool[] (convert to LangChain4j format)
        if (request.tools() != null && request.tools().length > 0) {
            for (Tool tool : request.tools()) {
                // Create executor wrapper for LLMProvider.Tool
                toolExecutors.put(tool.getName(), (toolExecRequest, memoryId) -> tool.execute(toolExecRequest.arguments()));
            }
        }

        return toolExecutors;
    }

    private List<ToolSpecification> prepareToolSpecifications(LLMRequest request) {
        List<ToolSpecification> toolSpecifications = new ArrayList<>();

        // Add tools from toolObjects (LangChain4j @Tool annotated methods)
        if (request.toolObjects() != null && request.toolObjects().length > 0) {
            for (Object toolObject : request.toolObjects()) {
                var specs = ToolSpecifications.toolSpecificationsFrom(toolObject);
                toolSpecifications.addAll(specs);
            }
        }

        // Add tools from LLMProvider.Tool[] (convert to LangChain4j format)
        if (request.tools() != null && request.tools().length > 0) {
            for (Tool tool : request.tools()) {
                toolSpecifications.add(convertToToolSpecification(tool));
            }
        }

        return toolSpecifications;
    }

    private void executeChatWithTools(LLMRequest request, FluxSink<String> sink,
            Map<String, ToolExecutor> toolExecutors, List<ToolSpecification> toolSpecifications) {
        List<ChatMessage> messages = buildMessages(request);

        // Call LangChain4j generate method with tools
        if (!toolSpecifications.isEmpty()) {
            chatModel.generate(messages, toolSpecifications, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    sink.next(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    AiMessage aiMessage = response.content();
                    if (aiMessage != null) {
                        chatMemory.add(aiMessage);

                        // Check if the AI wants to execute tools
                        if (aiMessage.hasToolExecutionRequests()) {
                            for (var toolExecRequest : aiMessage.toolExecutionRequests()) {
                                ToolExecutor executor = toolExecutors.get(toolExecRequest.name());

                                String result;
                                try {
                                    result = executor != null ? executor.execute(toolExecRequest, null)
                                        : "Tool not found: " + toolExecRequest.name();
                                } catch (Exception e) {
                                    result = "Error executing tool: " + e.getMessage();
                                }

                                chatMemory.add(ToolExecutionResultMessage.from(toolExecRequest, result));
                            }
                            // Continue the conversation with tool results
                            executeChatWithTools(request, sink, toolExecutors, toolSpecifications);
                        } else {
                            sink.complete();
                        }
                    } else {
                        sink.complete();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            });
        } else {
            // No tools, just generate
            chatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    sink.next(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    AiMessage aiMessage = response.content();
                    if (aiMessage != null) {
                        chatMemory.add(aiMessage);
                    }
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            });
        }
    }

    private List<ChatMessage> buildMessages(LLMRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        // Use request value if provided, otherwise fall back to default
        var systemPrompt = request.systemPrompt() != null ? request.systemPrompt() : defaultSystemPrompt;

        // Add system message
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }

        // Add chat history from memory (which already includes the current user message with attachments)
        messages.addAll(chatMemory.messages());

        return messages;
    }

    private UserMessage buildUserMessage(LLMRequest request) {
        List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();

        // Add text content
        contents.add(TextContent.from(request.userMessage()));

        // Add image attachments
        for (Attachment attachment : request.attachments()) {
            if (attachment.contentType().startsWith("image/")) {
                // Convert byte[] to base64 data URL
                String base64 = java.util.Base64.getEncoder().encodeToString(attachment.data());
                String dataUrl = "data:" + attachment.contentType() + ";base64," + base64;
                contents.add(ImageContent.from(dataUrl, ImageContent.DetailLevel.AUTO));
            } else if (attachment.contentType().contains("text") || attachment.contentType().contains("pdf")) {
                // For text/pdf, add as text content
                String textContent = new String(attachment.data());
                contents.add(TextContent.from("\n<attachment filename=\"" + attachment.fileName() + "\">\n"
                    + textContent + "\n</attachment>\n"));
            }
        }

        return UserMessage.from(contents);
    }

    private ToolSpecification convertToToolSpecification(Tool tool) {
        var builder = ToolSpecification.builder()
            .name(tool.getName())
            .description(tool.getDescription());

        // Note: LangChain4j 0.36.2 uses JsonObjectSchema for parameters
        // For simplicity, we're not converting the JSON schema string here
        // If you need full parameter schema support for LLMProvider.Tool,
        // you would need to parse the JSON string and build a JsonObjectSchema

        return builder.build();
    }
}
