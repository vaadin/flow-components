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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AiAttachment;
import com.vaadin.flow.component.ai.component.AiFileReceiver;
import com.vaadin.flow.component.ai.component.AiInput;
import com.vaadin.flow.component.ai.component.AiMessage;
import com.vaadin.flow.component.ai.component.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Orchestrator for AI-powered chat interfaces.
 * <p>
 * This class is a generic coordination engine that connects UI components with
 * an LLM provider. It provides:
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
 * AiOrchestrator orchestrator = AiOrchestrator
 *         .builder(llmProvider, systemPrompt).withInput(messageInput) // optional
 *         .withMessageList(messageList) // optional
 *         .withFileReceiver(upload) // optional
 *         .withTools(toolObj) // optional, for @Tool annotations
 *         .withUserName(userName) // optional
 *         .withAiName(aiName) // optional
 *         .build();
 * </pre>
 * <p>
 * Conversation history is managed internally by the {@link LLMProvider}
 * instance. Each orchestrator maintains its own conversation context through
 * its provider instance.
 * </p>
 * <p>
 * <b>Note:</b> AiOrchestrator is not serializable. If your application uses
 * session persistence, you will need to create a new orchestrator instance
 * after session restore.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class AiOrchestrator {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AiOrchestrator.class);

    /**
     * Default timeout for LLM response streaming in seconds.
     */
    private static final int TIMEOUT_SECONDS = 600;

    private final LLMProvider provider;
    private final String systemPrompt;
    private AiMessageList messageList;
    private AiInput input;
    private AiFileReceiver fileReceiver;
    private final List<AiAttachment> pendingAttachments = new CopyOnWriteArrayList<>();
    private Object[] tools = new Object[0];
    private String userName;
    private String aiName;

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    /**
     * Creates a new AI orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     * @param systemPrompt
     *            the system prompt for the LLM (can be null)
     */
    private AiOrchestrator(LLMProvider provider, String systemPrompt) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Creates a new builder for AiOrchestrator.
     *
     * @param provider
     *            the LLM provider
     * @return a new builder
     */
    public static Builder builder(LLMProvider provider) {
        return new Builder(provider, null);
    }

    /**
     * Creates a new builder for AiOrchestrator with a system prompt.
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
            List<AiAttachment> attachments) {
        if (messageList != null) {
            var userItem = messageList.createMessage(userMessage, userName,
                    attachments);
            messageList.addMessage(userItem);
        }
    }

    private AiMessage createAssistantMessagePlaceholder() {
        if (messageList == null) {
            return null;
        }
        var assistantMessage = messageList.createMessage("", aiName,
                Collections.emptyList());
        messageList.addMessage(assistantMessage);
        return assistantMessage;
    }

    private void streamResponseToMessage(LLMProvider.LLMRequest request,
            AiMessage assistantMessage, UI ui) {
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
            public List<AiAttachment> attachments() {
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
            pendingAttachments.add(new AiAttachment(meta.fileName(),
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
     * Builder for AiOrchestrator.
     */
    public static class Builder {
        private final LLMProvider provider;
        private final String systemPrompt;
        private AiMessageList messageList;
        private AiInput input;
        private AiFileReceiver fileReceiver;
        private Object[] tools = new Object[0];
        private String userName;
        private String aiName;

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
        public Builder withMessageList(AiMessageList messageList) {
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
        public Builder withInput(AiInput input) {
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
        public Builder withFileReceiver(AiFileReceiver fileReceiver) {
            this.fileReceiver = fileReceiver;
            return this;
        }

        /**
         * Sets the file receiver component using a Flow UploadManager
         * component.
         *
         * @param uploadManager
         *            the Flow UploadManager component
         * @return this builder
         */
        public Builder withFileReceiver(UploadManager uploadManager) {
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
         * @param aiName
         *            the display name for AI messages, not {@code null}
         * @return this builder
         */
        public Builder withAiName(String aiName) {
            Objects.requireNonNull(aiName, "AI name cannot be null");
            this.aiName = aiName;
            return this;
        }

        /**
         * Builds the orchestrator.
         *
         * @return the configured orchestrator
         */
        public AiOrchestrator build() {
            var orchestrator = new AiOrchestrator(provider, systemPrompt);
            orchestrator.messageList = messageList;
            orchestrator.input = input;
            orchestrator.fileReceiver = fileReceiver;
            orchestrator.tools = tools == null ? new Object[0] : tools;
            orchestrator.userName = userName == null ? "You" : userName;
            orchestrator.aiName = aiName == null ? "Assistant" : aiName;
            if (input != null) {
                input.addSubmitListener(
                        e -> orchestrator.doPrompt(e.getValue()));
            }
            if (fileReceiver != null) {
                orchestrator.configureFileReceiver();
            }
            LOGGER.debug("Built AiOrchestrator with messageList={}, input={}, "
                    + "fileReceiver={}, tools={}, userName={}, aiName={}",
                    orchestrator.messageList != null,
                    orchestrator.input != null,
                    orchestrator.fileReceiver != null,
                    orchestrator.tools.length, orchestrator.userName,
                    orchestrator.aiName);

            return orchestrator;
        }

        private static AiMessageList wrapMessageList(MessageList messageList) {
            return new MessageListWrapper(messageList);
        }

        private static AiInput wrapInput(MessageInput messageInput) {
            return new MessageInputWrapper(messageInput);
        }

        private static AiFileReceiver wrapUploadManager(
                UploadManager uploadManager) {
            return new UploadManagerWrapper(uploadManager);
        }
    }
}
