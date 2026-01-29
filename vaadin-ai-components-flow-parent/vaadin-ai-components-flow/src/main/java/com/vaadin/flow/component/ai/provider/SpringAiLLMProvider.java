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

import java.util.Objects;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Media;
import org.springframework.ai.tool.annotation.Tool;
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

    private static final int MAX_MESSAGES = 30;
    private static final String CONVERSATION_ID = "default";

    private final transient ChatClient chatClient;
    private boolean isStreaming = true;

    /**
     * Constructor with a chat model.
     *
     * @param chatModel
     *            the chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public SpringAiLLMProvider(ChatModel chatModel) {
        Objects.requireNonNull(chatModel, "ChatModel must not be null");
        var chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES).build();
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(CONVERSATION_ID).build())
                .build();
    }

    /**
     * Constructor with a chat client. Note: When using this constructor,
     * conversation memory must be configured externally in the
     * {@link ChatClient}.
     *
     * @param chatClient
     *            the chat client, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public SpringAiLLMProvider(ChatClient chatClient) {
        Objects.requireNonNull(chatClient, "ChatClient must not be null");
        this.chatClient = chatClient;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(request.userMessage(),
                "User message must not be null");
        if (isStreaming) {
            return executeStreamingChat(request);
        }
        return executeNonStreamingChat(request);
    }

    /**
     * Sets whether to use streaming mode. The default is {@code true}.
     *
     * @param streaming
     *            {@code true} to use streaming mode, {@code false} for
     *            non-streaming.
     */
    public void setStreaming(boolean streaming) {
        this.isStreaming = streaming;
    }

    private Flux<String> executeStreamingChat(LLMRequest request) {
        try {
            return getPromptSpec(request).stream().content();
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    private ChatClient.ChatClientRequestSpec getPromptSpec(LLMRequest request) {
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
        var tools = request.tools();
        if (tools != null && tools.length > 0) {
            promptSpec = promptSpec.tools(tools);
        }
        return promptSpec;
    }

    private Flux<String> executeNonStreamingChat(LLMRequest request) {
        return Flux.create(sink -> {
            try {
                var promptSpec = getPromptSpec(request);
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
