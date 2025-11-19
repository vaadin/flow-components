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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.server.Command;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class connects a {@link MessageList}, {@link MessageInput}, and an
 * {@link LLMProvider} to create an interactive AI chat experience. It handles:
 * </p>
 * <ul>
 * <li>Conversation history management</li>
 * <li>Streaming responses from the LLM to the MessageList</li>
 * <li>Automatic UI updates via server push</li>
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
 * AiChatOrchestrator orchestrator = new AiChatOrchestrator(provider,
 *         messageList, messageInput);
 * orchestrator.setSystemPrompt("You are a helpful assistant.");
 * orchestrator.setUserName("User");
 * orchestrator.setAssistantName("AI Assistant");
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiChatOrchestrator implements Serializable {

    private final LLMProvider provider;
    private final MessageList messageList;
    private final MessageInput messageInput;

    private String systemPrompt = "";
    private String userName = "User";
    private String assistantName = "Assistant";

    private final List<LLMProvider.Message> conversationHistory = new ArrayList<>();

    /**
     * Creates a new AI chat orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     * @param messageList
     *            the message list component to display messages
     * @param messageInput
     *            the message input component for user input
     */
    public AiChatOrchestrator(LLMProvider provider, MessageList messageList,
            MessageInput messageInput) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        Objects.requireNonNull(messageList, "MessageList cannot be null");
        Objects.requireNonNull(messageInput, "MessageInput cannot be null");

        this.provider = provider;
        this.messageList = messageList;
        this.messageInput = messageInput;

        // Listen to message input submissions
        messageInput.addSubmitListener(this::handleUserMessage);
    }

    /**
     * Sets the system prompt that guides the LLM's behavior.
     *
     * @param systemPrompt
     *            the system prompt
     */
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt != null ? systemPrompt : "";
    }

    /**
     * Gets the system prompt.
     *
     * @return the system prompt
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * Sets the name to display for user messages.
     *
     * @param userName
     *            the user name
     */
    public void setUserName(String userName) {
        this.userName = userName != null ? userName : "User";
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the name to display for assistant messages.
     *
     * @param assistantName
     *            the assistant name
     */
    public void setAssistantName(String assistantName) {
        this.assistantName = assistantName != null ? assistantName
                : "Assistant";
    }

    /**
     * Gets the assistant name.
     *
     * @return the assistant name
     */
    public String getAssistantName() {
        return assistantName;
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
    public MessageList getMessageList() {
        return messageList;
    }

    /**
     * Gets the message input component.
     *
     * @return the message input
     */
    public MessageInput getMessageInput() {
        return messageInput;
    }

    /**
     * Gets the conversation history.
     *
     * @return an unmodifiable list of messages in the conversation
     */
    public List<LLMProvider.Message> getConversationHistory() {
        return List.copyOf(conversationHistory);
    }

    /**
     * Clears the conversation history and message list.
     */
    public void clearConversation() {
        conversationHistory.clear();
        messageList.setItems(new ArrayList<>());
    }

    /**
     * Handles a user message submission.
     *
     * @param event
     *            the submit event
     */
    private void handleUserMessage(MessageInput.SubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to conversation history
        LLMProvider.Message message = LLMProvider.createMessage("user",
                userMessage);
        conversationHistory.add(message);

        // Add user message to UI
        MessageListItem userItem = new MessageListItem(userMessage,
                Instant.now(), userName);
        addMessageToList(userItem);

        // Generate AI response
        generateAiResponse();
    }

    /**
     * Generates an AI response based on the conversation history.
     */
    private void generateAiResponse() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException(
                    "No UI found. Make sure the orchestrator is used within a UI context.");
        }

        // Create a placeholder for the assistant's message
        MessageListItem assistantItem = new MessageListItem("",
                Instant.now(), assistantName);
        addMessageToList(assistantItem);

        StringBuilder fullResponse = new StringBuilder();

        // Get streaming response from LLM
        Flux<String> responseStream = provider.generateStream(
                conversationHistory, systemPrompt, null);

        responseStream.subscribe(token -> {
            // Append token to the full response
            fullResponse.append(token);

            // Update UI with the accumulated response
            ui.access(() -> {
                assistantItem.setText(fullResponse.toString());
                updateMessageInList(assistantItem);
            });
        }, error -> {
            // Handle error
            ui.access(() -> {
                assistantItem.setText(
                        "Error: " + error.getMessage());
                updateMessageInList(assistantItem);
            });
        }, () -> {
            // When complete, add to conversation history
            ui.access(() -> {
                LLMProvider.Message assistantMessage = LLMProvider
                        .createMessage("assistant", fullResponse.toString());
                conversationHistory.add(assistantMessage);
            });
        });
    }

    /**
     * Adds a message to the message list.
     *
     * @param item
     *            the message item to add
     */
    private void addMessageToList(MessageListItem item) {
        List<MessageListItem> items = new ArrayList<>(messageList.getItems());
        items.add(item);
        messageList.setItems(items);
    }

    /**
     * Updates a message in the message list (triggers re-render).
     *
     * @param item
     *            the message item that was updated
     */
    private void updateMessageInList(MessageListItem item) {
        // Trigger update by setting items again
        List<MessageListItem> items = new ArrayList<>(messageList.getItems());
        messageList.setItems(items);
    }
}
