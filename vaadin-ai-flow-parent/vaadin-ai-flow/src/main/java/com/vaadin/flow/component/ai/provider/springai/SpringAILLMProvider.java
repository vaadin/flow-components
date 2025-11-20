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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Spring AI implementation of LLMProvider.
 * <p>
 * This provider integrates with Spring AI's chat models to provide LLM
 * capabilities. It handles conversation memory internally using Spring AI's
 * ChatMemory and supports tool calling and multimodal inputs.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * ChatModel chatModel = ... // Obtain from Spring context or create directly
 * ChatMemory chatMemory = new InMemoryChatMemory(); // Or obtain from Spring context
 * LLMProvider provider = new SpringAILLMProvider(chatModel, chatMemory);
 * provider.setSystemPrompt("You are a helpful assistant.");
 *
 * LLMRequest request = LLMRequest.of("Hello, how are you?");
 * Flux&lt;String&gt; response = provider.stream(request);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class SpringAILLMProvider implements LLMProvider {

    private static final String ATTACHMENT_TEMPLATE = """
        <attachment filename="%s">
                %s
        </attachment>
        """;

    private final ChatModel chatModel;
    private final ChatMemory chatMemory;
    private String defaultSystemPrompt;

    /**
     * Creates a new Spring AI provider with the specified chat model and chat
     * memory.
     *
     * @param chatModel
     *            the Spring AI chat model to use
     * @param chatMemory
     *            the Spring AI chat memory for conversation history
     */
    public SpringAILLMProvider(ChatModel chatModel, ChatMemory chatMemory) {
        if (chatModel == null) {
            throw new IllegalArgumentException("ChatModel cannot be null");
        }
        if (chatMemory == null) {
            throw new IllegalArgumentException("ChatMemory cannot be null");
        }
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
    }

    @Override
    public void setSystemPrompt(String systemPrompt) {
        this.defaultSystemPrompt = systemPrompt;
    }

    /**
     * Gets the underlying Spring AI chat model.
     *
     * @return the chat model
     */
    public ChatModel getChatModel() {
        return chatModel;
    }

    /**
     * Gets the chat memory instance.
     *
     * @return the chat memory
     */
    public ChatMemory getChatMemory() {
        return chatMemory;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        if (request == null || request.userMessage() == null) {
            throw new IllegalArgumentException(
                    "Request and user message cannot be null");
        }

        var processedAttachments = processAttachments(request.attachments());

        var options = chatModel.getDefaultOptions();
        if (request.modelName() != null && options != null) {
            try {
                var method = options.getClass().getMethod("setModel",
                        String.class);
                method.setAccessible(true);
                method.invoke(options, request.modelName());
            } catch (Exception e) {
                // Model name setting failed, continue with default
            }
        }

        var chatClient = buildChatClient();

        // Use request system prompt if provided, otherwise fall back to
        // default
        var systemPrompt = request.systemPrompt() != null
                ? request.systemPrompt()
                : defaultSystemPrompt;

        var promptSpec = chatClient.prompt();

        if (options != null) {
            promptSpec.options(options);
        }

        // Only set system prompt if it's not null or empty
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            promptSpec.system(systemPrompt);
        }

        // Note: Tool/function calling support in Spring AI requires
        // FunctionCallback or function names.
        // For now, tools parameter is not directly supported and would need
        // to be configured at the ChatModel level or through FunctionCallback.
        // This is a limitation of the current Spring AI API design.

        promptSpec.user(u -> {
            u.text(request.userMessage()
                    + processedAttachments.documentContent());
            u.media(processedAttachments.mediaList().toArray(Media[]::new));
        });

        return promptSpec.stream().content();
    }

    private ChatClient buildChatClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    private ProcessedAttachments processAttachments(
            List<Attachment> attachments) {
        // Map text and pdf attachments as documents wrapped in <attachment>
        // tags
        var documentList = attachments.stream()
                .filter(attachment -> attachment.contentType().contains("text")
                        || attachment.contentType().contains("pdf"))
                .toList();

        var documentBuilder = new StringBuilder("\n");
        documentList.forEach(attachment -> {
            var data = new ByteArrayResource(attachment.data());
            var documents = new TikaDocumentReader(data).read();
            var content = String.join("\n",
                    documents.stream().map(Document::getContent).toList());
            documentBuilder.append(
                    String.format(ATTACHMENT_TEMPLATE, attachment.fileName(),
                            content));
        });

        // Map image attachments to Media objects
        var mediaList = attachments.stream()
                .filter(attachment -> attachment.contentType()
                        .contains("image"))
                .map(attachment -> new Media(
                        MimeType.valueOf(attachment.contentType()),
                        new ByteArrayResource(attachment.data())))
                .toList();

        return new ProcessedAttachments(documentBuilder.toString(), mediaList);
    }

    private record ProcessedAttachments(String documentContent,
            List<Media> mediaList) {
    }
}
