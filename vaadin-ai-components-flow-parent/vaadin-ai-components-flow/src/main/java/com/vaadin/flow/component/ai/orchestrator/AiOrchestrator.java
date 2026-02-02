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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.component.AiFileReceiver;
import com.vaadin.flow.component.ai.component.AiInput;
import com.vaadin.flow.component.ai.component.AiMessage;
import com.vaadin.flow.component.ai.component.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
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
 * <li>UI-safe updates via {@code UI.access()}</li>
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
 *         .build();
 * </pre>
 * <p>
 * Conversation history is managed internally by the {@link LLMProvider}
 * instance. Each orchestrator maintains its own conversation context through
 * its provider instance.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class AiOrchestrator implements Serializable {

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
    private final List<LLMProvider.Attachment> pendingAttachments = new CopyOnWriteArrayList<>();
    private transient Object[] tools = new Object[0];
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
     * Gets the message list component.
     *
     * @return the message list component, or null if not set
     */
    public AiMessageList getMessageList() {
        return messageList;
    }

    /**
     * Gets the file receiver component.
     *
     * @return the file receiver component, or null if not set
     */
    public AiFileReceiver getFileReceiver() {
        return fileReceiver;
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

    /**
     * Returns the system prompt for the LLM.
     *
     * @return the system prompt, or null if no system prompt was configured
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    private void addUserMessageToList(String userMessage) {
        if (messageList != null) {
            var userItem = messageList.createMessage(userMessage, userName);
            messageList.addMessage(userItem);
        }
    }

    private AiMessage createAssistantMessagePlaceholder() {
        if (messageList == null) {
            return null;
        }
        var assistantMessage = messageList.createMessage("", aiName);
        messageList.addMessage(assistantMessage);
        return assistantMessage;
    }

    private void streamResponseToMessage(LLMProvider.LLMRequest request,
            AiMessage assistantMessage, UI ui) {
        var responseStream = provider.stream(request)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS));
        responseStream.doFinally(signal -> isProcessing.set(false))
                .subscribe(token -> {
                    if (assistantMessage != null && messageList != null) {
                        ui.access(() -> assistantMessage.appendText(token));
                    }
                }, error -> {
                    var errorMessage = error.getMessage();
                    if (error instanceof TimeoutException) {
                        errorMessage = "Request timed out after "
                                + TIMEOUT_SECONDS + " seconds";
                        LOGGER.warn("LLM request timed out after {} seconds",
                                TIMEOUT_SECONDS);
                    } else {
                        LOGGER.error("Error during LLM streaming", error);
                    }
                    if (assistantMessage != null && messageList != null) {
                        var finalErrorMessage = errorMessage;
                        ui.access(() -> assistantMessage
                                .setText("Error: " + finalErrorMessage));
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
        addUserMessageToList(userMessage);
        var assistantMessage = createAssistantMessagePlaceholder();
        String effectiveSystemPrompt = null;
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            effectiveSystemPrompt = systemPrompt.trim();
        }
        final var finalSystemPrompt = effectiveSystemPrompt;
        var attachments = pendingAttachments.stream().toList();
        var request = new LLMProvider.LLMRequest() {

            @Override
            public String userMessage() {
                return userMessage;
            }

            @Override
            public List<LLMProvider.Attachment> attachments() {
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
        clearPendingAttachments();
        LOGGER.debug("Processing prompt with {} attachments",
                attachments.size());
        streamResponseToMessage(request, assistantMessage, ui);
    }

    private void clearPendingAttachments() {
        pendingAttachments.clear();
        if (fileReceiver != null) {
            UI.getCurrent().access(() -> fileReceiver.clearFileList());
        }
    }

    private void configureFileReceiver() {
        if (fileReceiver == null) {
            return;
        }
        fileReceiver.setUploadHandler(UploadHandler.inMemory((meta, data) -> {
            var isDuplicate = pendingAttachments.stream()
                    .anyMatch(a -> a.fileName().equals(meta.fileName()));
            if (isDuplicate) {
                throw new IllegalArgumentException(
                        "Duplicate file name: " + meta.fileName());
            }
            pendingAttachments.add(new LLMProvider.Attachment() {
                @Override
                public String fileName() {
                    return meta.fileName();
                }

                @Override
                public String contentType() {
                    return meta.contentType();
                }

                @Override
                public byte[] data() {
                    return data;
                }
            });
            LOGGER.debug("Added attachment: {}", meta.fileName());
        }));
        fileReceiver.addFileRemovedListener(fileName -> {
            var removed = pendingAttachments
                    .removeIf(a -> a.fileName().equals(fileName));
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
         * If neither a message list nor an input component is configured, a
         * warning will be logged as the orchestrator will have limited
         * functionality.
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
            return new AiMessageList() {
                @Override
                public void addMessage(AiMessage message) {
                    if (message instanceof MessageListItemWrapper messageListItemWrapper) {
                        messageList.addItem(messageListItemWrapper.getItem());
                    } else {
                        var item = new MessageListItem();
                        item.setText(message.getText());
                        item.setTime(message.getTime());
                        item.setUserName(message.getUserName());
                        messageList.addItem(item);
                    }
                }

                @Override
                public AiMessage createMessage(String text, String userName) {
                    return new MessageListItemWrapper(text, userName);
                }
            };
        }

        private static AiInput wrapInput(MessageInput messageInput) {
            return listener -> messageInput.addSubmitListener(
                    event -> listener.onSubmit(event::getValue));
        }

        private static AiFileReceiver wrapUploadManager(
                UploadManager uploadManager) {
            return new AiFileReceiver() {
                @Override
                public void setUploadHandler(UploadHandler uploadHandler) {
                    uploadManager.setUploadHandler(uploadHandler);
                }

                @Override
                public void addFileRemovedListener(Consumer<String> listener) {
                    uploadManager.addFileRemovedListener(
                            event -> listener.accept(event.getFileName()));
                }

                @Override
                public void clearFileList() {
                    uploadManager.clearFileList();
                }
            };
        }

        private static class MessageListItemWrapper implements AiMessage {
            private final MessageListItem item;

            public MessageListItemWrapper(String text, String userName) {
                item = new MessageListItem(text, Instant.now(), userName);
            }

            public MessageListItem getItem() {
                return item;
            }

            @Override
            public String getText() {
                return item.getText();
            }

            @Override
            public void setText(String text) {
                item.setText(text);
            }

            @Override
            public Instant getTime() {
                return item.getTime();
            }

            @Override
            public String getUserName() {
                return item.getUserName();
            }

            @Override
            public void appendText(String token) {
                item.appendText(token);
            }
        }
    }
}
