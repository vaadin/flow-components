/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.AIComponentsExperimentalFeatureException;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.UploadHelper;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class is a non-visual coordination engine that connects UI components
 * with an LLM provider. It is <b>not</b> a UI component itself and should
 * <b>not</b> be added to a layout or the UI. Instead, add the individual UI
 * components (e.g. {@link MessageInput}, {@link MessageList}) to your layout
 * and pass them to the orchestrator through its {@link Builder}. The
 * orchestrator then wires the components together and manages the LLM
 * interaction behind the scenes.
 * </p>
 * <p>
 * It provides:
 * </p>
 * <ul>
 * <li>LLM integration</li>
 * <li>Component wiring (input, message list, file receiver)</li>
 * <li>Tool execution coordination</li>
 * <li>Programmatic invocation via {@link #prompt(String)}</li>
 * </ul>
 * <p>
 * The orchestrator is configured via a fluent builder:
 * </p>
 *
 * <pre>
 * AIOrchestrator orchestrator = AIOrchestrator
 *         .builder(llmProvider, systemPrompt).withInput(messageInput) // optional
 *         .withMessageList(messageList) // optional
 *         .withFileReceiver(upload) // optional
 *         .withTools(toolObj) // optional, for @Tool annotations
 *         .withUserName(userName) // optional
 *         .withAssistantName(assistantName) // optional
 *         .build();
 * </pre>
 * <p>
 * Conversation history is managed internally by the {@link LLMProvider}
 * instance. Each orchestrator maintains its own conversation context through
 * its provider instance.
 * </p>
 * <p>
 * <b>Note:</b> AIOrchestrator is not serializable. If your application uses
 * session persistence, you will need to create a new orchestrator instance
 * after session restore.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class AIOrchestrator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AIOrchestrator.class);

    /**
     * Default timeout for LLM response streaming in seconds.
     */
    private static final int TIMEOUT_SECONDS = 600;

    /**
     * The feature flag ID for AI components.
     */
    static final String FEATURE_FLAG_ID = AIComponentsFeatureFlagProvider.FEATURE_FLAG_ID;

    private final LLMProvider provider;
    private final String systemPrompt;
    private AIMessageList messageList;
    private AIInput input;
    private AIFileReceiver fileReceiver;
    private final List<AIAttachment> pendingAttachments = new CopyOnWriteArrayList<>();
    private Object[] tools = new Object[0];
    private String userName;
    private String assistantName;

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final AtomicBoolean featureFlagChecked = new AtomicBoolean(false);

    /**
     * Creates a new AI orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     * @param systemPrompt
     *            the system prompt for the LLM (can be null)
     */
    private AIOrchestrator(LLMProvider provider, String systemPrompt) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Creates a new builder for AIOrchestrator with a system prompt.
     *
     * @param provider
     *            the LLM provider
     * @param systemPrompt
     *            the system prompt for the LLM
     * @return a new builder
     */
    public static Builder builder(LLMProvider provider, String systemPrompt) {
        return new Builder(provider, systemPrompt);
    }

    /**
     * Sends a prompt to the AI orchestrator programmatically. This method
     * allows sending prompts without requiring an input component.
     * <p>
     * This is useful for scenarios where you want to trigger AI interaction
     * from button clicks or other UI events without using a message input
     * component.
     * </p>
     * <p>
     * If a request is already being processed, this method will log a warning
     * and return without processing the new prompt.
     * </p>
     *
     * @param userMessage
     *            the prompt to send to the AI
     * @throws IllegalStateException
     *             if no UI context is available
     */
    public void prompt(String userMessage) {
        doPrompt(userMessage);
    }

    private void addUserMessageToList(String userMessage,
            List<AIAttachment> attachments) {
        if (messageList != null) {
            var userItem = messageList.createMessage(userMessage, userName,
                    attachments);
            messageList.addMessage(userItem);
        }
    }

    private AIMessage createAssistantMessagePlaceholder() {
        if (messageList == null) {
            return null;
        }
        var assistantMessage = messageList.createMessage("", assistantName,
                Collections.emptyList());
        messageList.addMessage(assistantMessage);
        return assistantMessage;
    }

    private void streamResponseToMessage(LLMProvider.LLMRequest request,
            AIMessage assistantMessage, UI ui) {
        var responseStream = provider.stream(request)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS));
        responseStream.doFinally(signal -> {
            isProcessing.set(false);
        }).subscribe(token -> {
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> assistantMessage.appendText(token));
            }
        }, error -> {
            String userMessage;
            if (error instanceof TimeoutException) {
                userMessage = "Request timed out. Please try again.";
                LOGGER.warn("LLM request timed out after {} seconds",
                        TIMEOUT_SECONDS);
            } else {
                userMessage = "An error occurred. Please try again.";
                LOGGER.error("Error during LLM streaming", error);
            }
            if (assistantMessage != null && messageList != null) {
                ui.access(() -> assistantMessage.setText(userMessage));
            }
        }, () -> LOGGER.debug("LLM streaming completed successfully"));
    }

    private void doPrompt(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }
        if (!isProcessing.compareAndSet(false, true)) {
            LOGGER.warn(
                    "Ignoring prompt: another request is already in progress");
            return;
        }
        processUserInput(userMessage);
    }

    private void processUserInput(String userMessage) {
        var ui = UI.getCurrentOrThrow();
        checkFeatureFlag(ui);
        var attachments = pendingAttachments.stream().toList();
        addUserMessageToList(userMessage, attachments);
        clearPendingAttachments(ui);
        var assistantMessage = createAssistantMessagePlaceholder();
        String effectiveSystemPrompt = null;
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            effectiveSystemPrompt = systemPrompt.trim();
        }
        final var finalSystemPrompt = effectiveSystemPrompt;
        var request = new LLMProvider.LLMRequest() {

            @Override
            public String userMessage() {
                return userMessage;
            }

            @Override
            public List<AIAttachment> attachments() {
                return attachments;
            }

            @Override
            public String systemPrompt() {
                return finalSystemPrompt;
            }

            @Override
            public Object[] tools() {
                return tools;
            }
        };
        LOGGER.debug("Processing prompt with {} attachments",
                attachments.size());
        streamResponseToMessage(request, assistantMessage, ui);
    }

    private void clearPendingAttachments(UI ui) {
        pendingAttachments.clear();
        if (fileReceiver != null) {
            ui.access(() -> fileReceiver.clearFileList());
        }
    }

    private void checkFeatureFlag(UI ui) {
        if (featureFlagChecked.get()) {
            return;
        }
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        if (!featureFlags.isEnabled(FEATURE_FLAG_ID)) {
            throw new AIComponentsExperimentalFeatureException(
                    "AIOrchestrator");
        }
        featureFlagChecked.set(true);
    }

    private void configureFileReceiver() {
        if (fileReceiver == null) {
            return;
        }
        fileReceiver.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            var isDuplicate = pendingAttachments.stream()
                    .anyMatch(a -> a.name().equals(meta.fileName()));
            if (isDuplicate) {
                throw new IllegalArgumentException(
                        "Duplicate file name: " + meta.fileName());
            }
            pendingAttachments.add(new AIAttachment(meta.fileName(),
                    meta.contentType(), data));
            LOGGER.debug("Added attachment: {}", meta.fileName());
        }));
        fileReceiver.addFileRemovedListener(fileName -> {
            var removed = pendingAttachments
                    .removeIf(a -> a.name().equals(fileName));
            if (removed) {
                LOGGER.debug("Removed attachment: {}", fileName);
            }
        });
    }

    /**
     * Builder for AIOrchestrator.
     */
    public static class Builder {
        private final LLMProvider provider;
        private final String systemPrompt;
        private AIMessageList messageList;
        private AIInput input;
        private AIFileReceiver fileReceiver;
        private Object[] tools = new Object[0];
        private String userName;
        private String assistantName;

        private Builder(LLMProvider provider, String systemPrompt) {
            Objects.requireNonNull(provider, "Provider cannot be null");
            this.provider = provider;
            this.systemPrompt = systemPrompt;
        }

        /**
         * Sets the message list component.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public Builder withMessageList(AIMessageList messageList) {
            this.messageList = messageList;
            return this;
        }

        /**
         * Sets the message list component using a Flow MessageList component.
         *
         * @param messageList
         *            the Flow MessageList component
         * @return this builder
         */
        public Builder withMessageList(MessageList messageList) {
            this.messageList = wrapMessageList(messageList);
            return this;
        }

        /**
         * Sets the input component.
         *
         * @param input
         *            the input component
         * @return this builder
         */
        public Builder withInput(AIInput input) {
            this.input = input;
            return this;
        }

        /**
         * Sets the input component using a Flow MessageInput component.
         *
         * @param messageInput
         *            the Flow MessageInput component
         * @return this builder
         */
        public Builder withInput(MessageInput messageInput) {
            this.input = wrapInput(messageInput);
            return this;
        }

        /**
         * Sets the file receiver component for file uploads.
         *
         * @param fileReceiver
         *            the file receiver
         * @return this builder
         */
        public Builder withFileReceiver(AIFileReceiver fileReceiver) {
            this.fileReceiver = fileReceiver;
            return this;
        }

        /**
         * Sets the file receiver component using a Flow UploadManager. The
         * provided manager should not have an UploadHandler set beforehand.
         *
         * @param uploadManager
         *            the Flow UploadManager
         * @return this builder
         * @throws IllegalArgumentException
         *             if the {@link UploadManager} already has an
         *             {@link UploadHandler}
         */
        public Builder withFileReceiver(UploadManager uploadManager) {
            if (uploadManager != null
                    && UploadHelper.hasUploadHandler(uploadManager)) {
                throw new IllegalArgumentException(
                        "The provided UploadManager already has an UploadHandler.");
            }
            this.fileReceiver = wrapUploadManager(uploadManager);
            return this;
        }

        /**
         * Sets the objects containing vendor-specific tool-annotated methods
         * that will be available to the LLM.
         * <p>
         * For LangChain4j, use {@code @dev.langchain4j.agent.tool.Tool}. For
         * Spring AI, use {@code @org.springframework.ai.tool.annotation.Tool}.
         * </p>
         *
         * @param tools
         *            the objects containing tool methods
         * @return this builder
         */
        public Builder withTools(Object... tools) {
            this.tools = tools != null ? tools : new Object[0];
            return this;
        }

        /**
         * Sets the display name for user messages in the message list.
         * <p>
         * This name is shown as the author of messages sent by the user. If not
         * set, defaults to "You".
         * </p>
         *
         * @param userName
         *            the display name for user messages, not {@code null}
         * @return this builder
         */
        public Builder withUserName(String userName) {
            Objects.requireNonNull(userName, "User name cannot be null");
            this.userName = userName;
            return this;
        }

        /**
         * Sets the display name for AI assistant messages in the message list.
         * <p>
         * This name is shown as the author of messages generated by the AI. If
         * not set, defaults to "Assistant".
         * </p>
         *
         * @param assistantName
         *            the display name for AI messages, not {@code null}
         * @return this builder
         */
        public Builder withAssistantName(String assistantName) {
            Objects.requireNonNull(assistantName,
                    "Assistant name cannot be null");
            this.assistantName = assistantName;
            return this;
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public AIOrchestrator build() {
            var orchestrator = new AIOrchestrator(provider, systemPrompt);
            orchestrator.messageList = messageList;
            orchestrator.input = input;
            orchestrator.fileReceiver = fileReceiver;
            orchestrator.tools = tools == null ? new Object[0] : tools;
            orchestrator.userName = userName == null ? "You" : userName;
            orchestrator.assistantName = assistantName == null ? "Assistant"
                    : assistantName;
            if (input != null) {
                input.addSubmitListener(
                        e -> orchestrator.doPrompt(e.getValue()));
            }
            if (fileReceiver != null) {
                orchestrator.configureFileReceiver();
            }
            LOGGER.debug("Built AIOrchestrator with messageList={}, input={}, "
                    + "fileReceiver={}, tools={}, userName={}, assistantName={}",
                    orchestrator.messageList != null,
                    orchestrator.input != null,
                    orchestrator.fileReceiver != null,
                    orchestrator.tools.length, orchestrator.userName,
                    orchestrator.assistantName);

            return orchestrator;
        }

        private static AIMessageList wrapMessageList(MessageList messageList) {
            return new MessageListWrapper(messageList);
        }

        private static AIInput wrapInput(MessageInput messageInput) {
            return new MessageInputWrapper(messageInput);
        }

        private static AIFileReceiver wrapUploadManager(
                UploadManager uploadManager) {
            return new UploadManagerWrapper(uploadManager);
        }
    }
}
