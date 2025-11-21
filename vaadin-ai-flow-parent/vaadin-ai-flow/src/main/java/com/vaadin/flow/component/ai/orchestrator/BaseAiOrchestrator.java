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
package com.vaadin.flow.component.ai.orchestrator;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.input.AiInput;
import com.vaadin.flow.component.ai.messagelist.AiMessage;
import com.vaadin.flow.component.ai.messagelist.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.upload.AiFileReceiver;
import com.vaadin.flow.server.streams.UploadHandler;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for AI orchestrators providing common functionality.
 * <p>
 * This abstract class handles:
 * </p>
 * <ul>
 * <li>LLM provider management</li>
 * <li>Input component integration</li>
 * <li>File upload integration</li>
 * <li>UI context validation</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
public abstract class BaseAiOrchestrator implements Serializable {

    protected final LLMProvider provider;
    protected AiMessageList messageList;
    protected AiInput input;
    protected AiFileReceiver fileReceiver;
    protected final List<LLMProvider.Attachment> pendingAttachments = new ArrayList<>();

    /**
     * Creates a new base orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     */
    protected BaseAiOrchestrator(LLMProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
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
     * Sets the message list component.
     *
     * @param messageList
     *            the message list
     */
    protected void setMessageList(AiMessageList messageList) {
        this.messageList = messageList;
    }

    /**
     * Sets the input component.
     *
     * @param input
     *            the input component
     */
    protected void setInput(AiInput input) {
        this.input = input;
    }

    /**
     * Sets the file receiver component.
     *
     * @param fileReceiver
     *            the file receiver
     */
    protected void setFileReceiver(AiFileReceiver fileReceiver) {
        this.fileReceiver = fileReceiver;
    }

    /**
     * Validates that a UI context exists for the current thread.
     *
     * @return the current UI
     * @throws IllegalStateException
     *             if no UI context is available
     */
    protected UI validateUiContext() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException(
                    "No UI found. Make sure the orchestrator is used within a UI context.");
        }
        return ui;
    }

    /**
     * Adds a user message to the message list.
     *
     * @param userMessage
     *            the user's message text
     */
    protected void addUserMessageToList(String userMessage) {
        if (messageList != null) {
            AiMessage userItem = messageList.createMessage(userMessage, "User");
            messageList.addMessage(userItem);
        }
    }

    /**
     * Creates and adds an assistant message placeholder to the message list.
     *
     * @return the created assistant message, or null if messageList is not
     *         configured
     */
    protected AiMessage createAssistantMessagePlaceholder() {
        if (messageList == null) {
            return null;
        }
        AiMessage assistantMessage = messageList.createMessage("", "Assistant");
        messageList.addMessage(assistantMessage);
        return assistantMessage;
    }

    /**
     * Streams a response from the LLM and updates the assistant message in
     * real-time.
     *
     * @param request
     *            the LLM request to process
     * @param assistantMessage
     *            the assistant message to update (can be null if messageList is
     *            not configured)
     * @param onComplete
     *            optional callback to execute when streaming completes
     *            successfully (can be null)
     */
    protected void streamResponseToMessage(LLMProvider.LLMRequest request,
            AiMessage assistantMessage, Runnable onComplete) {
        UI ui = validateUiContext();

        StringBuilder fullResponse = new StringBuilder();

        Flux<String> responseStream = provider.stream(request);

        responseStream.subscribe(token -> {
            // Append token to the full response
            fullResponse.append(token);

            // Update UI with the accumulated response
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> {
                    assistantMessage.setText(fullResponse.toString());
                    messageList.updateMessage(assistantMessage);
                });
            }
        }, error -> {
            // Handle error
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> {
                    assistantMessage.setText("Error: " + error.getMessage());
                    messageList.updateMessage(assistantMessage);
                });
            }
        }, () -> {
            // Streaming complete - provider has already added the response to
            // conversation history
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Streams a response with custom token processing.
     *
     * @param request
     *            the LLM request to process
     * @param onToken
     *            callback to process each token
     * @param onError
     *            callback to handle errors
     * @param onComplete
     *            callback when streaming completes
     */
    protected void streamResponse(LLMProvider.LLMRequest request,
            Consumer<String> onToken, Consumer<Throwable> onError,
            Runnable onComplete) {
        Flux<String> responseStream = provider.stream(request);
        responseStream.subscribe(onToken::accept, onError::accept,
                onComplete);
    }

    /**
     * Handles a user input submission event. This method implements the common
     * pattern of validating input, adding the user message to the UI, and
     * delegating to the subclass for processing.
     * <p>
     * Subclasses must implement {@link #processUserInput(String)} to define
     * their specific processing logic.
     * </p>
     *
     * @param event
     *            the input submit event containing the user's message
     */
    protected void handleUserInput(
            com.vaadin.flow.component.ai.input.InputSubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to UI
        addUserMessageToList(userMessage);

        // Delegate to subclass for specific processing
        processUserInput(userMessage);
    }

    /**
     * Processes the user's input message by building an LLM request and
     * streaming the response. This method implements the common pattern shared
     * by all orchestrators.
     *
     * @param userMessage
     *            the user's input message
     */
    protected void processUserInput(String userMessage) {
        // Create a placeholder for the assistant's message (may be null if no
        // messageList)
        AiMessage assistantMessage = createAssistantMessagePlaceholder();

        // Get tools from subclass
        LLMProvider.Tool[] tools = createTools();

        // Build LLM request with any pending attachments
        LLMProvider.LLMRequestBuilder requestBuilder = new LLMProvider.LLMRequestBuilder()
                .userMessage(userMessage)
                .attachments(new ArrayList<>(pendingAttachments));

        // Add system prompt if provided by subclass
        String systemPrompt = getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            requestBuilder.systemPrompt(systemPrompt);
        }

        // Add tools if provided by subclass
        if (tools != null && tools.length > 0) {
            requestBuilder.tools(tools);
        }

        LLMProvider.LLMRequest request = requestBuilder.build();

        // Clear pending attachments after building the request
        pendingAttachments.clear();
        if (fileReceiver != null) {
            fileReceiver.clearFileList();
        }

        // Stream response using base class method
        streamResponseToMessage(request, assistantMessage,
                () -> onProcessingComplete());
    }

    /**
     * Creates tools for the LLM to use. Subclasses that need tool support
     * should override this method.
     *
     * @return array of tools, or empty array if no tools needed
     */
    protected LLMProvider.Tool[] createTools() {
        return new LLMProvider.Tool[0];
    }

    /**
     * Returns the system prompt for the LLM. Subclasses that need a system
     * prompt should override this method.
     *
     * @return the system prompt, or null if no system prompt needed
     */
    protected String getSystemPrompt() {
        return null;
    }

    /**
     * Called when processing is complete. Subclasses can override to perform
     * additional actions after streaming completes.
     */
    protected void onProcessingComplete() {
        // Default: do nothing
    }

    /**
     * Configures the file receiver with the appropriate upload handler for
     * managing file attachments.
     */
    protected void configureFileReceiver() {
        if (fileReceiver == null) {
            return;
        }

        fileReceiver.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            pendingAttachments.add(LLMProvider.Attachment.of(
                    meta.fileName(),
                    meta.contentType(),
                    data));
        }));

        fileReceiver.addFileRemovedListener(fileName -> {
            pendingAttachments.removeIf(
                    attachment -> attachment.fileName().equals(fileName));
        });
    }

    /**
     * Base builder for orchestrators.
     *
     * @param <T>
     *            the orchestrator type being built
     * @param <B>
     *            the builder type (for method chaining)
     */
    protected abstract static class BaseBuilder<T extends BaseAiOrchestrator, B extends BaseBuilder<T, B>> {
        protected final LLMProvider provider;
        protected AiMessageList messageList;
        protected AiInput input;
        protected AiFileReceiver fileReceiver;

        protected BaseBuilder(LLMProvider provider) {
            this.provider = provider;
        }

        /**
         * Sets the message list component.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public B withMessageList(AiMessageList messageList) {
            this.messageList = messageList;
            return self();
        }

        /**
         * Sets the input component.
         *
         * @param input
         *            the input component
         * @return this builder
         */
        public B withInput(AiInput input) {
            this.input = input;
            return self();
        }

        /**
         * Sets the file receiver component for file uploads.
         *
         * @param fileReceiver
         *            the file receiver
         * @return this builder
         */
        public B withFileReceiver(AiFileReceiver fileReceiver) {
            this.fileReceiver = fileReceiver;
            return self();
        }

        /**
         * Returns this builder instance with the correct type.
         *
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        /**
         * Applies common configuration to the orchestrator being built.
         *
         * @param orchestrator
         *            the orchestrator to configure
         */
        protected void applyCommonConfiguration(T orchestrator) {
            orchestrator.setMessageList(messageList);
            orchestrator.setInput(input);
            orchestrator.setFileReceiver(fileReceiver);

            // Configure input listener if provided
            if (input != null) {
                input.addSubmitListener(orchestrator::handleUserInput);
            }

            if (messageList != null) {
                messageList.setMarkdown(true);
            }

            // Configure file receiver if provided
            if (fileReceiver != null) {
                orchestrator.configureFileReceiver();
            }
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public abstract T build();
    }
}
