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
import com.vaadin.flow.internal.JacksonUtils;

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
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import tools.jackson.databind.JsonNode;

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
 * arrive, which requires automatic server push or polling to deliver them.
 * Annotate your UI class or application shell with {@code @Push}, or enable
 * polling with {@code UI.setPollInterval()}, before using a streaming model. A
 * warning is logged at runtime when neither is active.
 * </p>
 * <p>
 * <b>Blocking the request thread:</b> a {@link ChatModel} call blocks the
 * thread that subscribes to the response, which is the UI thread for a prompt
 * triggered from the browser. Call {@link #setBackgroundExecution(boolean)
 * setBackgroundExecution(true)} to run the call on a background thread instead,
 * so the request completes and the user's message renders while the LLM works.
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
 * @since 25.1
 */
public class LangChain4JLLMProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LangChain4JLLMProvider.class);

    private static final int MAX_MESSAGES = 30;

    private final transient StreamingChatModel streamingChatModel;
    private final transient ChatModel nonStreamingChatModel;
    private final transient ChatMemory chatMemory;
    private final BackgroundExecution backgroundExecution = new BackgroundExecution(
            LangChain4JLLMProvider.class);

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
        var response = Flux.<String> create(sink -> {
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
        return streamingChatModel != null
                ? backgroundExecution.applyToStreamingResponse(response)
                : backgroundExecution.applyToBlockingResponse(response);
    }

    /**
     * Gets whether the LLM call runs on a background thread.
     *
     * @return {@code true} if the call runs on a background thread,
     *         {@code false} if it runs on the thread that asks for the response
     * @since 25.3
     */
    public boolean isBackgroundExecution() {
        return backgroundExecution.isEnabled();
    }

    /**
     * Sets whether to run the LLM call on a background thread. The default is
     * {@code false}, which runs it on the thread that asks for the response —
     * the UI thread, for a prompt triggered from the browser. The setting has
     * no effect with a {@link StreamingChatModel}, whose response already
     * arrives on the LLM client's own threads.
     * <p>
     * A {@link ChatModel} call blocks for the whole turn, every tool call
     * included. On the UI thread that means holding the session lock until the
     * turn ends, so nothing the turn produces reaches the browser and the
     * application appears frozen. Set this to {@code true} to run the call on a
     * background thread instead: the request completes immediately, the user's
     * message and the assistant placeholder render, and the response is added
     * when it arrives.
     * <p>
     * This requires three things from the application:
     * <ul>
     * <li><b>A way to deliver the response.</b> Annotate the application shell
     * or UI class with {@code @Push}, or enable polling with
     * {@link UI#setPollInterval(int)}. Manual push mode is not enough on its
     * own, because nothing calls {@code ui.push()} for you. A warning is logged
     * when neither is active.</li>
     * <li><b>Thread-safe tools.</b> On a background thread Vaadin thread locals
     * such as {@link UI#getCurrent()} and framework contexts such as Spring
     * Security's {@code SecurityContext} are not bound, and UI components must
     * not be accessed directly. Wrap component access in {@code ui.access()},
     * or capture what you need in
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onRequest()},
     * which still runs on the UI thread. This is the same requirement a
     * {@link StreamingChatModel} already has.</li>
     *
     * <li><b>A gated input.</b> The orchestrator processes one prompt at a
     * time. Without background execution, a message submitted while a turn is
     * running waits for the session lock and is processed when the turn ends;
     * with it, the submit is rejected and dropped with a warning — and a
     * connected input has already cleared its text. Disable the input while a
     * turn is running, for example from
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onRequest()}
     * and
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onResponse(Throwable)}.</li>
     * </ul>
     *
     * <p>
     * Like the streaming mode, the setting is not preserved when the session is
     * serialized: an application that restores sessions must re-apply it when
     * it recreates the provider.
     * </p>
     *
     * @param backgroundExecution
     *            {@code true} to run the call on a background thread,
     *            {@code false} to run it on the thread that asks for the
     *            response
     * @since 25.3
     */
    public void setBackgroundExecution(boolean backgroundExecution) {
        this.backgroundExecution.setEnabled(backgroundExecution);
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
        var explicitTools = request.explicitTools();
        var toolExecutors = new HashMap<String, ToolExecutor>();
        // Add tools from LangChain4j @Tool annotated methods. Create executors
        // for each annotated method including private methods.
        if (tools != null) {
            for (var toolObject : tools) {
                Arrays.stream(toolObject.getClass().getDeclaredMethods())
                        .filter(method -> method
                                .isAnnotationPresent(Tool.class))
                        .forEach(method -> {
                            var toolExecutorKey = ToolSpecifications
                                    .toolSpecificationFrom(method).name();
                            var toolExecutor = getToolExecutor(toolObject,
                                    method);
                            toolExecutors.put(toolExecutorKey, toolExecutor);
                        });
            }
        }
        // Add explicit (framework-agnostic) tools
        for (var tool : explicitTools) {
            toolExecutors.put(tool.getName(), (execReq, memoryId) -> tool
                    .execute(parseExplicitToolArguments(execReq.arguments())));
        }
        return toolExecutors;
    }

    private static JsonNode parseExplicitToolArguments(String arguments) {
        try {
            return parseArguments(arguments);
        } catch (Exception e) {
            // The malformed JSON came from the model itself, so the parser
            // message is safe to relay and lets the model repair its next
            // attempt.
            throw new ToolException("invalid JSON arguments: " + e.getMessage(),
                    e);
        }
    }

    private ToolExecutor getToolExecutor(Object toolObject, Method method) {
        var baseExecutor = new DefaultToolExecutor(toolObject, method);
        return baseExecutor::execute;
    }

    private List<ToolSpecification> prepareToolSpecifications(
            LLMRequest request) {
        var specs = new ArrayList<ToolSpecification>();
        if (request.tools() != null) {
            Arrays.stream(request.tools())
                    .map(ToolSpecifications::toolSpecificationsFrom)
                    .flatMap(List::stream).forEach(specs::add);
        }
        request.explicitTools().stream()
                .map(LangChain4JLLMProvider::toToolSpecification)
                .forEach(specs::add);
        return specs;
    }

    private static ToolSpecification toToolSpecification(
            LLMProvider.ToolSpec tool) {
        var builder = ToolSpecification.builder().name(tool.getName())
                .description(tool.getDescription());
        var schema = tool.getParametersSchema();
        if (schema == null || schema.isBlank()) {
            // A tool without a declared schema breaks some LLM APIs —
            // see ToolSpec.NO_PARAMETERS_SCHEMA.
            schema = LLMProvider.ToolSpec.NO_PARAMETERS_SCHEMA;
        }
        builder.parameters(parseParametersSchema(schema));
        return builder.build();
    }

    private static JsonNode parseArguments(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return JacksonUtils.createObjectNode();
        }
        return JacksonUtils.readTree(arguments);
    }

    private static JsonObjectSchema parseParametersSchema(String schemaJson) {
        try {
            JsonNode root = JacksonUtils.readTree(schemaJson);
            var schemaBuilder = JsonObjectSchema.builder();
            if (root.has("properties")) {
                JsonNode props = root.get("properties");
                for (String name : props.propertyNames()) {
                    schemaBuilder.addProperty(name,
                            JsonRawSchema.from(props.get(name).toString()));
                }
            }
            if (root.has("required")) {
                var required = new ArrayList<String>();
                root.get("required")
                        .forEach(e -> required.add(e.stringValue()));
                schemaBuilder.required(required);
            }
            return schemaBuilder.build();
        } catch (Exception e) {
            LOGGER.warn("Failed to parse tool parameters schema, "
                    + "using no-parameters schema", e);
            // The constant is a well-formed literal, so this cannot recurse.
            return parseParametersSchema(
                    LLMProvider.ToolSpec.NO_PARAMETERS_SCHEMA);
        }
    }

    private void executeChat(ChatExecutionContext context) {
        var messages = buildMessages(context.getRequest(),
                context.getChatMemory());
        if (streamingChatModel != null) {
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
        context.observeMetadata(response);
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
            } catch (ToolException e) {
                LOGGER.warn("Tool '{}' failed: {}", toolExecRequest.name(),
                        e.getMessage(), e);
                result = "Error executing tool: " + e.getMessage();
            } catch (Exception e) {
                LOGGER.error("Tool '{}' failed", toolExecRequest.name(), e);
                result = "Error executing tool.";
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
        private FinishReason lastFinishReason;
        private TokenUsage accumulatedUsage;

        ChatExecutionContext(LLMRequest request, FluxSink<String> sink,
                ChatMemory chatMemory, ToolContext toolContext) {
            this.request = request;
            this.sink = sink;
            this.chatMemory = chatMemory;
            this.toolContext = toolContext;
        }

        /**
         * Records the metadata of one model round trip and passes the state
         * known so far to the metadata sink right away, so that a turn whose
         * later round trip fails has still reported what the earlier ones cost.
         * The reason that ends the turn wins; token usage accumulates across
         * the round trips.
         */
        void observeMetadata(ChatResponse response) {
            if (response.finishReason() == null
                    && response.tokenUsage() == null) {
                // Nothing new in this round trip, nothing to re-publish.
                return;
            }
            if (response.finishReason() != null) {
                lastFinishReason = response.finishReason();
            }
            var usage = response.tokenUsage();
            if (usage != null) {
                accumulatedUsage = accumulatedUsage == null ? usage
                        : accumulatedUsage.add(usage);
            }
            publishMetadata();
        }

        private void publishMetadata() {
            if (lastFinishReason == null && accumulatedUsage == null) {
                return;
            }
            var finishReason = lastFinishReason == null ? null
                    : lastFinishReason.name();
            var tokenUsage = accumulatedUsage == null ? null
                    : new ResponseMetadata.TokenUsage(
                            accumulatedUsage.inputTokenCount(),
                            accumulatedUsage.outputTokenCount(),
                            accumulatedUsage.totalTokenCount());
            request.metadataSink()
                    .accept(new ResponseMetadata(finishReason, tokenUsage));
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
