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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.streams.UploadHandler;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Generic AI orchestrator providing configuration-driven AI functionality.
 * <p>
 * This class handles:
 * </p>
 * <ul>
 * <li>LLM provider management</li>
 * <li>Token streaming</li>
 * <li>File attachment handling</li>
 * <li>Tool execution</li>
 * <li>UI-safe updates (via UI.access())</li>
 * <li>Programmatic invocation (prompt())</li>
 * </ul>
 * <p>
 * Instead of subclassing, configure an orchestrator instance through the
 * builder:
 * </p>
 *
 * <pre>
 * AiOrchestrator orchestrator = AiOrchestrator.builder(llmProvider)
 *         .withSystemPrompt(prompt)
 *         .withTools(toolSet)
 *         .withInput(messageInput)
 *         .withMessageList(messageList)
 *         .withFileReceiver(upload)
 *         .build();
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class AiOrchestrator implements Serializable {

    private final LLMProvider provider;
    private final String systemPrompt;
    private final List<LLMProvider.Tool> tools;
    private final List<Consumer<UpdateEvent>> updateListeners;
    private AiMessageList messageList;
    private AiInput input;
    private AiFileReceiver fileReceiver;
    private InputValidator inputValidator;
    private final List<LLMProvider.Attachment> pendingAttachments = new ArrayList<>();
    private Object[] toolObjects = new Object[0];
    private UI ui;

    /**
     * Creates a new orchestrator with the given configuration.
     *
     * @param builder
     *            the builder containing the configuration
     */
    private AiOrchestrator(Builder builder) {
        this.provider = Objects.requireNonNull(builder.provider,
                "Provider cannot be null");
        this.systemPrompt = builder.systemPrompt;
        this.tools = new ArrayList<>(builder.tools);
        this.updateListeners = new ArrayList<>(builder.updateListeners);
        this.messageList = builder.messageList;
        this.input = builder.input;
        this.fileReceiver = builder.fileReceiver;
        this.inputValidator = builder.inputValidator;
        this.toolObjects = builder.toolObjects;
        this.ui = builder.ui;

        // Configure input listener if provided
        if (input != null) {
            input.addSubmitListener(this::handleUserInput);
        }

        if (messageList != null) {
            messageList.setMarkdown(true);
        }

        // Configure file receiver if provided
        if (fileReceiver != null) {
            configureFileReceiver();
        }
    }

    /**
     * Creates a new builder for configuring an orchestrator.
     *
     * @param provider
     *            the LLM provider to use
     * @return a new builder instance
     */
    public static Builder builder(LLMProvider provider) {
        return new Builder(provider);
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
     * Gets the input component.
     *
     * @return the input component, or null if not set
     */
    public AiInput getInput() {
        return input;
    }

    /**
     * Validates that a UI context exists for the current thread.
     *
     * @return the current UI
     * @throws IllegalStateException
     *             if no UI context is available
     */
    private UI getOrValidateUi() {
        if (ui != null) {
            return ui;
        }

        UI currentUi = UI.getCurrent();
        if (currentUi == null) {
            throw new IllegalStateException(
                    "No UI found. Make sure the orchestrator is used within a UI context.");
        }

        this.ui = currentUi;
        return ui;
    }

    /**
     * Handles a validation rejection by displaying an error message to the
     * user.
     *
     * @param rejectionMessage
     *            the reason for rejection
     */
    private void handleValidationRejection(String rejectionMessage) {
        if (messageList != null) {
            AiMessage errorMessage = messageList
                    .createMessage("⚠️ " + rejectionMessage, "System");
            messageList.addMessage(errorMessage);
        }
    }

    /**
     * Adds a user message to the message list.
     *
     * @param userMessage
     *            the user's message text
     */
    private void addUserMessageToList(String userMessage) {
        if (messageList != null) {
            AiMessage userItem = messageList.createMessage(userMessage, "User");
            messageList.addMessage(userItem);

            if (pendingAttachments.isEmpty()) {
                return;
            }
            var attachmentsLayout = new Div();
            for (LLMProvider.Attachment attachment : pendingAttachments) {
                var fileComponent = new Span();
                fileComponent.getElement().getStyle().setMarginInlineEnd("20px");
                fileComponent.setText("📎 " + attachment.fileName());
                attachmentsLayout.add(fileComponent);
            }

            userItem.setPrefix(attachmentsLayout);
        }
    }

    /**
     * Creates and adds an assistant message placeholder to the message list.
     *
     * @return the created assistant message, or null if messageList is not
     *         configured
     */
    private AiMessage createAssistantMessagePlaceholder() {
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
    private void streamResponseToMessage(LLMProvider.LLMRequest request,
            AiMessage assistantMessage, Runnable onComplete) {

        Flux<String> responseStream = provider.stream(request);

        responseStream.subscribe(token -> {
            // Update UI with the accumulated response
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> {
                    assistantMessage.appendText(token);
                });
            }
        }, error -> {
            // Handle error
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> {
                    assistantMessage.setText("Error: " + error.getMessage());
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
     * Sends a prompt to the AI orchestrator programmatically.
     * <p>
     * This method allows triggering AI interaction from button clicks or other
     * UI events without using a message input component.
     * </p>
     *
     * @param text
     *            the prompt text to send to the AI
     */
    public void prompt(String text) {
        prompt(text, List.of());
    }

    /**
     * Sends a prompt with attachments to the AI orchestrator programmatically.
     *
     * @param text
     *            the prompt text to send to the AI
     * @param attachments
     *            the list of attachments to include
     */
    public void prompt(String text, List<LLMProvider.Attachment> attachments) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        // Add attachments to pending list
        if (attachments != null && !attachments.isEmpty()) {
            pendingAttachments.addAll(attachments);
        }

        // Add user message to UI if messageList is configured
        addUserMessageToList(text);

        // Process the message through the LLM
        processUserInput(text);
    }

    /**
     * Handles a user input submission event.
     *
     * @param event
     *            the input submit event containing the user's message
     */
    private void handleUserInput(
            com.vaadin.flow.component.ai.input.InputSubmitEvent event) {
        String userMessage = event.getValue();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        // Add user message to UI
        addUserMessageToList(userMessage);

        // Process the input
        processUserInput(userMessage);
    }

    /**
     * Processes the user's input message by building an LLM request and
     * streaming the response.
     *
     * @param userMessage
     *            the user's input message
     */
    private void processUserInput(String userMessage) {
        if (this.ui == null) {
            this.ui = getOrValidateUi();
        }

        // Validate user input if validator is configured
        if (inputValidator != null) {
            InputValidator.ValidationResult result = inputValidator
                    .validateInput(userMessage);
            if (!result.isAccepted()) {
                handleValidationRejection(result.getRejectionMessage());
                return;
            }
        }

        // Validate attachments if validator is configured
        if (inputValidator != null && !pendingAttachments.isEmpty()) {
            for (LLMProvider.Attachment attachment : pendingAttachments) {
                InputValidator.ValidationResult result = inputValidator
                        .validateAttachment(attachment);
                if (!result.isAccepted()) {
                    handleValidationRejection(result.getRejectionMessage());
                    return;
                }
            }
        }

        // Create a placeholder for the assistant's message (may be null if no
        // messageList)
        AiMessage assistantMessage = createAssistantMessagePlaceholder();

        // Build LLM request with any pending attachments
        LLMProvider.LLMRequestBuilder requestBuilder = new LLMProvider.LLMRequestBuilder()
                .userMessage(userMessage)
                .attachments(new ArrayList<>(pendingAttachments));

        // Add system prompt if configured
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            requestBuilder.systemPrompt(systemPrompt);
        }

        // Add tools if provided
        if (!tools.isEmpty()) {
            requestBuilder.tools(tools.toArray(new LLMProvider.Tool[0]));
        }

        if (toolObjects.length > 0) {
            requestBuilder.toolObjects(toolObjects);
        }

        LLMProvider.LLMRequest request = requestBuilder.build();

        // Clear pending attachments after building the request
        pendingAttachments.clear();
        if (fileReceiver != null) {
            ui.access(() -> {
                fileReceiver.clearFileList();
            });
        }

        // Stream response
        streamResponseToMessage(request, assistantMessage, () -> {
            // Notify update listeners
            UpdateEvent event = new UpdateEvent(this);
            for (Consumer<UpdateEvent> listener : updateListeners) {
                listener.accept(event);
            }
        });
    }

    /**
     * Configures the file receiver with the appropriate upload handler for
     * managing file attachments.
     */
    private void configureFileReceiver() {
        if (fileReceiver == null) {
            return;
        }

        fileReceiver.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            pendingAttachments
                    .add(LLMProvider.Attachment.of(meta.fileName(),
                            meta.contentType(), data));
        }));

        fileReceiver.addFileRemovedListener(fileName -> {
            pendingAttachments.removeIf(
                    attachment -> attachment.fileName().equals(fileName));
        });
    }

    /**
     * Event fired when the orchestrator completes processing an AI update.
     */
    public static class UpdateEvent implements Serializable {
        private final AiOrchestrator source;

        private UpdateEvent(AiOrchestrator source) {
            this.source = source;
        }

        /**
         * Gets the orchestrator that fired this event.
         *
         * @return the orchestrator
         */
        public AiOrchestrator getSource() {
            return source;
        }
    }

    /**
     * Builder for configuring an {@link AiOrchestrator}.
     */
    public static class Builder {
        private final LLMProvider provider;
        private String systemPrompt;
        private final List<LLMProvider.Tool> tools = new ArrayList<>();
        private final List<Consumer<UpdateEvent>> updateListeners = new ArrayList<>();
        private AiMessageList messageList;
        private AiInput input;
        private AiFileReceiver fileReceiver;
        private InputValidator inputValidator;
        private Object[] toolObjects = new Object[0];
        private UI ui;

        private Builder(LLMProvider provider) {
            this.provider = provider;
        }

        /**
         * Sets the system prompt for the orchestrator.
         *
         * @param systemPrompt
         *            the system prompt text
         * @return this builder
         */
        public Builder withSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Adds tools to the orchestrator.
         * <p>
         * This method can be called multiple times to add tools from different
         * sources (e.g., predefined tool sets, custom tools).
         * </p>
         *
         * @param tools
         *            the tools to add
         * @return this builder
         */
        public Builder withTools(LLMProvider.Tool... tools) {
            if (tools != null) {
                for (LLMProvider.Tool tool : tools) {
                    if (tool != null) {
                        this.tools.add(tool);
                    }
                }
            }
            return this;
        }

        /**
         * Adds a list of tools to the orchestrator.
         *
         * @param tools
         *            the list of tools to add
         * @return this builder
         */
        public Builder withTools(List<LLMProvider.Tool> tools) {
            if (tools != null) {
                this.tools.addAll(tools);
            }
            return this;
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
         * Sets the input validator for security checks.
         * <p>
         * Validators can prevent prompt injection attacks and enforce content
         * policies by checking both text input and file attachments before they
         * are sent to the LLM.
         * </p>
         *
         * @param inputValidator
         *            the input validator
         * @return this builder
         */
        public Builder withInputValidator(InputValidator inputValidator) {
            this.inputValidator = inputValidator;
            return this;
        }

        /**
         * Sets the objects containing vendor-specific tool-annotated methods
         * that will be available to the LLM.
         * <p>
         * This is used for vendor-specific tool annotations like LangChain4j's
         * {@code @Tool} annotation.
         * </p>
         *
         * @param toolObjects
         *            the objects containing tool methods
         * @return this builder
         */
        public Builder withToolObjects(Object... toolObjects) {
            this.toolObjects = toolObjects != null ? toolObjects
                    : new Object[0];
            return this;
        }

        /**
         * Sets an explicit UI instance for thread-safe updates.
         * <p>
         * If not provided, the UI will be derived from configured components or
         * the current UI context.
         * </p>
         *
         * @param ui
         *            the UI instance
         * @return this builder
         */
        public Builder withUI(UI ui) {
            this.ui = ui;
            return this;
        }

        /**
         * Adds a listener that will be notified when the orchestrator completes
         * processing an AI update.
         *
         * @param listener
         *            the listener to add
         * @return this builder
         */
        public Builder addUpdateListener(Consumer<UpdateEvent> listener) {
            if (listener != null) {
                this.updateListeners.add(listener);
            }
            return this;
        }

        /**
         * Builds and returns the configured orchestrator.
         *
         * @return the configured orchestrator
         */
        public AiOrchestrator build() {
            return new AiOrchestrator(this);
        }
    }
}
