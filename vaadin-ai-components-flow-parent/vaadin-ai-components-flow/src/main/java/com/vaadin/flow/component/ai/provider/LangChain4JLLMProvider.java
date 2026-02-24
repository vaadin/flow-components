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
package com.vaadin.flow.component.ai.provider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.AttachmentContentType;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
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
 * </p>
 * <p>
 * <b>Streaming vs. non-streaming:</b> The mode is determined by the constructor
 * used. Pass a {@link StreamingChatModel} to
 * {@link #LangChain4JLLMProvider(StreamingChatModel)} for streaming, or a
 * {@link ChatModel} to {@link #LangChain4JLLMProvider(ChatModel)} for
 * non-streaming. Streaming mode pushes partial responses to the UI as they
 * arrive, which requires server push to be enabled. Annotate your UI class or
 * application shell with {@code @Push}, or configure push programmatically,
 * before using a streaming model. A warning is logged at runtime if push is not
 * enabled.
 * </p>
 * <p>
 * Each provider instance maintains its own chat memory. To share conversation
 * history across components, reuse the same provider instance.
 * </p>
 * <p>
 * <b>Note:</b> LangChain4JLLMProvider is not serializable. If your application
 * uses session persistence, you will need to create a new provider instance
 * after session restore.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class LangChain4JLLMProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LangChain4JLLMProvider.class);

    private static final int MAX_MESSAGES = 30;

    private final transient StreamingChatModel streamingChatModel;
    private final transient ChatModel nonStreamingChatModel;
    private final transient ChatMemory chatMemory;

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
                var userMessage = buildUserMessage(request);
                chatMemory.add(userMessage);
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

    @Override
    public void setHistory(List<ChatMessage> history,
            Map<String, List<AIAttachment>> attachmentsByMessageId) {
        Objects.requireNonNull(history, "History must not be null");
        Objects.requireNonNull(attachmentsByMessageId,
                "Attachments map must not be null");
        chatMemory.clear();
        for (var message : history) {
            var attachments = message.messageId() != null
                    ? attachmentsByMessageId.getOrDefault(message.messageId(),
                            Collections.emptyList())
                    : Collections.<AIAttachment> emptyList();
            chatMemory.add(toVendorMessage(message, attachments));
        }
    }

    private static dev.langchain4j.data.message.ChatMessage toVendorMessage(
            ChatMessage message) {
        if (message.role() == ChatMessage.Role.USER) {
            return UserMessage.from(message.content());
        }
        return AiMessage.from(message.content());
    }

    private static dev.langchain4j.data.message.ChatMessage toVendorMessage(
            ChatMessage message, List<AIAttachment> attachments) {
        if (message.role() != ChatMessage.Role.USER || attachments.isEmpty()) {
            return toVendorMessage(message);
        }
        var contents = new ArrayList<Content>();
        contents.add(TextContent.from(message.content()));
        attachments.stream().map(LangChain4JLLMProvider::getAttachmentContent)
                .flatMap(Optional::stream).forEach(contents::add);
        return UserMessage.from(contents);
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
                        var toolExecutorKey = ToolSpecifications
                                .toolSpecificationFrom(method).name();
                        var toolExecutor = getToolExecutor(toolObject, method);
                        toolExecutors.put(toolExecutorKey, toolExecutor);
                    });
        }
        return toolExecutors;
    }

    private ToolExecutor getToolExecutor(Object toolObject, Method method) {
        var baseExecutor = new DefaultToolExecutor(toolObject, method);
        return baseExecutor::execute;
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
        var messages = buildMessages(context.getRequest(),
                context.getChatMemory());
        if (streamingChatModel != null) {
            checkPushConfiguration();
            executeStreamingChat(messages, context);
        } else {
            executeNonStreamingChat(messages, context);
        }
    }

    private void executeStreamingChat(
            List<dev.langchain4j.data.message.ChatMessage> messages,
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

    private void executeNonStreamingChat(
            List<dev.langchain4j.data.message.ChatMessage> messages,
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

    private List<dev.langchain4j.data.message.ChatMessage> buildMessages(
            LLMRequest request, ChatMemory chatMemory) {
        var messages = new ArrayList<dev.langchain4j.data.message.ChatMessage>();
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
            AIAttachment attachment) {
        LLMProviderHelpers.validateAttachment(attachment);
        var contentType = AttachmentContentType
                .fromMimeType(attachment.mimeType());
        return switch (contentType) {
        case IMAGE -> Optional.of(getImageAttachmentContent(attachment));
        case TEXT -> Optional.of(getTextAttachmentContent(attachment));
        case PDF -> Optional.of(getPdfAttachmentContent(attachment));
        case AUDIO -> Optional.of(getAudioAttachmentContent(attachment));
        case VIDEO -> Optional.of(getVideoAttachmentContent(attachment));
        case UNSUPPORTED -> Optional.empty();
        };
    }

    private static TextContent getTextAttachmentContent(
            AIAttachment attachment) {
        var textContent = LLMProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.name(), false);
        return TextContent.from(LLMProviderHelpers
                .formatTextAttachment(attachment.name(), textContent));
    }

    private static PdfFileContent getPdfAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return PdfFileContent.from(base64, attachment.mimeType());
    }

    private static ImageContent getImageAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return ImageContent.from(base64, attachment.mimeType());
    }

    private static AudioContent getAudioAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return AudioContent.from(base64, attachment.mimeType());
    }

    private static VideoContent getVideoAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return VideoContent.from(base64, attachment.mimeType());
    }

    private static void checkPushConfiguration() {
        var ui = UI.getCurrent();
        if (ui != null && PushMode.DISABLED
                .equals(ui.getPushConfiguration().getPushMode())) {
            LOGGER.warn("Push is not enabled. Streaming LLM responses "
                    + "require @Push annotation or programmatic push "
                    + "configuration to update the UI in real-time.");
        }
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

        ChatExecutionContext(LLMRequest request, FluxSink<String> sink,
                ChatMemory chatMemory, ToolContext toolContext) {
            this.request = request;
            this.sink = sink;
            this.chatMemory = chatMemory;
            this.toolContext = toolContext;
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
    }
}
