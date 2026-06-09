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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.server.streams.UploadHandler;

import tools.jackson.databind.JsonNode;

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
 * <li>Controller-based feature composition via {@link AIController}</li>
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
 *         .withResponseListener(e -&gt; save(e.getResponse())) // optional
 *         .withHistory(savedHistory, savedAttachments) // optional, for restore
 *         .build();
 * </pre>
 * <p>
 * The orchestrator tracks conversation history internally and delegates to the
 * {@link LLMProvider} for the LLM's working memory. Use {@link #getHistory()}
 * to obtain a snapshot and {@link Builder#withHistory(List, Map)} to restore
 * conversation state (including attachments) across sessions. To persist
 * history automatically after each exchange, use
 * {@link Builder#withResponseListener(ResponseListener)}.
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

    /**
     * Pattern for valid tool names. Popular LLM APIs (OpenAI, Anthropic, etc.)
     * require tool names to contain only alphanumeric characters, underscores,
     * and hyphens, with a maximum length of 64 characters.
     */
    private static final Pattern VALID_TOOL_NAME_PATTERN = Pattern
            .compile("^[a-zA-Z0-9_-]{1,64}$");

    private static final Set<Object> CLAIMED_INSTANCES = Collections
            .synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static void claim(Object instance) {
        if (instance == null) {
            return;
        }
        if (!CLAIMED_INSTANCES.add(instance)) {
            throw new IllegalStateException(instance.getClass().getSimpleName()
                    + " is already in use by another AIOrchestrator. "
                    + "Each instance can only be used by a single "
                    + "orchestrator.");
        }
    }

    private static void unclaim(Object instance) {
        if (instance != null) {
            CLAIMED_INSTANCES.remove(instance);
        }
    }

    /**
     * Name of the built-in tool that exposes per-turn session context to the
     * LLM. Reserved — applications should not register their own tool with this
     * name.
     */
    static final String SESSION_CONTEXT_TOOL_NAME = "get_session_context";

    private transient LLMProvider provider;
    private final String systemPrompt;
    private AIMessageList messageList;
    private AIInput input;
    private AIFileReceiver fileReceiver;
    private transient Object[] tools;
    private transient AIController controller;
    private String userName;
    private String assistantName;
    private RequestListener requestListener;
    private AttachmentClickListener attachmentClickListener;
    private ResponseListener responseListener;
    private SerializableSupplier<String> contextSupplier;
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
     * <p>
     * A system prompt is strongly recommended — without one, the LLM has no
     * guidance beyond tool descriptions and may behave inconsistently.
     * </p>
     *
     * @param provider
     *            the LLM provider
     * @param systemPrompt
     *            the system prompt for the LLM, or {@code null} to omit
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
     * Attachments are taken from a configured {@link AIFileReceiver} (if any).
     * To send a prompt with caller-supplied attachments, use
     * {@link #prompt(String, List)}.
     * </p>
     * <p>
     * If a request is already being processed, this method logs a warning and
     * returns without processing the new prompt.
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
        doPrompt(userMessage, null);
    }

    /**
     * Sends a prompt with caller-supplied attachments. Useful for programmatic
     * flows where attachments are produced server-side — generated files,
     * fetched data, content from non-Upload sources — rather than uploaded
     * through a UI component.
     * <p>
     * Behaves like {@link #prompt(String)} otherwise: the message and
     * attachments appear in the message list, the request listener fires with
     * the {@code messageId} also recorded in the conversation history, and the
     * attachments are forwarded to the {@link LLMProvider.LLMRequest}.
     * </p>
     * <p>
     * <b>Interaction with a configured {@link AIFileReceiver}:</b> this
     * overload uses only the supplied list and does <b>not</b> drain the
     * receiver. Attachments pending in the receiver stay there for the next
     * call to {@link #prompt(String)} (or the user's next submit through a
     * connected input). To merge UI-driven and programmatic attachments, the
     * caller must drain the receiver explicitly and pass the combined list.
     * </p>
     * <p>
     * If a request is already being processed, this method logs a warning and
     * returns without processing the new prompt.
     * </p>
     *
     * @param userMessage
     *            the prompt to send to the AI
     * @param attachments
     *            the attachments to send with the prompt; pass an empty list
     *            for none. Copied defensively — subsequent mutations of the
     *            caller's list have no effect.
     * @throws NullPointerException
     *             if {@code attachments} is {@code null} or contains
     *             {@code null} elements
     * @throws IllegalStateException
     *             if no UI context is available, or if the orchestrator needs
     *             to be reconnected after deserialization (see
     *             {@link #reconnect(LLMProvider)})
     */
    public void prompt(String userMessage, List<AIAttachment> attachments) {
        Objects.requireNonNull(attachments, "attachments cannot be null");
        doPrompt(userMessage, List.copyOf(attachments));
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
     * {@link ChatMessage#messageId()} matching the id provided to the
     * {@link RequestListener}, which can be used to correlate with externally
     * stored attachment data.
     * <p>
     * <b>Note:</b> This method returns a point-in-time snapshot. If a streaming
     * response is in progress, the snapshot may contain the user message
     * without its corresponding assistant response. For automatic persistence,
     * use {@link Builder#withResponseListener(ResponseListener)} to be notified
     * at the right time, then call {@code getHistory()} from that callback.
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
            fireResponseListener("", error, ui);
        }, () -> {
            var responseText = responseBuilder.toString();
            if (!responseText.isEmpty()) {
                conversationHistory
                        .add(new ChatMessage(ChatMessage.Role.ASSISTANT,
                                responseText, null, Instant.now()));
            }
            fireResponseListener(responseText, null, ui);
            LOGGER.debug("LLM streaming completed successfully");
        });
    }

    private void doPrompt(String userMessage,
            List<AIAttachment> explicitAttachments) {
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
        try {
            processUserInput(userMessage, explicitAttachments);
        } catch (Throwable t) { // NOSONAR — Throwable for cleanup-then-rethrow
            // Reset the flag before firing the hook so a controller can
            // retry from onResponse, matching the async paths where
            // doFinally clears the flag before the hook runs. Catching
            // Throwable rather than Exception covers Error subtypes (OOM,
            // AssertionError) — otherwise the flag would stay stuck and
            // every later prompt would be dropped. Firing the hook for
            // any Throwable mirrors the async onError consumer.
            isProcessing.set(false);
            // null only if UI.getCurrentOrThrow inside processUserInput
            // failed first — in which case nothing past it executed.
            var currentUi = UI.getCurrent();
            if (currentUi != null) {
                fireResponseListener("", t, currentUi);
            }
            throw t;
        }
    }

    private void processUserInput(String userMessage,
            List<AIAttachment> explicitAttachments) {
        var ui = UI.getCurrentOrThrow();
        checkFeatureFlag(ui);

        var attachments = getAttachmentsToProcess(explicitAttachments);
        var userAIMessage = messageList == null ? null
                : messageList.addMessage(userMessage, userName, attachments);
        var assistantMessage = createAssistantMessagePlaceholder();

        try {
            if (controller != null) {
                controller.onRequest();
            }

            var request = buildRequest(userMessage, attachments);
            LOGGER.debug("Processing prompt with {} attachments",
                    attachments.size());

            var messageId = UUID.randomUUID().toString();
            if (requestListener != null) {
                // Fires on every prompt with the user message, assigned
                // messageId, and attachments (empty list when none) — the
                // generic "request being submitted" hook for listener users,
                // counterpart to AIController.onRequest().
                requestListener.onRequest(new RequestListener.RequestEvent(
                        userMessage, messageId, List.copyOf(attachments)));
            }
            conversationHistory.add(new ChatMessage(ChatMessage.Role.USER,
                    userMessage, messageId, Instant.now()));
            if (userAIMessage != null) {
                itemToMessageId.put(userAIMessage, messageId);
            }

            streamResponseToMessage(request, assistantMessage, ui);
        } catch (Throwable t) { // NOSONAR — Throwable to surface UI on any
                                // throw
            // Single update — stream errors are async and never reach this
            // catch; onResponse throws are handled inside
            // fireResponseListener (which appends rather than rewrites).
            if (assistantMessage != null) {
                assistantMessage
                        .setText("An error occurred. Please try again.");
            }
            throw t;
        }
    }

    private List<AIAttachment> getAttachmentsToProcess(
            List<AIAttachment> explicitAttachments) {
        // Explicit list wins; the receiver buffer stays untouched in that
        // case (see prompt(String, List) JavaDoc).
        if (explicitAttachments != null) {
            return explicitAttachments;
        }
        if (fileReceiver != null) {
            return fileReceiver.takeAttachments();
        }
        return List.of();
    }

    private LLMProvider.LLMRequest buildRequest(String userMessage,
            List<AIAttachment> attachments) {
        final var effectiveSystemPrompt = systemPrompt != null
                && !systemPrompt.isBlank() ? systemPrompt.trim() : null;
        var controllerTools = controller != null
                && controller.getTools() != null ? controller.getTools()
                        : List.<LLMProvider.ToolSpec> of();
        final var explicitTools = mergeWithContextTool(controllerTools);
        return new LLMProvider.LLMRequest() {

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
                return effectiveSystemPrompt;
            }

            @Override
            public Object[] tools() {
                return tools == null ? new Object[0] : tools;
            }

            @Override
            public List<LLMProvider.ToolSpec> explicitTools() {
                return explicitTools;
            }
        };
    }

    /**
     * Resolves the configured {@link Supplier} for session context and, if it
     * returns non-empty content, prepends a {@value #SESSION_CONTEXT_TOOL_NAME}
     * tool that carries that content in its description. The resolved string is
     * captured in the per-turn tool instance so {@code execute()} can return it
     * without re-invoking the supplier off the UI thread.
     * <p>
     * A supplier that throws aborts the turn — the exception propagates through
     * {@link #buildRequest} and is handled by the existing error path in
     * {@link #processUserInput}.
     */
    private List<LLMProvider.ToolSpec> mergeWithContextTool(
            List<LLMProvider.ToolSpec> controllerTools) {
        if (contextSupplier == null) {
            return controllerTools;
        }
        var resolved = contextSupplier.get();
        if (resolved == null || resolved.isBlank()) {
            return controllerTools;
        }
        var contextTool = buildSessionContextTool(resolved);
        var merged = new ArrayList<LLMProvider.ToolSpec>(
                controllerTools.size() + 1);
        merged.add(contextTool);
        merged.addAll(controllerTools);
        return List.copyOf(merged);
    }

    /**
     * Builds the per-turn {@value #SESSION_CONTEXT_TOOL_NAME} tool. The
     * resolved content is baked into the description so the LLM sees it just
     * from listing the available tools — no separate call is normally needed.
     * {@link LLMProvider.ToolSpec#execute} returns the same content so a model
     * that does call the tool gets exactly what the description already
     * carries.
     */
    private static LLMProvider.ToolSpec buildSessionContextTool(
            String content) {
        return new SessionContextTool(content);
    }

    private static final DateTimeFormatter DEFAULT_CONTEXT_DATE_TIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mmXXX");

    /**
     * Default supplier installed when {@link Builder#withMetadata} has not been
     * called. Renders the server clock as e.g.
     * {@code "Current server date and time: 2026-05-28T17:42+03:00 (Friday, Europe/Helsinki)"}
     * so the LLM can interpret relative date references without guessing.
     */
    private static SerializableSupplier<String> defaultContextSupplier() {
        return () -> {
            var now = ZonedDateTime.now();
            var dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL,
                    Locale.ENGLISH);
            return String.format("Current server date and time: %s (%s, %s)",
                    now.format(DEFAULT_CONTEXT_DATE_TIME_FORMAT), dayOfWeek,
                    now.getZone().getId());
        };
    }

    /**
     * Per-turn {@link LLMProvider.ToolSpec} that surfaces the resolved session
     * context. {@link Serializable} so the orchestrator's per-turn explicit
     * tools list does not break the serialization round-trip test even though
     * tools themselves are rebuilt on every turn.
     */
    private static final class SessionContextTool
            implements LLMProvider.ToolSpec, Serializable {
        private final String content;
        private final String description;

        SessionContextTool(String content) {
            this.content = content;
            this.description = """
                    Read for current session context (e.g. date and time, user \
                    locale). The content below is captured at the start of \
                    this turn:

                    """ + content;
        }

        @Override
        public String getName() {
            return SESSION_CONTEXT_TOOL_NAME;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getParametersSchema() {
            return null;
        }

        @Override
        public String execute(JsonNode arguments) {
            return content;
        }
    }

    private void fireResponseListener(String responseText, Throwable error,
            UI ui) {
        if (responseListener != null) {
            try {
                responseListener.onResponse(new ResponseListener.ResponseEvent(
                        responseText, error));
            } catch (Exception e) {
                LOGGER.error("Error in response listener", e);
            }
        }
        if (controller != null) {
            ui.access(() -> {
                try {
                    controller.onResponse(error);
                } catch (Exception e) {
                    LOGGER.error("Error in controller onResponse", e);
                    // Append a separate assistant message instead of
                    // rewriting the LLM's response. By the time this
                    // runs, the response is already in the provider's
                    // chat memory and in our history; rewriting either
                    // would misrepresent what the LLM actually said.
                    // Only on the success path — the failure path already
                    // rewrote the placeholder to a generic error message.
                    if (error == null && messageList != null) {
                        messageList.addMessage(
                                "An error occurred. Please try again.",
                                assistantName, Collections.emptyList());
                    }
                }
            });
        }
    }

    private static void validateToolNames(List<LLMProvider.ToolSpec> tools) {
        var seen = new HashSet<String>();
        for (var tool : tools) {
            var name = tool.getName();
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException(
                        "Tool name must not be null or empty.");
            }
            if (!VALID_TOOL_NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException(
                        "Tool name '" + name + "' is not valid. "
                                + "Tool names must contain only alphanumeric "
                                + "characters, underscores, and hyphens, "
                                + "with a maximum length of 64 characters "
                                + "(pattern: "
                                + VALID_TOOL_NAME_PATTERN.pattern() + ").");
            }
            if (SESSION_CONTEXT_TOOL_NAME.equals(name)) {
                LOGGER.warn(
                        "Tool name '{}' is reserved for the built-in session context tool",
                        name);
            }
            if (!seen.add(name)) {
                LOGGER.warn(
                        "Duplicate tool name '{}': previous tool will be replaced",
                        name);
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

    /**
     * Fluent API for restoring transient dependencies on an
     * {@link AIOrchestrator} after deserialization. Obtain an instance via
     * {@link AIOrchestrator#reconnect(LLMProvider)}.
     * <p>
     * The provider is specified when the reconnector is created. Optional
     * transient dependencies (tools, controller, and file attachments) can be
     * restored via chained methods before calling {@link #apply()}. The
     * {@code apply()} call replays the existing conversation history onto the
     * new provider but does not modify the UI.
     * </p>
     *
     * <pre>
     * orchestrator.reconnect(provider).withTools(toolObj) // optional
     *         .withController(controller) // optional
     *         .withAttachments(attachmentsByMsgId) // optional
     *         .apply();
     * </pre>
     */
    public static class Reconnector {
        private final AIOrchestrator orchestrator;
        private final LLMProvider provider;
        private Object[] tools;
        private AIController controller;
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
         * Sets the controller to use after reconnection.
         *
         * @param controller
         *            the controller to use, not {@code null}
         * @return this reconnector
         * @throws IllegalArgumentException
         *             if any tool name is invalid
         */
        public Reconnector withController(AIController controller) {
            Objects.requireNonNull(controller, "Controller cannot be null");
            if (controller.getTools() != null) {
                validateToolNames(controller.getTools());
            }
            this.controller = controller;
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
         * {@link RequestListener} before serialization, pass it here so the new
         * provider can reconstruct multimodal context. Pass
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
            orchestrator.tools = tools;
            orchestrator.controller = controller;
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
     * <li>{@link #withController(AIController)} – sets the controller that
     * provides framework-agnostic tools and lifecycle hooks.</li>
     * <li>{@link #withUserName(String)} – sets the display name for user
     * messages (defaults to "You").</li>
     * <li>{@link #withAssistantName(String)} – sets the display name for
     * assistant messages (defaults to "Assistant").</li>
     * <li>{@link #withRequestListener(RequestListener)} – registers a callback
     * that fires on every prompt with the user message, the assigned message
     * id, and any attachments.</li>
     * <li>{@link #withResponseListener(ResponseListener)} – registers a
     * callback that fires after each exchange with the assistant's response
     * text and an optional error (success and failure use the same listener),
     * enabling persistence via {@link AIOrchestrator#getHistory()} or follow-up
     * actions.</li>
     * <li>{@link #withHistory(List, Map)} – restores a previously saved
     * conversation history with attachments (from
     * {@link AIOrchestrator#getHistory()}).</li>
     * <li>{@link #withMetadata(SerializableSupplier)} – sets a supplier the
     * orchestrator invokes on every turn to give the LLM free-form session
     * context. Defaults to a current-date-and-time supplier so the LLM can
     * interpret relative date/time references; pass {@code null} to disable, or
     * a custom supplier to include tenant, locale, page state, or anything else
     * worth keeping out of the system prompt.</li>
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
        private Object[] tools;
        private AIController controller;
        private String userName;
        private String assistantName;
        private RequestListener requestListener;
        private AttachmentClickListener attachmentClickListener;
        private ResponseListener responseListener;
        private List<ChatMessage> history;
        private Map<String, List<AIAttachment>> historyAttachments;
        private SerializableSupplier<String> contextSupplier;
        private boolean contextSupplierSet;

        private Builder(LLMProvider provider, String systemPrompt) {
            Objects.requireNonNull(provider, "Provider cannot be null");
            if (systemPrompt == null || systemPrompt.isBlank()) {
                LOGGER.warn("No system prompt was provided to the "
                        + "AIOrchestrator. Pass a system prompt to "
                        + "AIOrchestrator.builder(provider, systemPrompt) "
                        + "to guide the LLM's behaviour.");
            }
            this.provider = provider;
            this.systemPrompt = systemPrompt;
        }

        private static void warnIfAlreadySet(Object current, String name) {
            if (current != null) {
                LOGGER.warn("{} was already set on the builder and will "
                        + "be replaced", name);
            }
        }

        /**
         * Sets the message list component.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public Builder withMessageList(AIMessageList messageList) {
            warnIfAlreadySet(this.messageList, "Message list");
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
            warnIfAlreadySet(this.messageList, "Message list");
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
            warnIfAlreadySet(this.input, "Input");
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
            warnIfAlreadySet(this.input, "Input");
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
            warnIfAlreadySet(this.fileReceiver, "File receiver");
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
            warnIfAlreadySet(this.fileReceiver, "File receiver");
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
            warnIfAlreadySet(this.fileReceiver, "File receiver");
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
            if (this.tools != null) {
                warnIfAlreadySet(this.tools, "Tools");
            }
            this.tools = tools;
            return this;
        }

        /**
         * Sets the controller that provides framework-agnostic tools and
         * lifecycle hooks to the orchestrator. The controller's tools are
         * collected before each LLM request.
         *
         * @param controller
         *            the controller to set, not {@code null}
         * @return this builder
         * @throws NullPointerException
         *             if controller is {@code null}
         * @throws IllegalArgumentException
         *             if any tool name is invalid
         */
        public Builder withController(AIController controller) {
            Objects.requireNonNull(controller, "Controller cannot be null");
            if (controller.getTools() != null) {
                validateToolNames(controller.getTools());
            }
            warnIfAlreadySet(this.controller, "Controller");
            this.controller = controller;
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
            warnIfAlreadySet(this.userName, "User name");
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
            warnIfAlreadySet(this.assistantName, "Assistant name");
            this.assistantName = assistantName;
            return this;
        }

        /**
         * Sets a listener that is called on every prompt, just before the LLM
         * stream opens. The listener receives the user message, the assigned
         * {@code messageId}, and the attachments included with the message
         * (empty list when none). Same lifecycle moment as
         * {@link AIController#onRequest()}.
         * <p>
         * Typical use: persist attachment data in your own storage keyed by
         * {@code messageId}, so the same id can be used later to look the
         * attachment up via
         * {@link AttachmentClickListener.AttachmentClickEvent#getMessageId()}
         * or when restoring conversation history via
         * {@link #withHistory(List, Map)}.
         *
         * @param listener
         *            the listener to call on each prompt
         * @return this builder
         */
        public Builder withRequestListener(RequestListener listener) {
            warnIfAlreadySet(this.requestListener, "Request listener");
            this.requestListener = listener;
            return this;
        }

        /**
         * Sets a listener that is called when an attachment in the message list
         * is clicked. The listener receives the message ID and attachment
         * index, allowing you to retrieve attachment data from your own storage
         * using the same message ID provided in
         * {@link RequestListener.RequestEvent#getMessageId()}.
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
            warnIfAlreadySet(this.attachmentClickListener,
                    "Attachment click listener");
            this.attachmentClickListener = listener;
            return this;
        }

        /**
         * Sets a listener that is called once per turn when the assistant's
         * stream has completed — successfully or with an error. This is the
         * recommended hook for persisting conversation state (via
         * {@link AIOrchestrator#getHistory()}), triggering follow-up actions,
         * or surfacing errors to the user. Same lifecycle moment as
         * {@link AIController#onResponse(Throwable)}.
         * <p>
         * On success the response text may be empty if the model emitted only
         * tool calls or otherwise stopped without producing visible content.
         * Such turns are still successful exchanges; check
         * {@code event.getResponse().isEmpty()} if your listener should only
         * react to text-bearing responses. Empty responses are <i>not</i>
         * appended to the conversation history.
         * <p>
         * On failure {@code event.getError()} carries the cause and the
         * response text is empty or a partial stream that was received before
         * the failure.
         * <p>
         * The listener is called from a background thread (Reactor scheduler).
         * It is safe to perform blocking I/O (e.g. database writes) directly.
         * To update Vaadin UI components from this listener, use
         * {@code ui.access()}.
         * <p>
         * The listener is not called when history is restored via
         * {@link #withHistory(List, Map)}.
         *
         * @param listener
         *            the listener to call after each exchange
         * @return this builder
         */
        public Builder withResponseListener(ResponseListener listener) {
            warnIfAlreadySet(this.responseListener, "Response listener");
            this.responseListener = listener;
            return this;
        }

        /**
         * Sets the supplier of free-form session context the LLM sees on every
         * turn. The supplier is invoked once per turn on the UI thread when the
         * request is being built.
         * <p>
         * The supplier may return any string the application wants the LLM to
         * have on hand — current date and time, the active tenant, the user's
         * locale, the page the user is on, feature flags. Compose multiple
         * pieces of context with plain string concatenation.
         * <p>
         * If the supplier returns {@code null} or an empty/blank string, no
         * context is added for that turn — useful for "context only when X"
         * patterns. If the supplier throws, the turn is aborted via the normal
         * error path: the assistant placeholder is updated to a generic error
         * message, {@link AIController#onResponse(Throwable)} fires with the
         * thrown exception, and the exception propagates to the caller of the
         * prompt entry point.
         * <p>
         * Passing {@code null} disables session context entirely, including the
         * built-in default. By default, the orchestrator installs a supplier
         * that yields the current date and time so the LLM can interpret
         * relative date/time references ("show me sales from the past two
         * months") without having to guess.
         * <p>
         * The supplier runs on the UI thread, so it can read
         * {@link UI#getCurrent()} and session-scoped state.
         *
         * @param contextSupplier
         *            supplier of the per-turn context string, or {@code null}
         *            to disable session context entirely
         * @return this builder
         */
        public Builder withMetadata(
                SerializableSupplier<String> contextSupplier) {
            if (contextSupplierSet) {
                LOGGER.warn("Context supplier was already set on the "
                        + "builder and will be replaced");
            }
            this.contextSupplier = contextSupplier;
            this.contextSupplierSet = true;
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
            warnIfAlreadySet(this.history, "History");
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
            forEachClaimable(AIOrchestrator::claim);
            var orchestrator = new AIOrchestrator(provider, systemPrompt);
            orchestrator.messageList = messageList;
            orchestrator.input = input;
            orchestrator.fileReceiver = fileReceiver;
            orchestrator.tools = tools == null ? new Object[0] : tools;
            orchestrator.controller = controller;
            orchestrator.userName = userName == null ? "You" : userName;
            orchestrator.assistantName = assistantName == null ? "Assistant"
                    : assistantName;
            orchestrator.requestListener = requestListener;
            orchestrator.attachmentClickListener = attachmentClickListener;
            orchestrator.responseListener = responseListener;
            orchestrator.contextSupplier = contextSupplierSet ? contextSupplier
                    : defaultContextSupplier();
            try {
                if (input != null) {
                    input.addSubmitListener(
                            msg -> orchestrator.doPrompt(msg, null));
                }

                if (attachmentClickListener != null && messageList != null) {
                    messageList
                            .addAttachmentClickListener((message, attIndex) -> {
                                var messageId = orchestrator.itemToMessageId
                                        .get(message);
                                if (messageId != null) {
                                    orchestrator.attachmentClickListener
                                            .onAttachmentClick(
                                                    new AttachmentClickListener.AttachmentClickEvent(
                                                            messageId,
                                                            attIndex));
                                }
                            });
                }
                if (history != null) {
                    orchestrator.restoreHistory(history, historyAttachments);
                }
            } catch (RuntimeException e) {
                forEachClaimable(AIOrchestrator::unclaim);
                throw e;
            }

            LOGGER.debug(
                    "Built AIOrchestrator with messageList={}, input={}, "
                            + "fileReceiver={}, tools={}, controller={}, "
                            + "userName={}, assistantName={}",
                    orchestrator.messageList != null,
                    orchestrator.input != null,
                    orchestrator.fileReceiver != null,
                    orchestrator.tools == null ? 0 : orchestrator.tools.length,
                    orchestrator.controller != null, orchestrator.userName,
                    orchestrator.assistantName);

            return orchestrator;
        }

        private void forEachClaimable(Consumer<Object> action) {
            action.accept(provider);
            action.accept(
                    messageList instanceof MessageListWrapper w ? w.messageList
                            : messageList);
            action.accept(
                    input instanceof MessageInputWrapper w ? w.messageInput()
                            : input);
            if (fileReceiver instanceof UploadManagerWrapper w) {
                action.accept(w.uploadManager);
            } else if (fileReceiver instanceof UploadWrapper w) {
                action.accept(w.upload);
            } else {
                action.accept(fileReceiver);
            }
            action.accept(controller);
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
