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
import com.vaadin.flow.component.ai.orchestrator.BaseAiOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import reactor.core.publisher.Flux;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class connects an {@link AiMessageList}, {@link AiInput}, and an
 * {@link LLMProvider} to create an interactive AI chat experience. It handles:
 * </p>
 * <ul>
 * <li>Streaming responses from the LLM to the message list</li>
 * <li>Automatic UI updates via server push</li>
 * <li>Optional file upload support</li>
 * </ul>
 * <p>
 * Conversation history is managed internally by the {@link LLMProvider}
 * instance. Each orchestrator maintains its own conversation context through
 * its provider instance.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * MessageList messageList = new MessageList();
 * MessageInput messageInput = new MessageInput();
 *
 * LLMProvider provider = new LangChain4jProvider(model);
 * provider.setSystemPrompt("You are a helpful assistant.");
 *
 * AiChatOrchestrator orchestrator = AiChatOrchestrator.create(provider)
 *         .withMessageList(messageList)
 *         .withInput(messageInput)
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChatOrchestrator extends BaseAiOrchestrator {

    /**
     * Creates a new AI chat orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     */
    private AiChatOrchestrator(LLMProvider provider) {
        super(provider);
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
    public static class Builder extends BaseBuilder<AiChatOrchestrator, Builder> {

        private Builder(LLMProvider provider) {
            super(provider);
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        @Override
        public AiChatOrchestrator build() {
            AiChatOrchestrator orchestrator = new AiChatOrchestrator(provider);

            // Apply common configuration from base builder
            applyCommonConfiguration(orchestrator);

            if (input != null) {
                input.addSubmitListener(orchestrator::handleUserMessage);
            }

            return orchestrator;
        }
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
     * Handles a user message submission.
     *
     * @param event
     *            the submit event
     */
    private void handleUserMessage(
            com.vaadin.flow.component.ai.input.InputSubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to UI
        if (messageList != null) {
            AiMessage userItem = messageList.createMessage(userMessage, "User");
            messageList.addMessage(userItem);
        }

        // Generate AI response
        generateAiResponse(userMessage);
    }

    /**
     * Generates an AI response based on the user message.
     *
     * @param userMessage
     *            the user's message
     */
    private void generateAiResponse(String userMessage) {
        if (messageList == null) {
            return;
        }

        UI ui = validateUiContext();

        // Create a placeholder for the assistant's message
        AiMessage assistantMessage = messageList.createMessage("", "Assistant");
        messageList.addMessage(assistantMessage);

        StringBuilder fullResponse = new StringBuilder();

        // Build LLM request
        LLMProvider.LLMRequest request = new LLMProvider.LLMRequestBuilder()
                .userMessage(userMessage)
                .build();

        // Get streaming response from LLM
        Flux<String> responseStream = provider.stream(request);

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
            // Streaming complete - provider has already added the response to
            // conversation history
        });
    }
}
