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
package com.vaadin.flow.component.ai.provider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Media;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;

/**
 * Spring AI implementation of {@link LLMProvider}.
 * <p>
 * Supports both streaming and non-streaming Spring AI models. Tool calling is
 * supported through Spring AI's {@link Tool} annotation.
 * <p>
 * Each provider instance maintains its own chat memory. To share conversation
 * history across components, reuse the same provider instance.
 *
 * @author Vaadin Ltd
 */
public class SpringAiLLMProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpringAiLLMProvider.class);

    private static final int MAX_MESSAGES = 30;
    private static final int MAX_TOOL_EXECUTION_DEPTH = 20;
    private static final String CONVERSATION_ID = "default";

    private final transient ChatClient chatClient;
    private final boolean isStreaming;

    /**
     * Constructor with a chat model and streaming mode configuration.
     *
     * @param chatModel
     *            the chat model, not {@code null}
     * @param streaming
     *            {@code true} to use streaming mode, {@code false} for
     *            non-streaming
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public SpringAiLLMProvider(ChatModel chatModel, boolean streaming) {
        Objects.requireNonNull(chatModel, "ChatModel must not be null");
        var chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES).build();
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(CONVERSATION_ID).build())
                .build();
        isStreaming = streaming;
    }

    /**
     * Constructor with a chat client and streaming mode configuration. Note:
     * When using this constructor, conversation memory must be configured
     * externally in the {@link ChatClient}.
     *
     * @param chatClient
     *            the chat client, not {@code null}
     * @param streaming
     *            {@code true} to use streaming mode, {@code false} for
     *            non-streaming
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public SpringAiLLMProvider(ChatClient chatClient, boolean streaming) {
        Objects.requireNonNull(chatClient, "ChatClient must not be null");
        this.chatClient = chatClient;
        this.isStreaming = streaming;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(request.userMessage(),
                "User message must not be null");
        var toolCallbacks = prepareToolCallbacks(request);
        if (isStreaming) {
            return executeStreamingChat(request, toolCallbacks);
        }
        return executeNonStreamingChat(request, toolCallbacks);
    }

    private List<FunctionToolCallback<Object, Object>> prepareToolCallbacks(
            LLMRequest request) {
        var tools = request.tools();
        if (tools == null) {
            return Collections.emptyList();
        }
        var executionCounter = new AtomicInteger(0);
        var callbacks = new ArrayList<FunctionToolCallback<Object, Object>>();
        for (var toolObject : tools) {
            Arrays.stream(toolObject.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Tool.class))
                    .map(method -> createToolCallback(toolObject, method,
                            executionCounter))
                    .forEach(callbacks::add);
        }
        return callbacks;
    }

    private FunctionToolCallback<Object, Object> createToolCallback(
            Object toolObject, Method method, AtomicInteger executionCounter) {
        var toolAnnotation = method.getAnnotation(Tool.class);
        var toolName = toolAnnotation.name().isEmpty() ? method.getName()
                : toolAnnotation.name();
        var description = toolAnnotation.description().isEmpty()
                ? "Executes " + toolName
                : toolAnnotation.description();

        UnaryOperator<Object> function = input -> {
            int currentDepth = executionCounter.incrementAndGet();
            if (currentDepth > MAX_TOOL_EXECUTION_DEPTH) {
                throw new IllegalStateException(
                        "Maximum tool execution depth exceeded: "
                                + currentDepth);
            }
            return invokeMethod(toolObject, method, input);
        };
        return FunctionToolCallback.builder(toolName, function)
                .description(description).inputType(Object.class).build();
    }

    private Object invokeMethod(Object toolObject, Method method,
            Object input) {
        try {
            if (method.getParameterCount() == 0) {
                return method.invoke(toolObject);
            }
            return method.invoke(toolObject, input);
        } catch (Exception e) {
            LOGGER.error("Failed to invoke tool method: {}", method.getName(),
                    e);
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Tool execution failed", e);
        }
    }

    private Flux<String> executeStreamingChat(LLMRequest request,
            List<FunctionToolCallback<Object, Object>> toolCallbacks) {
        try {
            return getPromptSpec(request, toolCallbacks).stream().content();
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    private ChatClient.ChatClientRequestSpec getPromptSpec(LLMRequest request,
            List<FunctionToolCallback<Object, Object>> toolCallbacks) {
        var promptSpec = chatClient.prompt();
        promptSpec = promptSpec.user(userSpec -> {
            userSpec.text(request.userMessage());
            var media = buildMedia(request);
            if (media.length != 0) {
                userSpec.media(media);
            }
        });
        if (request.systemPrompt() != null
                && !request.systemPrompt().trim().isEmpty()) {
            promptSpec = promptSpec.system(request.systemPrompt().trim());
        }
        if (!toolCallbacks.isEmpty()) {
            promptSpec = promptSpec.toolCallbacks(
                    toolCallbacks.toArray(new FunctionToolCallback[0]));
        }
        return promptSpec;
    }

    private Flux<String> executeNonStreamingChat(LLMRequest request,
            List<FunctionToolCallback<Object, Object>> toolCallbacks) {
        return Flux.create(sink -> {
            try {
                var promptSpec = getPromptSpec(request, toolCallbacks);
                var response = promptSpec.call().content();
                if (response != null && !response.isEmpty()) {
                    sink.next(response);
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private Media[] buildMedia(LLMRequest request) {
        var attachments = request.attachments();
        if (attachments == null) {
            return new Media[0];
        }
        return attachments.stream().map(SpringAiLLMProvider::getAttachmentMedia)
                .filter(Optional::isPresent).map(Optional::get)
                .toArray(Media[]::new);
    }

    private static Optional<Media> getAttachmentMedia(Attachment attachment) {
        LLMProviderHelpers.validateAttachment(attachment);
        var contentType = AttachmentContentType
                .fromMimeType(attachment.contentType());
        return switch (contentType) {
        case TEXT -> Optional.of(getTextMedia(attachment));
        case IMAGE, PDF, AUDIO, VIDEO -> Optional.of(getMedia(attachment));
        case UNSUPPORTED -> Optional.empty();
        };
    }

    private static Media getMedia(Attachment attachment) {
        var mimeType = MimeType.valueOf(attachment.contentType());
        var resource = new ByteArrayResource(attachment.data());
        return Media.builder().mimeType(mimeType).data(resource).build();
    }

    private static Media getTextMedia(Attachment attachment) {
        var textContent = LLMProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.fileName(), false);
        var formattedText = LLMProviderHelpers
                .formatTextAttachment(attachment.fileName(), textContent);
        return Media.builder().mimeType(MimeType.valueOf("text/plain"))
                .data(formattedText).build();
    }
}
