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

import com.vaadin.flow.component.ai.provider.LLMProvider;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI implementation of LLMProvider.
 * Handles conversation memory internally by maintaining message history.
 *
 * <p>Note: This implementation is built for Spring AI 1.1.0 and will need
 * to be migrated to Spring AI 2.0.0 when it becomes available.</p>
 *
 * @author Vaadin Ltd
 */
public class SpringAiLLMProvider implements LLMProvider {

    private final ChatModel chatModel;
    private final List<Message> messageHistory;
    private final int maxMessages;
    private String defaultSystemPrompt;

    /**
     * Constructor with explicit message history limit.
     *
     * @param chatModel
     *            the streaming chat model
     * @param maxMessages
     *            maximum number of messages to keep in history
     */
    public SpringAiLLMProvider(ChatModel chatModel, int maxMessages) {
        this.chatModel = chatModel;
        this.messageHistory = new ArrayList<>();
        this.maxMessages = maxMessages;
    }

    /**
     * Constructor with default message history limit (10 messages).
     *
     * @param chatModel
     *            the streaming chat model
     */
    public SpringAiLLMProvider(ChatModel chatModel) {
        this(chatModel, 10);
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        this.defaultSystemPrompt = systemPrompt;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        return Flux.create(sink -> {
            try {
                // Add user message to history
                messageHistory.add(buildUserMessage(request));
                trimMessageHistory();

                // Build the prompt with conversation history and tools
                Prompt prompt = buildPrompt(request);

                // Stream the response
                StringBuilder responseBuilder = new StringBuilder();

                chatModel.stream(prompt)
                    .map(ChatResponse::getResult)
                    .filter(result -> result != null && result.getOutput() != null)
                    .doOnNext(result -> {
                        AssistantMessage output = result.getOutput();
                        String text = output.getText();

                        // Stream text tokens if available
                        if (text != null && !text.isEmpty()) {
                            responseBuilder.append(text);
                            sink.next(text);
                        }
                    })
                    .doOnComplete(() -> {
                        // Add assistant response to history
                        if (responseBuilder.length() > 0) {
                            messageHistory.add(new AssistantMessage(responseBuilder.toString()));
                            trimMessageHistory();
                        }
                        sink.complete();
                    })
                    .doOnError(sink::error)
                    .subscribe();

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private Prompt buildPrompt(LLMRequest request) {
        List<Message> messages = new ArrayList<>();

        // Use request value if provided, otherwise fall back to default
        String systemPrompt = request.systemPrompt() != null
            ? request.systemPrompt()
            : defaultSystemPrompt;

        // Add system message
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        // Add chat history
        messages.addAll(messageHistory);

        // Note: Spring AI 1.1.0's tool calling support is limited in the streaming API
        // Tool objects with @Tool annotations need to be registered at the ChatModel level
        // or passed through model-specific options (e.g., OpenAiChatOptions)
        //
        // This is a known limitation that will be improved in Spring AI 2.0.0 with:
        // - ChatClient API for easier tool integration
        // - Better streaming support for tools
        // - ToolCallbacks.from() utility methods
        //
        // For now, users should:
        // 1. Register tools at the ChatModel level when creating the model
        // 2. Use model-specific ChatOptions implementations (e.g., OpenAiChatOptions)
        // 3. Or wait for Spring AI 2.0.0 for full tool support

        return new Prompt(messages);
    }

    private UserMessage buildUserMessage(LLMRequest request) {
        if (request.attachments().isEmpty()) {
            // Simple text-only message
            return new UserMessage(request.userMessage());
        }

        // Message with media attachments
        List<Media> mediaList = new ArrayList<>();
        StringBuilder messageText = new StringBuilder(request.userMessage());

        for (Attachment attachment : request.attachments()) {
            if (attachment.contentType().startsWith("image/")) {
                // Create Media object for the image using ByteArrayResource
                MimeType mimeType = MimeType.valueOf(attachment.contentType());
                ByteArrayResource resource = new ByteArrayResource(attachment.data());
                mediaList.add(new Media(mimeType, resource));
            } else if (attachment.contentType().contains("text")
                    || attachment.contentType().contains("pdf")) {
                // For text/PDF, add as text content
                String textContent = new String(attachment.data());
                messageText.append("\n<attachment filename=\"")
                          .append(attachment.fileName())
                          .append("\">\n")
                          .append(textContent)
                          .append("\n</attachment>\n");
            }
        }

        if (mediaList.isEmpty()) {
            return new UserMessage(messageText.toString());
        }

        // Create UserMessage with text and media using builder
        return UserMessage.builder()
                .text(messageText.toString())
                .media(mediaList)
                .build();
    }

    private void trimMessageHistory() {
        // Keep only the most recent messages
        while (messageHistory.size() > maxMessages) {
            messageHistory.remove(0);
        }
    }

    /**
     * Gets the current message history.
     *
     * @return list of messages in the conversation history
     */
    public List<Message> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    /**
     * Clears the conversation history.
     */
    public void clearHistory() {
        messageHistory.clear();
    }
}
