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
package com.vaadin.flow.component.ai.provider.springai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;

import com.vaadin.flow.component.ai.provider.LLMProvider;

import reactor.core.publisher.Flux;

/**
 * Spring AI implementation of LLMProvider using ChatClient.
 * Handles conversation memory internally using Spring AI's ChatMemory and MessageChatMemoryAdvisor.
 * Supports tool calling through ChatClient's fluent API.
 *
 * <p>This implementation uses Spring AI 2.0's ChatClient API which provides:
 * <ul>
 * <li>Fluent API for building prompts</li>
 * <li>Built-in support for conversation memory via advisors</li>
 * <li>Easy tool registration and execution</li>
 * <li>Streaming and non-streaming responses</li>
 * <li>Multimodal support for image attachments</li>
 * </ul>
 * </p>
 *
 * @author Vaadin Ltd
 */
public class SpringAiLLMProvider implements LLMProvider {

    private static final String DEFAULT_CONVERSATION_ID = "default";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private String defaultSystemPrompt;

    /**
     * Constructor that accepts a ChatClient.
     * Note: When using this constructor, conversation memory must be configured
     * externally in the ChatClient.
     *
     * @param chatClient
     *            the Spring AI ChatClient instance
     */
    public SpringAiLLMProvider(ChatClient chatClient) {
        this.chatClient = chatClient;
        // Create a simple in-memory chat memory
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(30)
                .build();
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        this.defaultSystemPrompt = systemPrompt;
    }

    /**
     * Gets the underlying ChatClient.
     *
     * @return the ChatClient instance
     */
    public ChatClient getChatClient() {
        return chatClient;
    }

    /**
     * Gets the chat memory instance.
     *
     * @return the ChatMemory instance
     */
    public ChatMemory getChatMemory() {
        return chatMemory;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        try {
            // Use request system prompt if provided, otherwise fall back to default
            String systemPrompt = request.systemPrompt() != null
                ? request.systemPrompt()
                : defaultSystemPrompt;

            // Start building the prompt with ChatClient's fluent API
            var promptSpec = chatClient.prompt();

            // Always add the memory advisor to ensure chat history is maintained
            // This is especially important when ChatClient was passed directly
            // (without being built with default advisors)
            promptSpec = promptSpec.advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                    .conversationId(DEFAULT_CONVERSATION_ID)
                    .build());

            // Add system prompt if provided
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                promptSpec = promptSpec.system(systemPrompt);
            }

            // Process attachments
            var processedAttachments = processAttachments(request.attachments());

            // Add user message with text and media attachments
            promptSpec = promptSpec.user(u -> {
                u.text(request.userMessage() + processedAttachments.documentContent());
                if (!processedAttachments.mediaList().isEmpty()) {
                    u.media(processedAttachments.mediaList().toArray(Media[]::new));
                }
            });

            // Add tools if provided
            if (request.toolObjects() != null && request.toolObjects().length > 0) {
                promptSpec = promptSpec.tools(request.toolObjects());
            }

            // Stream the response using ChatClient
            return promptSpec.stream().content();

        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    /**
     * Clears the conversation history.
     */
    public void clearHistory() {
        chatMemory.clear(DEFAULT_CONVERSATION_ID);
    }

    private ProcessedAttachments processAttachments(List<Attachment> attachments) {
        var documentBuilder = new StringBuilder();

        // Process text and PDF attachments as document content
        var documentAttachments = attachments.stream()
                .filter(attachment -> attachment.contentType().contains("text")
                        || attachment.contentType().contains("pdf"))
                .toList();

        for (Attachment attachment : documentAttachments) {
            String textContent = new String(attachment.data());
            documentBuilder.append("\n<attachment filename=\"")
                    .append(attachment.fileName())
                    .append("\">\n")
                    .append(textContent)
                    .append("\n</attachment>\n");
        }

        // Process image attachments as Media objects
        var mediaList = attachments.stream()
                .filter(attachment -> attachment.contentType().contains("image"))
                .map(attachment -> new Media(
                        MimeType.valueOf(attachment.contentType()),
                        new ByteArrayResource(attachment.data())))
                .toList();

        return new ProcessedAttachments(documentBuilder.toString(), mediaList);
    }

    private record ProcessedAttachments(String documentContent, List<Media> mediaList) {
    }
}
