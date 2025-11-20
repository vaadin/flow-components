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
package com.vaadin.flow.component.ai.chat;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.input.AiInput;
import com.vaadin.flow.component.ai.messagelist.AiMessage;
import com.vaadin.flow.component.ai.messagelist.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.upload.AiFileReceiver;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class connects an {@link AiMessageList}, {@link AiInput}, and an
 * {@link LLMProvider} to create an interactive AI chat experience. It handles:
 * </p>
 * <ul>
 * <li>Conversation history management</li>
 * <li>Streaming responses from the LLM to the message list</li>
 * <li>Automatic UI updates via server push</li>
 * <li>Optional file upload support</li>
 * </ul>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * MessageList messageList = new MessageList();
 * MessageInput messageInput = new MessageInput();
 *
 * LLMProvider provider = new LangChain4jProvider(model);
 * AiChatOrchestrator orchestrator = AiChatOrchestrator.create(provider)
 *         .withMessageList(messageList)
 *         .withInput(messageInput)
 *         .build();
 * orchestrator.setSystemPrompt("You are a helpful assistant.");
 * orchestrator.setUserName("User");
 * orchestrator.setAssistantName("AI Assistant");
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChatOrchestrator implements Serializable {

    private final LLMProvider provider;
    private AiMessageList messageList;
    private AiInput input;
    private AiFileReceiver fileReceiver;

    private final List<LLMProvider.Message> conversationHistory = new ArrayList<>();

    /**
     * Creates a new AI chat orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     */
    private AiChatOrchestrator(LLMProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
    }

    /**
     * Creates a new builder for AiChatOrchestrator.
     *
     * @param provider
     *            the LLM provider
     * @return a new builder
     */
    public static Builder create(LLMProvider provider) {
        return new Builder(provider);
    }

    /**
     * Builder for AiChatOrchestrator.
     */
    public static class Builder {
        private final LLMProvider provider;
        private AiMessageList messageList;
        private AiInput input;
        private AiFileReceiver fileReceiver;

        private Builder(LLMProvider provider) {
            this.provider = provider;
        }

        /**
         * Sets the message list component.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public Builder withMessageList(AiMessageList messageList) {
            this.messageList = messageList;
            return this;
        }

        /**
         * Sets the input component.
         *
         * @param input
         *            the input component
         * @return this builder
         */
        public Builder withInput(AiInput input) {
            this.input = input;
            return this;
        }

        /**
         * Sets the file receiver component for file uploads.
         *
         * @param fileReceiver
         *            the file receiver
         * @return this builder
         */
        public Builder withFileReceiver(AiFileReceiver fileReceiver) {
            this.fileReceiver = fileReceiver;
            return this;
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public AiChatOrchestrator build() {
            AiChatOrchestrator orchestrator = new AiChatOrchestrator(provider);
            orchestrator.messageList = messageList;
            orchestrator.input = input;
            orchestrator.fileReceiver = fileReceiver;

            if (input != null) {
                input.addSubmitListener(orchestrator::handleUserMessage);
            }

            return orchestrator;
        }
    }

    /**
     * Gets the LLM provider.
     *
     * @return the provider
     */
    public LLMProvider getProvider() {
        return provider;
    }

    /**
     * Gets the message list component.
     *
     * @return the message list
     */
    public AiMessageList getMessageList() {
        return messageList;
    }

    /**
     * Gets the input component.
     *
     * @return the input component
     */
    public AiInput getInput() {
        return input;
    }

    /**
     * Gets the file receiver component.
     *
     * @return the file receiver, or null if not configured
     */
    public AiFileReceiver getFileReceiver() {
        return fileReceiver;
    }


    /**
     * Handles a user message submission.
     *
     * @param event
     *            the submit event
     */
    private void handleUserMessage(com.vaadin.flow.component.ai.input.InputSubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to conversation history
        LLMProvider.Message message = LLMProvider.createMessage("user",
                userMessage);
        conversationHistory.add(message);

        // Add user message to UI
        if (messageList != null) {
            AiMessage userItem = messageList.createMessage(userMessage, "User");
            messageList.addMessage(userItem);
        }

        // Generate AI response
        generateAiResponse();
    }

    /**
     * Generates an AI response based on the conversation history.
     */
    private void generateAiResponse() {
        if (messageList == null) {
            return;
        }

        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException(
                    "No UI found. Make sure the orchestrator is used within a UI context.");
        }

        // Create a placeholder for the assistant's message
        AiMessage assistantMessage = messageList.createMessage("", "Assistant");
        messageList.addMessage(assistantMessage);

        StringBuilder fullResponse = new StringBuilder();

        // Get streaming response from LLM
        Flux<String> responseStream = provider.generateStream(
                conversationHistory, null, null);

        responseStream.subscribe(token -> {
            // Append token to the full response
            fullResponse.append(token);

            // Update UI with the accumulated response
            ui.access(() -> {
                assistantMessage.setText(fullResponse.toString());
                messageList.updateMessage(assistantMessage);
            });
        }, error -> {
            // Handle error
            ui.access(() -> {
                assistantMessage.setText("Error: " + error.getMessage());
                messageList.updateMessage(assistantMessage);
            });
        }, () -> {
            // When complete, add to conversation history
            ui.access(() -> {
                LLMProvider.Message llmMessage = LLMProvider
                        .createMessage("assistant", fullResponse.toString());
                conversationHistory.add(llmMessage);
            });
        });
    }
}
