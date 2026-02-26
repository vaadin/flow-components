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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.ui.AIFileReceiver;
import com.vaadin.flow.component.ai.ui.AIInput;
import com.vaadin.flow.component.ai.ui.AIMessage;
import com.vaadin.flow.component.ai.ui.AIMessageList;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
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
 *         .withFileReceiver(uploadManager) // optional
 *         .withTools(toolObj) // optional, for @Tool annotations
 *         .withUserName(userName) // optional
 *         .withAssistantName(assistantName) // optional
 *         .withResponseCompleteListener(e -&gt; save(e.getResponse())) // optional
 *         .withHistory(savedHistory, savedAttachments) // optional, for restore
 *         .build();
 * </pre>
 * <p>
 * The orchestrator tracks conversation history internally and delegates to the
 * {@link LLMProvider} for the LLM's working memory. Use {@link #getHistory()}
 * to obtain a snapshot and {@link Builder#withHistory(List, Map)} to restore
 * conversation state (including attachments) across sessions. To persist
 * history automatically after each exchange, use
 * {@link Builder#withResponseCompleteListener(ResponseCompleteListener)}.
 * </p>
 * <p>
 * <b>Serialization:</b> The LLM provider and tool objects are not serialized
 * (they are transient). After deserialization, call
 * {@link #reconnect(LLMProvider)} to restore transient dependencies and replay
 * the conversation history onto the new provider:
 *
 * <pre>
 * orchestrator.reconnect(provider).withTools(toolObj) // optional
 *         .withAttachments(attachmentsByMsgId) // optional
 *         .apply();
 * </pre>
 *
 * The conversation history, UI component bindings, and listeners are preserved
 * across serialization.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class AIOrchestrator implements Serializable {

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

    private transient LLMProvider provider;
    private final String systemPrompt;
    private AIMessageList messageList;
    private AIInput input;
    private AIFileReceiver fileReceiver;
    private transient Object[] tools = new Object[0];
    private String userName;
    private String assistantName;
    private AttachmentSubmitListener attachmentSubmitListener;
    private AttachmentClickListener attachmentClickListener;
    private ResponseCompleteListener responseCompleteListener;
    private final Map<AIMessage, String> itemToMessageId = new HashMap<>();
    private final List<ChatMessage> conversationHistory = new CopyOnWriteArrayList<>();

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
     *             if no UI context is available, or if the orchestrator needs
     *             to be reconnected after deserialization (see
     *             {@link #reconnect(LLMProvider)})
     */
    public void prompt(String userMessage) {
        doPrompt(userMessage);
    }

    /**
     * Returns a {@link Reconnector} to restore transient dependencies after
     * deserialization. The provider is required; tools and attachments are
     * optional.
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
     * orchestrator.reconnect(provider).withTools(toolObj)
     *         .withAttachments(attachmentsByMsgId).apply();
     * </pre>
     * <p>
     * Calling {@link Reconnector#apply()} replays the existing conversation
     * history onto the new provider so it has full context for subsequent
     * prompts. The UI is not modified -- message list, input, and file receiver
     * components retain their state across serialization.
     * </p>
     * <p>
     * This method should only be called on a deserialized instance where the
     * provider is {@code null}.
     * </p>
     *
     * @param provider
     *            the LLM provider to use, not {@code null}
     * @return a reconnector for restoring additional transient dependencies
     * @throws NullPointerException
     *             if provider is {@code null}
     * @throws IllegalStateException
     *             if the orchestrator is already connected (provider is not
     *             {@code null})
     */
    public Reconnector reconnect(LLMProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        if (this.provider != null) {
            throw new IllegalStateException(
                    "AIOrchestrator is already connected. "
                            + "reconnect() should only be called after deserialization.");
        }
        return new Reconnector(this, provider);
    }

    /**
     * Returns the conversation history.
     * <p>
     * The returned list contains all user and assistant messages exchanged
     * through this orchestrator. User messages include a
     * {@link ChatMessage#messageId()} matching the ID provided to the
     * {@link AttachmentSubmitListener}, which can be used to correlate with
     * externally stored attachment data.
     * <p>
     * <b>Note:</b> This method returns a point-in-time snapshot. If a streaming
     * response is in progress, the snapshot may contain the user message
     * without its corresponding assistant response. For automatic persistence,
     * use
     * {@link Builder#withResponseCompleteListener(ResponseCompleteListener)} to
     * be notified at the right time, then call {@code getHistory()} from that
     * callback.
     *
     * @return an unmodifiable copy of the conversation history, never
     *         {@code null}
     */
    public List<ChatMessage> getHistory() {
        return List.copyOf(conversationHistory);
    }

    private void restoreHistory(List<ChatMessage> history,
            Map<String, List<AIAttachment>> attachmentsByMessageId) {
        provider.setHistory(history, attachmentsByMessageId);

        conversationHistory.addAll(history);

        if (messageList != null) {
            for (var message : history) {
                AIMessage aiMessage;
                if (message.role() == ChatMessage.Role.USER) {
                    var attachments = message.messageId() != null
                            ? attachmentsByMessageId.getOrDefault(
                                    message.messageId(),
                                    Collections.emptyList())
                            : Collections.<AIAttachment> emptyList();
                    aiMessage = messageList.addMessage(message.content(),
                            userName, attachments);
                    if (message.messageId() != null) {
                        itemToMessageId.put(aiMessage, message.messageId());
                    }
                } else {
                    aiMessage = messageList.addMessage(message.content(),
                            assistantName, Collections.emptyList());
                }
                if (message.time() != null) {
                    aiMessage.setTime(message.time());
                }
            }
        }
    }

    private AIMessage createAssistantMessagePlaceholder() {
        if (messageList == null) {
            return null;
        }
        return messageList.addMessage("", assistantName,
                Collections.emptyList());
    }

    private void streamResponseToMessage(LLMProvider.LLMRequest request,
            AIMessage assistantMessage, UI ui) {
        var responseBuilder = new StringBuilder();
        var responseStream = provider.stream(request)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS));
        responseStream.doFinally(signal -> {
            isProcessing.set(false);
        }).subscribe(token -> {
            responseBuilder.append(token);
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
        }, () -> {
            var responseText = responseBuilder.toString();
            if (!responseText.isEmpty()) {
                conversationHistory
                        .add(new ChatMessage(ChatMessage.Role.ASSISTANT,
                                responseText, null, Instant.now()));
                fireResponseCompleteListener(responseText);
            }
            LOGGER.debug("LLM streaming completed successfully");
        });
    }

    private void doPrompt(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }
        if (provider == null) {
            throw new IllegalStateException(
                    "AIOrchestrator needs to be reconnected after "
                            + "deserialization. Call reconnect(provider) first.");
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

        var attachments = fileReceiver != null ? fileReceiver.takeAttachments()
                : List.<AIAttachment> of();
        var userAIMessage = messageList == null ? null
                : messageList.addMessage(userMessage, userName, attachments);

        var messageId = UUID.randomUUID().toString();
        conversationHistory.add(new ChatMessage(ChatMessage.Role.USER,
                userMessage, messageId, Instant.now()));
        if (userAIMessage != null) {
            itemToMessageId.put(userAIMessage, messageId);
        }

        if (!attachments.isEmpty() && attachmentSubmitListener != null) {
            var attachmentsCopy = List.copyOf(attachments);
            attachmentSubmitListener.onAttachmentSubmit(
                    new AttachmentSubmitListener.AttachmentSubmitEvent(
                            messageId, attachmentsCopy));
        }

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

    private void fireResponseCompleteListener(String responseText) {
        if (responseCompleteListener != null) {
            try {
                responseCompleteListener.onResponseComplete(
                        new ResponseCompleteListener.ResponseCompleteEvent(
                                responseText));
            } catch (Exception e) {
                LOGGER.error("Error in response complete listener", e);
            }
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

    @Serial
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Initialize tools to empty array
        tools = new Object[0];
    }

    /**
     * Fluent API for restoring transient dependencies on an
     * {@link AIOrchestrator} after deserialization. Obtain an instance via
     * {@link AIOrchestrator#reconnect(LLMProvider)}.
     * <p>
     * The provider is specified when the reconnector is created. Optional
     * transient dependencies (tools and file attachments) can be restored via
     * chained methods before calling {@link #apply()}. The {@code apply()} call
     * replays the existing conversation history onto the new provider but does
     * not modify the UI.
     * </p>
     *
     * <pre>
     * orchestrator.reconnect(provider).withTools(toolObj) // optional
     *         .withAttachments(attachmentsByMsgId) // optional
     *         .apply();
     * </pre>
     */
    public static class Reconnector {
        private final AIOrchestrator orchestrator;
        private final LLMProvider provider;
        private Object[] tools;
        private Map<String, List<AIAttachment>> attachmentsByMessageId;

        private Reconnector(AIOrchestrator orchestrator, LLMProvider provider) {
            this.orchestrator = orchestrator;
            this.provider = provider;
        }

        /**
         * Sets the objects containing vendor-specific tool-annotated methods
         * that will be available to the LLM.
         *
         * @param tools
         *            the objects containing tool methods
         * @return this reconnector
         */
        public Reconnector withTools(Object... tools) {
            this.tools = tools;
            return this;
        }

        /**
         * Sets the file attachments to restore on the new provider's
         * conversation memory. The map is keyed by
         * {@link ChatMessage#messageId()} and contains the list of
         * {@link AIAttachment} objects for each message.
         * <p>
         * Attachments are not stored in the orchestrator's conversation
         * history. If the application persisted attachment data via
         * {@link AttachmentSubmitListener} before serialization, pass it here
         * so the new provider can reconstruct multimodal context. Pass
         * {@link Collections#emptyMap()} (the default) if there are no
         * attachments to restore.
         * <p>
         * The UI is not affected by this method; attachment thumbnails in the
         * message list are preserved across serialization automatically.
         *
         * @param attachmentsByMessageId
         *            a map from message ID to attachment list
         * @return this reconnector
         */
        public Reconnector withAttachments(
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
            this.attachmentsByMessageId = attachmentsByMessageId;
            return this;
        }

        /**
         * Applies the reconnection, restoring the provider, tools, and
         * conversation history on the new provider. The existing conversation
         * history is replayed onto the new provider's memory so that it has
         * full context for subsequent prompts.
         * <p>
         * The UI (message list, input, file receiver) is not modified -- those
         * components survive serialization and retain their state
         * automatically.
         */
        public void apply() {
            orchestrator.isProcessing.set(false);
            orchestrator.provider = provider;
            if (tools != null) {
                orchestrator.tools = tools;
            }
            if (!orchestrator.conversationHistory.isEmpty()) {
                provider.setHistory(
                        List.copyOf(orchestrator.conversationHistory),
                        attachmentsByMessageId == null ? Collections.emptyMap()
                                : attachmentsByMessageId);
            }
        }
    }

    /**
     * Builder for configuring and creating an {@link AIOrchestrator} instance.
     * <p>
     * The builder requires an {@link LLMProvider} and a system prompt. All
     * other settings are optional:
     * </p>
     * <ul>
     * <li>{@link #withInput(AIInput)} or {@link #withInput(MessageInput)} –
     * connects a text input component so that user submissions are
     * automatically forwarded to the LLM. If omitted, use
     * {@link AIOrchestrator#prompt(String)} to send messages
     * programmatically.</li>
     * <li>{@link #withMessageList(AIMessageList)} or
     * {@link #withMessageList(MessageList)} – connects a message list component
     * to display the conversation. If omitted, responses are still streamed but
     * not rendered.</li>
     * <li>{@link #withFileReceiver(AIFileReceiver)},
     * {@link #withFileReceiver(UploadManager)}, or
     * {@link #withFileReceiver(Upload) withFileReceiver(Upload)} – enables file
     * upload support. Uploaded files are sent to the LLM as attachments with
     * the next prompt.</li>
     * <li>{@link #withTools(Object...)} – registers objects containing
     * vendor-specific tool-annotated methods (e.g. LangChain4j's {@code @Tool}
     * or Spring AI's {@code @Tool}) that the LLM can invoke.</li>
     * <li>{@link #withUserName(String)} – sets the display name for user
     * messages (defaults to "You").</li>
     * <li>{@link #withAssistantName(String)} – sets the display name for
     * assistant messages (defaults to "Assistant").</li>
     * <li>{@link #withResponseCompleteListener(ResponseCompleteListener)} –
     * registers a callback that fires after each successful exchange with the
     * assistant's response text, enabling persistence via
     * {@link AIOrchestrator#getHistory()} or follow-up actions.</li>
     * <li>{@link #withHistory(List, Map)} – restores a previously saved
     * conversation history with attachments (from
     * {@link AIOrchestrator#getHistory()}).</li>
     * </ul>
     * <p>
     * Both Flow components ({@link MessageInput}, {@link MessageList},
     * {@link UploadManager}, {@link Upload}) and custom implementations of the
     * AI interfaces ({@link AIInput}, {@link AIMessageList},
     * {@link AIFileReceiver}) are accepted.
     * </p>
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
        private AttachmentSubmitListener attachmentSubmitListener;
        private AttachmentClickListener attachmentClickListener;
        private ResponseCompleteListener responseCompleteListener;
        private List<ChatMessage> history;
        private Map<String, List<AIAttachment>> historyAttachments;

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
         * Sets the file receiver component using a Flow Upload component. The
         * provided upload should not have an UploadHandler or a Receiver set
         * beforehand.
         *
         * @param upload
         *            the Flow Upload component
         * @return this builder
         * @throws IllegalArgumentException
         *             if the {@link Upload} already has an
         *             {@link UploadHandler} or a {@link Receiver}
         */
        public Builder withFileReceiver(Upload upload) {
            if (UploadHelper.hasUploadHandler(upload)) {
                throw new IllegalArgumentException(
                        "The provided Upload already has an UploadHandler.");
            }
            if (upload.getReceiver() != null) {
                throw new IllegalArgumentException(
                        "The provided Upload already has a Receiver.");
            }
            this.fileReceiver = wrapUpload(upload);
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
         * Sets a listener that is called when a message with attachments is
         * submitted to the LLM provider. This allows you to store attachment
         * data in your own storage. The listener receives a unique message ID
         * that can later be used to identify the attachments when they are
         * clicked or when restoring conversation history via
         * {@link #withHistory(List, Map)}.
         *
         * @param listener
         *            the listener to call on attachment submit
         * @return this builder
         */
        public Builder withAttachmentSubmitListener(
                AttachmentSubmitListener listener) {
            this.attachmentSubmitListener = listener;
            return this;
        }

        /**
         * Sets a listener that is called when an attachment in the message list
         * is clicked. The listener receives the message ID and attachment
         * index, allowing you to retrieve attachment data from your own storage
         * using the same message ID provided in
         * {@link AttachmentSubmitListener.AttachmentSubmitEvent#getMessageId()}.
         * <p>
         * Note: This listener requires a message list to be configured via
         * {@link #withMessageList(MessageList)}. If no message list is set, the
         * listener will have no effect.
         *
         * @param listener
         *            the listener to call on attachment click
         * @return this builder
         */
        public Builder withAttachmentClickListener(
                AttachmentClickListener listener) {
            this.attachmentClickListener = listener;
            return this;
        }

        /**
         * Sets a listener that is called after each successful exchange — when
         * the assistant's response has been fully streamed and added to the
         * conversation history. This is the recommended hook for persisting
         * conversation state (via {@link AIOrchestrator#getHistory()}),
         * triggering follow-up actions, or updating UI elements.
         * <p>
         * The listener is called from a background thread (Reactor scheduler).
         * It is safe to perform blocking I/O (e.g. database writes) directly.
         * To update Vaadin UI components from this listener, use
         * {@code ui.access()}.
         * <p>
         * The listener is not called when the LLM response fails, times out, or
         * produces an empty response, nor when history is restored via
         * {@link #withHistory(List, Map)}.
         *
         * @param listener
         *            the listener to call after each successful exchange
         * @return this builder
         */
        public Builder withResponseCompleteListener(
                ResponseCompleteListener listener) {
            this.responseCompleteListener = listener;
            return this;
        }

        /**
         * Sets the conversation history and associated attachments to restore
         * when the orchestrator is built. This restores the LLM provider's
         * conversation context (including multimodal content), the message list
         * UI with attachment thumbnails, and the internal message ID mappings
         * for attachment click handling.
         * <p>
         * The attachment map is keyed by {@link ChatMessage#messageId()} and
         * contains the list of {@link AIAttachment} objects for each message.
         * Messages whose IDs are not in the map are restored as text-only. The
         * attachments are used once during initialization and are not retained
         * by the orchestrator. Pass {@link Collections#emptyMap()} if there are
         * no attachments to restore.
         *
         * @param history
         *            the conversation history to restore, not {@code null}
         * @param attachmentsByMessageId
         *            a map from message ID to attachment list, not {@code null}
         * @return this builder
         */
        public Builder withHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
            Objects.requireNonNull(history, "History must not be null");
            Objects.requireNonNull(attachmentsByMessageId,
                    "Attachments map must not be null");
            this.history = history;
            this.historyAttachments = attachmentsByMessageId;
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
            orchestrator.attachmentSubmitListener = attachmentSubmitListener;
            orchestrator.attachmentClickListener = attachmentClickListener;
            orchestrator.responseCompleteListener = responseCompleteListener;
            if (input != null) {
                input.addSubmitListener(orchestrator::doPrompt);
            }

            if (attachmentClickListener != null && messageList != null) {
                messageList.addAttachmentClickListener((message, attIndex) -> {
                    var messageId = orchestrator.itemToMessageId.get(message);
                    if (messageId != null) {
                        orchestrator.attachmentClickListener.onAttachmentClick(
                                new AttachmentClickListener.AttachmentClickEvent(
                                        messageId, attIndex));
                    }
                });
            }
            if (history != null) {
                orchestrator.restoreHistory(history, historyAttachments);
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

        private static AIFileReceiver wrapUpload(Upload upload) {
            return new UploadWrapper(upload);
        }
    }
}
