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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.LLMProviderHelpers;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * LangChain4j implementation of {@link LLMProvider}.
 * <p>
 * Supports both streaming and non-streaming LangChain4j models. Tool calling is
 * supported through LangChain4j's {@link Tool} annotation.
 * <p>
 * Each provider instance maintains its own chat memory. To share conversation
 * history across components, reuse the same provider instance.
 *
 * @author Vaadin Ltd
 */
public class LangChain4JLLMProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LangChain4JLLMProvider.class);

    private static final int MAX_MESSAGES = 30;
    private static final int MAX_TOOL_EXECUTION_DEPTH = 20;

    private final transient StreamingChatModel streamingChatModel;
    private final transient ChatModel nonStreamingChatModel;
    private final transient ChatMemory chatMemory;

    private int toolExecutionTimeoutSeconds = 120;

    /**
     * Constructor with a streaming chat model.
     *
     * @param chatModel
     *            the streaming chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public LangChain4JLLMProvider(StreamingChatModel chatModel) {
        this(null, Objects.requireNonNull(chatModel,
                "StreamingChatModel must not be null"));
    }

    /**
     * Constructor with a non-streaming chat model.
     *
     * @param chatModel
     *            the non-streaming chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public LangChain4JLLMProvider(ChatModel chatModel) {
        this(Objects.requireNonNull(chatModel, "ChatModel must not be null"),
                null);
    }

    private LangChain4JLLMProvider(ChatModel chatModel,
            StreamingChatModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
        this.nonStreamingChatModel = chatModel;
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES);
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(request.userMessage(),
                "User message must not be null");
        return Flux.create(sink -> {
            try {
                chatMemory.add(buildUserMessage(request));
                var toolContext = new ToolContext(prepareToolExecutors(request),
                        prepareToolSpecifications(request));
                var context = new ChatExecutionContext(request, sink,
                        chatMemory, toolContext);
                executeChat(context);
            } catch (Exception e) {
                sink.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * Only for testing purposes
     */
    void setToolExecutionTimeoutSeconds(int toolExecutionTimeoutSeconds) {
        this.toolExecutionTimeoutSeconds = toolExecutionTimeoutSeconds;
    }

    private Map<String, ToolExecutor> prepareToolExecutors(LLMRequest request) {
        var tools = request.tools();
        if (tools == null) {
            return Collections.emptyMap();
        }
        var toolExecutors = new HashMap<String, ToolExecutor>();
        // Add tools from LangChain4j @Tool annotated methods. Create executors
        // for each annotated method including private methods.
        for (var toolObject : tools) {
            Arrays.stream(toolObject.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Tool.class))
                    .forEach(method -> {
                        try {
                            method.setAccessible(true);
                        } catch (Exception e) {
                            LOGGER.warn(
                                    "Failed to make tool method accessible: {}",
                                    method.getName(), e);
                            return;
                        }
                        var toolExecutor = getToolExecutor(toolObject, method);
                        var toolExecutorKey = ToolSpecifications
                                .toolSpecificationFrom(method).name();
                        toolExecutors.put(toolExecutorKey, toolExecutor);
                    });
        }
        return toolExecutors;
    }

    private ToolExecutor getToolExecutor(Object toolObject, Method method) {
        var baseExecutor = new DefaultToolExecutor(toolObject, method);
        return (toolRequest, memoryId) -> {
            var currentUI = UI.getCurrent();
            if (currentUI == null) {
                return baseExecutor.execute(toolRequest, memoryId);
            }
            return executeToolInUIContext(baseExecutor, toolRequest, memoryId,
                    currentUI);
        };
    }

    private String executeToolInUIContext(ToolExecutor baseExecutor,
            ToolExecutionRequest toolRequest, Object memoryId, UI ui) {
        var result = new AtomicReference<String>();
        var error = new AtomicReference<Exception>();
        try {
            ui.access(() -> {
                try {
                    result.set(baseExecutor.execute(toolRequest, memoryId));
                } catch (Exception e) {
                    error.set(e);
                }
            }).get(toolExecutionTimeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            error.set(e);
        }
        if (error.get() != null) {
            LOGGER.error("Tool execution failed: {}", toolRequest.name(),
                    error.get());
            return "Error executing tool: " + error.get().getMessage();
        }
        return result.get();
    }

    private List<ToolSpecification> prepareToolSpecifications(
            LLMRequest request) {
        if (request.tools() == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(request.tools())
                .map(ToolSpecifications::toolSpecificationsFrom)
                .flatMap(List::stream).toList();
    }

    private void executeChat(ChatExecutionContext context) {
        if (context.getDepth() == MAX_TOOL_EXECUTION_DEPTH) {
            context.getSink()
                    .error(new IllegalStateException(
                            "Maximum tool execution depth exceeded: "
                                    + context.getDepth()));
            return;
        }
        var messages = buildMessages(context.getRequest(),
                context.getChatMemory());
        if (streamingChatModel != null) {
            executeStreamingChat(messages, context);
        } else {
            executeNonStreamingChat(messages, context);
        }
    }

    private void executeStreamingChat(List<ChatMessage> messages,
            ChatExecutionContext context) {
        var chatRequestBuilder = ChatRequest.builder().messages(messages);
        var specifications = context.getToolContext().specifications();
        if (!specifications.isEmpty()) {
            chatRequestBuilder = chatRequestBuilder
                    .toolSpecifications(specifications);
        }
        var chatRequest = chatRequestBuilder.build();
        streamingChatModel.chat(chatRequest,
                new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        context.getSink().next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse response) {
                        handleResponse(context, response);
                    }

                    @Override
                    public void onError(Throwable error) {
                        context.getSink().error(error);
                    }
                });
    }

    private void executeToolRequests(AiMessage aiMessage,
            ChatExecutionContext context) {
        var toolExecutionRequests = aiMessage.toolExecutionRequests();
        for (var toolExecRequest : toolExecutionRequests) {
            var toolExecutor = context.getToolContext().executors()
                    .get(toolExecRequest.name());
            var result = executeToolRequest(toolExecutor, toolExecRequest);
            context.getChatMemory().add(result);
        }
    }

    private void executeNonStreamingChat(List<ChatMessage> messages,
            ChatExecutionContext context) {
        try {
            var requestBuilder = ChatRequest.builder().messages(messages);
            var specifications = context.getToolContext().specifications();
            if (!specifications.isEmpty()) {
                requestBuilder.toolSpecifications(specifications);
            }
            var response = nonStreamingChatModel.chat(requestBuilder.build());
            handleResponse(context, response);
        } catch (Exception e) {
            context.getSink().error(e);
        }
    }

    private void handleResponse(ChatExecutionContext context,
            ChatResponse response) {
        var aiMessage = response.aiMessage();
        if (aiMessage == null) {
            context.getSink().complete();
            return;
        }
        context.getChatMemory().add(aiMessage);
        if (!isStreaming()) {
            var text = aiMessage.text();
            if (text != null && !text.isEmpty()) {
                context.getSink().next(text);
            }
        }
        if (aiMessage.hasToolExecutionRequests()) {
            executeToolRequests(aiMessage, context);
            context.incrementDepth();
            executeChat(context);
        } else {
            context.getSink().complete();
        }
    }

    private static ToolExecutionResultMessage executeToolRequest(
            ToolExecutor toolExecutor, ToolExecutionRequest toolExecRequest) {
        String result;
        if (toolExecutor == null) {
            result = "Tool not found: " + toolExecRequest.name();
        } else {
            try {
                result = toolExecutor.execute(toolExecRequest, null);
            } catch (Exception e) {
                result = "Error executing tool: " + e.getMessage();
            }
        }
        return ToolExecutionResultMessage.from(toolExecRequest, result);
    }

    private List<ChatMessage> buildMessages(LLMRequest request,
            ChatMemory chatMemory) {
        var messages = new ArrayList<ChatMessage>();
        if (request.systemPrompt() != null) {
            var systemPrompt = request.systemPrompt().trim();
            if (!systemPrompt.isEmpty()) {
                messages.add(SystemMessage.from(systemPrompt));
            }
        }
        messages.addAll(chatMemory.messages());
        return messages;
    }

    private UserMessage buildUserMessage(LLMRequest request) {
        var contents = new ArrayList<Content>();
        contents.add(TextContent.from(request.userMessage()));
        var attachments = request.attachments();
        if (attachments != null) {
            attachments.stream()
                    .map(LangChain4JLLMProvider::getAttachmentContent)
                    .flatMap(Optional::stream).forEach(contents::add);
        }
        return UserMessage.from(contents);
    }

    private boolean isStreaming() {
        return streamingChatModel != null;
    }

    private static Optional<Content> getAttachmentContent(
            Attachment attachment) {
        LLMProviderHelpers.validateAttachment(attachment);
        var contentType = LLMProviderHelpers.AttachmentContentType
                .fromMimeType(attachment.contentType());
        return switch (contentType) {
        case IMAGE -> Optional.of(getImageAttachmentContent(attachment));
        case TEXT -> Optional.of(getTextAttachmentContent(attachment));
        case PDF -> Optional.of(getPdfAttachmentContent(attachment));
        case UNSUPPORTED -> Optional.empty();
        };
    }

    private static TextContent getTextAttachmentContent(Attachment attachment) {
        var textContent = LLMProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.fileName(), false);
        return TextContent.from(LLMProviderHelpers
                .formatTextAttachment(attachment.fileName(), textContent));
    }

    private static TextContent getPdfAttachmentContent(Attachment attachment) {
        var textContent = LLMProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.fileName(), true);
        return TextContent.from(LLMProviderHelpers
                .formatTextAttachment(attachment.fileName(), textContent));
    }

    private static ImageContent getImageAttachmentContent(
            Attachment attachment) {
        var dataUrl = LLMProviderHelpers.toBase64DataUrl(attachment.data(),
                attachment.contentType());
        return ImageContent.from(dataUrl, ImageContent.DetailLevel.AUTO);
    }

    /**
     * Encapsulates tool-related data for chat execution.
     */
    private record ToolContext(Map<String, ToolExecutor> executors,
            List<ToolSpecification> specifications) {
    }

    /**
     * Encapsulates execution state for a chat stream.
     */
    private static class ChatExecutionContext {
        private final LLMRequest request;
        private final FluxSink<String> sink;
        private final ChatMemory chatMemory;
        private final ToolContext toolContext;
        private final AtomicInteger depth;

        ChatExecutionContext(LLMRequest request, FluxSink<String> sink,
                ChatMemory chatMemory, ToolContext toolContext) {
            this.request = request;
            this.sink = sink;
            this.chatMemory = chatMemory;
            this.toolContext = toolContext;
            this.depth = new AtomicInteger(0);
        }

        LLMRequest getRequest() {
            return request;
        }

        FluxSink<String> getSink() {
            return sink;
        }

        ChatMemory getChatMemory() {
            return chatMemory;
        }

        ToolContext getToolContext() {
            return toolContext;
        }

        void incrementDepth() {
            depth.incrementAndGet();
        }

        int getDepth() {
            return depth.get();
        }
    }
}
