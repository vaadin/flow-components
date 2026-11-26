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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.ai.AiComponentsExperimentalFeatureException;
import com.vaadin.flow.component.ai.AiComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.common.AiAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.provider.LlmProvider;
import com.vaadin.flow.component.ai.provider.ResponseMetadata;
import com.vaadin.flow.component.ai.ui.AiFileReceiver;
import com.vaadin.flow.component.ai.ui.AiInput;
import com.vaadin.flow.component.ai.ui.AiMessage;
import com.vaadin.flow.component.ai.ui.AiMessageList;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadHelper;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.UploadHandler;

import reactor.core.scheduler.Schedulers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
 * <li>Controller-based feature composition via {@link AiController}</li>
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
 *         .withFileReceiver(uploadManager) // optional
 *         .withTools(toolObj) // optional, for @Tool annotations
 *         .withUserName(userName) // optional
 *         .withAssistantName(assistantName) // optional
 *         .withI18n(i18n) // optional
 *         .withResponseListener(e -&gt; save(e.getResponse())) // optional
 *         .withHistory(savedHistory, savedAttachments) // optional, for restore
 *         .build();
 * </pre>
 * <p>
 * The orchestrator tracks conversation history internally and delegates to the
 * {@link LlmProvider} for the LLM's working memory. Use {@link #getHistory()}
 * to obtain a snapshot and {@link Builder#withHistory(List, Map)} to restore
 * conversation state (including attachments) across sessions. To persist
 * history automatically after each exchange, use
 * {@link Builder#withResponseListener(ResponseListener)}.
 * </p>
 * <p>
 * <b>Serialization:</b> The LLM provider and tool objects are not serialized
 * (they are transient). After deserialization, call
 * {@link #reconnect(LlmProvider)} to restore transient dependencies and replay
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
 * @since 25.3
 */
public class AiOrchestrator implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AiOrchestrator.class);

    /**
     * Default timeout for LLM response streaming in seconds.
     */
    private static final int TIMEOUT_SECONDS = 600;

    /**
     * Shown in the message list in place of, or after, the assistant's answer
     * when a turn fails for a reason the user cannot act on, unless
     * {@link AiOrchestratorI18n#getErrorMessage()} is set.
     */
    private static final String DEFAULT_ERROR_MESSAGE = "An error occurred. Please try again.";

    /**
     * Shown in the message list in place of the assistant's answer when the
     * response times out, unless
     * {@link AiOrchestratorI18n#getTimeoutErrorMessage()} is set.
     */
    private static final String DEFAULT_TIMEOUT_ERROR_MESSAGE = "Request timed out. Please try again.";

    /**
     * The feature flag ID for AI components.
     */
    static final String FEATURE_FLAG_ID = AiComponentsFeatureFlagProvider.FEATURE_FLAG_ID;

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
                    + " is already in use by another AiOrchestrator. "
                    + "Each instance can only be used by a single "
                    + "orchestrator.");
        }
    }

    private static void unclaim(Object instance) {
        if (instance != null) {
            CLAIMED_INSTANCES.remove(instance);
        }
    }

    private transient LlmProvider provider;
    private final String systemPrompt;
    private AiMessageList messageList;
    private AiInput input;
    private AiFileReceiver fileReceiver;
    private transient Object[] tools;
    private transient AiController controller;
    private String userName;
    private String assistantName;
    private AiOrchestratorI18n i18n;
    private RequestInterceptor requestInterceptor;
    private RequestListener requestListener;
    private AttachmentClickListener attachmentClickListener;
    private ResponseListener responseListener;
    private SerializableSupplier<String> contextSupplier;
    private final Map<AiMessage, String> itemToMessageId = new HashMap<>();
    private final List<ChatMessage> conversationHistory = new CopyOnWriteArrayList<>();

    /**
     * Busy guard: claimed by {@code doPrompt}'s CAS, and every downstream path
     * must release it exactly once — the {@code doPrompt} catch (sync
     * failures), the verdict-drop paths, the stream's {@code doFinally}, the
     * postponement abort/resume/detach handlers (including their
     * release-then-rethrow catches), and {@code Reconnector#apply()} after
     * deserialization. When adding a new path out of a prompt, decide which of
     * these owns the release; a missed release blocks all later prompts.
     */
    private final ProcessingFlag isProcessing = new ProcessingFlag();
    /**
     * Disables submitting on the input while busy, or {@code null} if there is
     * no input. Notified when {@link #isProcessing} is released, so the release
     * paths do not call it.
     */
    private SubmitDisabler submitDisabler;
    private final AtomicBoolean featureFlagChecked = new AtomicBoolean(false);

    /**
     * Creates a new AI orchestrator.
     *
     * @param provider
     *            the LLM provider to use for generating responses
     * @param systemPrompt
     *            the system prompt for the LLM (can be null)
     */
    private AiOrchestrator(LlmProvider provider, String systemPrompt) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        this.provider = provider;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Creates a new builder for AiOrchestrator with a system prompt.
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
    public static Builder builder(LlmProvider provider, String systemPrompt) {
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
     * Attachments are taken from a configured {@link AiFileReceiver} (if any).
     * To send a prompt with caller-supplied attachments, use
     * {@link #prompt(String, List)}.
     * </p>
     * <p>
     * If a request is already being processed, this method logs a warning and
     * returns without processing the new prompt. The same applies while a file
     * is still being uploaded to the file receiver (see
     * {@link AiFileReceiver#isUploading()}), so that the file is not left out
     * of the prompt.
     * </p>
     * <p>
     * An interceptor configured via
     * {@link Builder#withRequestInterceptor(RequestInterceptor)} runs first and
     * may change the content, reject the prompt entirely, or postpone it to
     * finish preprocessing asynchronously.
     * </p>
     *
     * @param userMessage
     *            the prompt to send to the AI
     * @throws IllegalStateException
     *             if no UI context is available, or if the orchestrator needs
     *             to be reconnected after deserialization (see
     *             {@link #reconnect(LlmProvider)})
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
     * attachments are forwarded to the {@link LlmProvider.LlmRequest}.
     * </p>
     * <p>
     * <b>Interaction with a configured {@link AiFileReceiver}:</b> this
     * overload uses only the supplied list and does <b>not</b> drain the
     * receiver. Attachments pending in the receiver stay there for the next
     * call to {@link #prompt(String)} (or the user's next submit through a
     * connected input). To merge UI-driven and programmatic attachments, the
     * caller must drain the receiver explicitly and pass the combined list.
     * </p>
     * <p>
     * If a request is already being processed, this method logs a warning and
     * returns without processing the new prompt. Unlike
     * {@link #prompt(String)}, it is not ignored while a file is still being
     * uploaded to the file receiver, since it does not take attachments from
     * the receiver.
     * </p>
     * <p>
     * An interceptor configured via
     * {@link Builder#withRequestInterceptor(RequestInterceptor)} runs first and
     * may change the content, reject the prompt entirely, or postpone it to
     * finish preprocessing asynchronously.
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
     *             {@link #reconnect(LlmProvider)})
     */
    public void prompt(String userMessage, List<AiAttachment> attachments) {
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
     * prompts, unless the provider manages its conversation memory outside the
     * orchestrator. The UI is not modified -- message list, input, and file
     * receiver components retain their state across serialization. The
     * exception is that submitting is enabled again on the input if the
     * orchestrator disabled it, so call {@code apply()} while the session is
     * locked.
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
    public Reconnector reconnect(LlmProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        if (this.provider != null) {
            throw new IllegalStateException(
                    "AiOrchestrator is already connected. "
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
            Map<String, List<AiAttachment>> attachmentsByMessageId) {
        provider.setHistory(history, attachmentsByMessageId);

        conversationHistory.addAll(history);

        if (messageList != null) {
            for (var message : history) {
                AiMessage aiMessage;
                if (message.role() == ChatMessage.Role.USER) {
                    var attachments = message.messageId() != null
                            ? attachmentsByMessageId.getOrDefault(
                                    message.messageId(),
                                    Collections.emptyList())
                            : Collections.<AiAttachment> emptyList();
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

    /**
     * Returns the assistant message of the turn, creating it on first use. The
     * message is created lazily so that the typing indicator, rather than an
     * empty message, is shown while the response is being produced.
     * <p>
     * Must be called on the UI thread, so that the holder is only ever written
     * while the session lock is held.
     *
     * @param assistantMessage
     *            the holder of the turn's assistant message
     * @return the assistant message, or {@code null} if there is no message
     *         list to add it to
     */
    private AiMessage getOrCreateAssistantMessage(
            AtomicReference<AiMessage> assistantMessage) {
        if (messageList == null) {
            return null;
        }
        var existing = assistantMessage.get();
        if (existing != null) {
            return existing;
        }
        messageList.hideTypingIndicator(assistantName);
        var created = messageList.addMessage("", assistantName,
                Collections.emptyList());
        assistantMessage.set(created);
        return created;
    }

    private void streamResponseToMessage(LlmProvider.LlmRequest request,
            AtomicReference<AiMessage> assistantMessage, UI ui,
            AtomicReference<ResponseMetadata> metadataHolder) {
        var responseBuilder = new StringBuilder();
        var responseStream = provider.stream(request)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS));
        responseStream.doFinally(signal -> {
            isProcessing.set(false);
        }).subscribe(token -> {
            responseBuilder.append(token);
            if (messageList != null) {
                accessIfAttached(ui, () -> {
                    var message = getOrCreateAssistantMessage(assistantMessage);
                    if (message != null) {
                        message.appendText(token);
                    }
                });
            }
        }, error -> {
            String userMessage;
            if (error instanceof TimeoutException) {
                userMessage = getTimeoutErrorMessage();
                LOGGER.warn("LLM request timed out after {} seconds",
                        TIMEOUT_SECONDS);
            } else {
                userMessage = getErrorMessage();
                LOGGER.error("Error during LLM streaming", error);
            }
            if (messageList != null) {
                accessIfAttached(ui, () -> {
                    var message = getOrCreateAssistantMessage(assistantMessage);
                    if (message != null) {
                        message.setText(userMessage);
                    }
                });
            }
            fireResponseListener("", error, ui, metadataHolder.get());
        }, () -> {
            if (messageList != null) {
                // Hides the indicator of a turn that produced no tokens, for
                // example a tool-only turn. Checked inside the command, as the
                // command that creates the message may still be queued.
                accessIfAttached(ui, () -> {
                    if (assistantMessage.get() == null) {
                        messageList.hideTypingIndicator(assistantName);
                    }
                });
            }
            var responseText = responseBuilder.toString();
            if (!responseText.isEmpty()) {
                conversationHistory
                        .add(new ChatMessage(ChatMessage.Role.ASSISTANT,
                                responseText, null, Instant.now()));
            }
            fireResponseListener(responseText, null, ui, metadataHolder.get());
            LOGGER.debug("LLM streaming completed successfully");
        });
    }

    private void doPrompt(String userMessage,
            List<AiAttachment> explicitAttachments) {
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }
        if (provider == null) {
            throw new IllegalStateException(
                    "AiOrchestrator needs to be reconnected after "
                            + "deserialization. Call reconnect(provider) first.");
        }
        if (explicitAttachments == null && isFileReceiverUploading()) {
            LOGGER.warn("Ignoring prompt: a file is still being uploaded");
            return;
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
        // Here rather than when the flag is claimed, so that submitting which
        // the interceptor or a request hook disabled is left as it is
        if (submitDisabler != null) {
            submitDisabler.update();
        }
    }

    private void processUserInput(String userMessage,
            List<AiAttachment> explicitAttachments) {
        var ui = UI.getCurrentOrThrow();
        checkFeatureFlag(ui);

        var attachments = getAttachmentsToProcess(explicitAttachments);
        if (requestInterceptor != null) {
            var event = new RequestInterceptor.RequestInterceptEvent(
                    userMessage, attachments);
            requestInterceptor.intercept(event);
            if (event.getContinuation() != null) {
                // isProcessing stays claimed until the continuation
                // completes, so no other prompt can interleave with the
                // suspended one.
                handlePostponedPrompt(ui, event);
                return;
            }
            if (!applyInterceptVerdict(event)) {
                isProcessing.set(false);
                return;
            }
            userMessage = event.getUserMessage();
            attachments = event.getAttachments();
        }
        startTurn(ui, userMessage, attachments);
    }

    /**
     * Runs an accepted prompt: displays it, fires the controller and request
     * listener hooks, records the history entry, and starts the LLM stream.
     * Assumes {@code isProcessing} is already claimed; the stream releases it
     * on completion, and a throw propagates to the caller, which owns the
     * cleanup.
     */
    private void startTurn(UI ui, String userMessage,
            List<AiAttachment> attachments) {
        var userAIMessage = messageList == null ? null
                : messageList.addMessage(userMessage, userName, attachments);
        var assistantMessage = new AtomicReference<AiMessage>();
        if (messageList != null) {
            messageList.showTypingIndicator(assistantName);
        }

        try {
            var messageId = UUID.randomUUID().toString();
            // One event for both hooks: the user message, assigned messageId,
            // and attachments (empty list when none).
            var requestEvent = new RequestListener.RequestEvent(userMessage,
                    messageId, attachments);
            if (controller != null) {
                controller.onRequest(requestEvent);
            }

            var metadataHolder = new AtomicReference<ResponseMetadata>();
            var request = buildRequest(userMessage, attachments,
                    metadataHolder);
            LOGGER.debug("Processing prompt with {} attachments",
                    attachments.size());

            if (requestListener != null) {
                requestListener.onRequest(requestEvent);
            }
            conversationHistory.add(new ChatMessage(ChatMessage.Role.USER,
                    userMessage, messageId, Instant.now()));
            if (userAIMessage != null) {
                itemToMessageId.put(userAIMessage, messageId);
            }

            streamResponseToMessage(request, assistantMessage, ui,
                    metadataHolder);
        } catch (Throwable t) { // NOSONAR — Throwable to surface UI on any
                                // throw
            // Single update — stream errors are async and never reach this
            // catch; onResponse throws are handled inside
            // fireResponseListener (which appends rather than rewrites).
            var message = getOrCreateAssistantMessage(assistantMessage);
            if (message != null) {
                message.setText(getErrorMessage());
            }
            throw t;
        }
    }

    private String getErrorMessage() {
        var message = i18n == null ? null : i18n.getErrorMessage();
        return message == null ? DEFAULT_ERROR_MESSAGE : message;
    }

    private String getTimeoutErrorMessage() {
        var message = i18n == null ? null : i18n.getTimeoutErrorMessage();
        return message == null ? DEFAULT_TIMEOUT_ERROR_MESSAGE : message;
    }

    /**
     * Applies the interceptor's verdict to the event. Returns {@code false}
     * when the prompt must be dropped: the interceptor rejected it (with a
     * rejection message, the original prompt and the reason are shown in the
     * message list, list-only), or the replacement text is blank. The caller
     * owns releasing {@code isProcessing} on the {@code false} path.
     */
    private boolean applyInterceptVerdict(
            RequestInterceptor.RequestInterceptEvent event) {
        if (event.isRejected()) {
            if (event.getRejectionMessage() != null && messageList != null) {
                // Show the exchange as the user experienced it: their
                // original message (list-only, never in the history or a
                // request) followed by the reason — a reason with no
                // question above it would read as the assistant speaking
                // unprompted, and the input has already cleared the text.
                messageList.addMessage(event.getOriginalUserMessage(), userName,
                        event.getOriginalAttachments());
                messageList.addMessage(event.getRejectionMessage(),
                        assistantName, Collections.emptyList());
            }
            return false;
        }
        if (event.getUserMessage().isBlank()) {
            LOGGER.warn("Prompt dropped: the processed message is blank. "
                    + "Use reject() to cancel a prompt explicitly.");
            return false;
        }
        return true;
    }

    /**
     * Arms the postponed prompt: schedules the timeout and registers the resume
     * callback on the continuation. The callback may fire immediately on the
     * current thread (the interceptor completed the continuation before
     * returning) or later on whichever application or timer thread completes
     * it.
     */
    private void handlePostponedPrompt(UI ui,
            RequestInterceptor.RequestInterceptEvent event) {
        var continuation = event.getContinuation();
        var timeout = continuation.getTimeout();
        // boundedElastic, not parallel: on timeout the end-of-turn hooks may
        // run inline on this thread (ui.access runs its task on the calling
        // thread when the session lock is free), and application work in them
        // must not occupy the CPU-sized parallel pool that backs stream
        // timeouts.
        var timer = Schedulers.boundedElastic()
                .schedule(() -> continuation.fail(new TimeoutException(
                        "Request interception timed out after " + timeout)),
                        timeout.toMillis(), TimeUnit.MILLISECONDS);
        continuation.onComplete(failure -> {
            try {
                timer.dispose();
                if (failure != null) {
                    abortPostponedPrompt(ui, failure);
                } else {
                    resumePostponedPrompt(ui, event);
                }
            } catch (Throwable t) { // NOSONAR — release-then-rethrow
                // A throw from the completion plumbing would otherwise leave
                // isProcessing claimed forever, blocking all later prompts.
                // Releasing here is safe: the resume task either never ran
                // (nothing started) or already handed the flag to the stream,
                // whose doFinally release is idempotent with this one.
                isProcessing.set(false);
                throw t;
            }
        });
    }

    /**
     * Ends a postponed prompt that failed or timed out. Nothing was displayed
     * for the prompt, so no UI cleanup is needed beyond reporting — which is
     * why the flag is released before touching the UI: a detached UI must not
     * leave the orchestrator stuck.
     */
    private void abortPostponedPrompt(UI ui, Throwable failure) {
        isProcessing.set(false);
        fireResponseListener("", failure, ui);
    }

    /**
     * Runs a UI update for the current turn, tolerating a UI that was detached
     * while the turn ran: the update is skipped and logged at debug instead of
     * surfacing {@link UIDetachedException} as a stream error. The turn itself
     * is unaffected — the conversation history and the {@link ResponseListener}
     * are UI-independent.
     */
    private static void accessIfAttached(UI ui, Command command) {
        try {
            ui.access(command);
        } catch (UIDetachedException e) {
            LOGGER.debug(
                    "Skipped a UI update for an abandoned turn (UI detached)",
                    e);
        }
    }

    /**
     * Resumes a postponed prompt on the UI thread: applies the verdict and
     * starts the turn with the event's final content. Mirrors
     * {@link #doPrompt}'s cleanup-then-rethrow handling, which cannot cover
     * this path — the prompt call returned when the prompt was postponed, so
     * the rethrow surfaces the failure to the session error handler instead.
     * That is deliberate: without a response listener or controller the
     * graceful report reaches nobody, and the error handler is the only
     * remaining channel. A detached UI abandons the prompt; {@code accessLater}
     * is used instead of {@code access} because its detach handler also covers
     * a UI that detaches after the resume task is enqueued but before it runs —
     * a plain {@code access} would silently drop the task and leave
     * {@code isProcessing} claimed forever.
     */
    private void resumePostponedPrompt(UI ui,
            RequestInterceptor.RequestInterceptEvent event) {
        ui.accessLater(() -> {
            try {
                if (!applyInterceptVerdict(event)) {
                    isProcessing.set(false);
                    return;
                }
                startTurn(ui, event.getUserMessage(), event.getAttachments());
            } catch (Throwable t) { // NOSONAR — cleanup-then-rethrow
                isProcessing.set(false);
                fireResponseListener("", t, ui);
                throw t;
            }
        }, () -> {
            isProcessing.set(false);
            LOGGER.warn("Postponed prompt abandoned: its UI was detached "
                    + "before interception completed");
        }).run();
    }

    /**
     * Sending while a file is still uploading would leave the file out of the
     * prompt and clear it from the file list, while the upload goes on and the
     * file would then be sent with a later prompt.
     */
    private boolean isFileReceiverUploading() {
        return fileReceiver != null && fileReceiver.isUploading();
    }

    private List<AiAttachment> getAttachmentsToProcess(
            List<AiAttachment> explicitAttachments) {
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

    private LlmProvider.LlmRequest buildRequest(String userMessage,
            List<AiAttachment> attachments,
            AtomicReference<ResponseMetadata> metadataHolder) {
        final var effectiveSystemPrompt = systemPrompt != null
                && !systemPrompt.isBlank() ? systemPrompt.trim() : null;
        final var explicitTools = controller != null
                && controller.getTools() != null ? controller.getTools()
                        : List.<LlmProvider.ToolSpec> of();
        final var sessionContext = resolveSessionContext();
        return new LlmProvider.LlmRequest() {

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
                return effectiveSystemPrompt;
            }

            @Override
            public Object[] tools() {
                return tools == null ? new Object[0] : tools;
            }

            @Override
            public List<LlmProvider.ToolSpec> explicitTools() {
                return explicitTools;
            }

            @Override
            public String sessionContext() {
                return sessionContext;
            }

            @Override
            public Consumer<ResponseMetadata> metadataSink() {
                return metadataHolder::set;
            }
        };
    }

    /**
     * Resolves the configured session context supplier for this turn. The
     * content travels in {@link LlmProvider.LlmRequest#sessionContext()} and
     * the provider appends it to the user message, at the very end of the
     * prompt. That placement is deliberate: LLM providers cache the prompt
     * prefix in the order tools, system prompt, messages, so per-turn content
     * carried by a tool description or the system prompt would invalidate the
     * whole cache on every turn.
     * <p>
     * A supplier that throws aborts the turn — the exception propagates through
     * {@link #buildRequest} and is handled by the existing error path in
     * {@link #processUserInput}.
     *
     * @return the context for this turn, or {@code null} when there is none
     */
    private String resolveSessionContext() {
        if (contextSupplier == null) {
            return null;
        }
        var resolved = contextSupplier.get();
        return resolved == null || resolved.isBlank() ? null : resolved;
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

    private void fireResponseListener(String responseText, Throwable error,
            UI ui) {
        fireResponseListener(responseText, error, ui, null);
    }

    private void fireResponseListener(String responseText, Throwable error,
            UI ui, ResponseMetadata metadata) {
        var event = new ResponseListener.ResponseEvent(responseText, error,
                metadata);
        // Both hooks run as one ui.access() task, the controller first, so
        // that a turn the controller could not apply reaches the listener as
        // the turn's error, and so that the listener has the same threading
        // contract with and without a controller. accessLater rather than
        // access: its detach handler also covers a UI that detaches after the
        // task is enqueued, and the listener must hear the turn end either way.
        ui.accessLater(() -> notifyResponseListener(applyTurn(event)), () -> {
            LOGGER.debug("Turn ended after its UI was detached; the response "
                    + "listener runs without a session");
            notifyResponseListener(event);
        }).run();
    }

    private void notifyResponseListener(ResponseListener.ResponseEvent event) {
        if (responseListener == null) {
            return;
        }
        try {
            responseListener.onResponse(event);
        } catch (Exception e) {
            LOGGER.error("Error in response listener", e);
        }
    }

    /**
     * Runs the controller hook, if there is a controller, and returns the
     * outcome the listener gets: the turn's own, or a failure carrying the
     * hook's throw when a completed turn could not be applied.
     */
    private ResponseListener.ResponseEvent applyTurn(
            ResponseListener.ResponseEvent event) {
        if (controller == null) {
            return event;
        }
        try {
            controller.onResponse(event);
        } catch (Exception e) {
            LOGGER.error("Error in controller onResponse", e);
            if (event.getError().isPresent()) {
                // The turn had already failed; that error stands.
                return event;
            }
            // Append a separate assistant message instead of rewriting the
            // LLM's response. By the time this runs, the response is already
            // in the provider's chat memory and in our history; rewriting
            // either would misrepresent what the LLM actually said. Only on
            // the success path: the failure path already rewrote the
            // assistant message to a generic error message.
            if (messageList != null) {
                messageList.addMessage(getErrorMessage(), assistantName,
                        Collections.emptyList());
            }
            return new ResponseListener.ResponseEvent("", e,
                    event.getMetadata().orElse(null));
        }
        return event;
    }

    /**
     * Validates the tools a controller exposes at registration time, so a
     * malformed definition fails fast and names the tool instead of failing
     * later at request time.
     */
    private static void validateTools(List<LlmProvider.ToolSpec> tools) {
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
            validateParametersSchema(tool);
            if (!seen.add(name)) {
                LOGGER.warn(
                        "Duplicate tool name '{}': previous tool will be replaced",
                        name);
            }
        }
    }

    private static final ObjectMapper STRICT_JSON = JsonMapper.builder()
            .build();

    /**
     * Checks that the tool's parameters schema, when it declares one, is a JSON
     * object. A {@code null} or blank schema means the tool takes no parameters
     * and is valid.
     */
    private static void validateParametersSchema(LlmProvider.ToolSpec tool) {
        var schema = tool.getParametersSchema();
        if (schema == null || schema.isBlank()) {
            return;
        }
        JsonNode parsed;
        try {
            parsed = STRICT_JSON.readTree(schema);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Tool '" + tool.getName()
                    + "' has a parameters schema that is not valid JSON: "
                    + e.getOriginalMessage(), e);
        }
        if (parsed == null || !parsed.isObject()) {
            throw new IllegalArgumentException("Tool '" + tool.getName()
                    + "' has a parameters schema that is not a JSON object.");
        }
    }

    private void checkFeatureFlag(UI ui) {
        if (featureFlagChecked.get()) {
            return;
        }
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSessionOrThrow().getService().getContext());
        if (!featureFlags.isEnabled(FEATURE_FLAG_ID)) {
            throw new AiComponentsExperimentalFeatureException(
                    "AiOrchestrator");
        }
        featureFlagChecked.set(true);
    }

    /**
     * Fluent API for restoring transient dependencies on an
     * {@link AiOrchestrator} after deserialization. Obtain an instance via
     * {@link AiOrchestrator#reconnect(LlmProvider)}.
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
        private final AiOrchestrator orchestrator;
        private final LlmProvider provider;
        private Object[] tools;
        private AiController controller;
        private Map<String, List<AiAttachment>> attachmentsByMessageId;

        private Reconnector(AiOrchestrator orchestrator, LlmProvider provider) {
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
         *             if any tool name or parameters schema is invalid
         */
        public Reconnector withController(AiController controller) {
            Objects.requireNonNull(controller, "Controller cannot be null");
            if (controller.getTools() != null) {
                validateTools(controller.getTools());
            }
            this.controller = controller;
            return this;
        }

        /**
         * Sets the file attachments to restore on the new provider's
         * conversation memory. The map is keyed by
         * {@link ChatMessage#messageId()} and contains the list of
         * {@link AiAttachment} objects for each message.
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
                Map<String, List<AiAttachment>> attachmentsByMessageId) {
            this.attachmentsByMessageId = attachmentsByMessageId;
            return this;
        }

        /**
         * Applies the reconnection, restoring the provider, tools, and
         * conversation history on the new provider. The existing conversation
         * history is replayed onto the new provider's memory so that it has
         * full context for subsequent prompts. A provider that manages its
         * conversation memory outside the orchestrator ignores the replay, in
         * which case the application must restore that memory itself before
         * passing the provider in.
         * <p>
         * The UI (message list, input, file receiver) is not modified -- those
         * components survive serialization and retain their state
         * automatically. The exception is that submitting is enabled again on
         * the input if the orchestrator disabled it, so call this method while
         * the session is locked.
         */
        public void apply() {
            orchestrator.isProcessing.set(false);
            if (orchestrator.submitDisabler != null) {
                orchestrator.submitDisabler.update();
            }
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
     * Builder for configuring and creating an {@link AiOrchestrator} instance.
     * <p>
     * The builder requires an {@link LlmProvider} and a system prompt. All
     * other settings are optional:
     * </p>
     * <ul>
     * <li>{@link #withInput(AiInput)} or {@link #withInput(MessageInput)} –
     * connects a text input component so that user submissions are
     * automatically forwarded to the LLM. If omitted, use
     * {@link AiOrchestrator#prompt(String)} to send messages
     * programmatically.</li>
     * <li>{@link #withMessageList(AiMessageList)} or
     * {@link #withMessageList(MessageList)} – connects a message list component
     * to display the conversation. If omitted, responses are still streamed but
     * not rendered.</li>
     * <li>{@link #withFileReceiver(AiFileReceiver)},
     * {@link #withFileReceiver(UploadManager)}, or
     * {@link #withFileReceiver(Upload) withFileReceiver(Upload)} – enables file
     * upload support. Uploaded files are sent to the LLM as attachments with
     * the next prompt.</li>
     * <li>{@link #withTools(Object...)} – registers objects containing
     * vendor-specific tool-annotated methods (e.g. LangChain4j's {@code @Tool}
     * or Spring AI's {@code @Tool}) that the LLM can invoke.</li>
     * <li>{@link #withController(AiController)} – sets the controller that
     * provides framework-agnostic tools and lifecycle hooks.</li>
     * <li>{@link #withUserName(String)} – sets the display name for user
     * messages (defaults to "You").</li>
     * <li>{@link #withAssistantName(String)} – sets the display name for
     * assistant messages (defaults to "Assistant").</li>
     * <li>{@link #withI18n(AiOrchestratorI18n)} – sets the texts the
     * orchestrator shows in the message list on its own, such as the error
     * message of a failed turn, for example to localize them.</li>
     * <li>{@link #withRequestInterceptor(RequestInterceptor)} – sets an
     * interceptor that can validate, sanitize, replace, reject, or postpone the
     * user's message and attachments before anything is sent to the LLM.</li>
     * <li>{@link #withRequestListener(RequestListener)} – registers a callback
     * that fires on every prompt with the user message, the assigned message
     * id, and any attachments.</li>
     * <li>{@link #withResponseListener(ResponseListener)} – registers a
     * callback that fires after each exchange with the assistant's response
     * text and an optional error (success and failure use the same listener),
     * enabling persistence via {@link AiOrchestrator#getHistory()} or follow-up
     * actions.</li>
     * <li>{@link #withHistory(List, Map)} – restores a previously saved
     * conversation history with attachments (from
     * {@link AiOrchestrator#getHistory()}).</li>
     * <li>{@link #withMetadata(SerializableSupplier)} – sets a supplier the
     * orchestrator invokes on every turn to give the LLM free-form session
     * context. Defaults to a current-date-and-time supplier so the LLM can
     * interpret relative date/time references; pass {@code null} to disable, or
     * a custom supplier to include tenant, locale, page state, or anything else
     * worth keeping out of the system prompt. The context is appended to the
     * user message of the turn, so the system prompt and the tool definitions
     * stay identical from turn to turn.</li>
     * </ul>
     * <p>
     * Both Flow components ({@link MessageInput}, {@link MessageList},
     * {@link UploadManager}, {@link Upload}) and custom implementations of the
     * AI interfaces ({@link AiInput}, {@link AiMessageList},
     * {@link AiFileReceiver}) are accepted.
     * </p>
     */
    public static class Builder {
        private final LlmProvider provider;
        private final String systemPrompt;
        private AiMessageList messageList;
        private AiInput input;
        private AiFileReceiver fileReceiver;
        private Object[] tools;
        private AiController controller;
        private String userName;
        private String assistantName;
        private AiOrchestratorI18n i18n;
        private RequestInterceptor requestInterceptor;
        private RequestListener requestListener;
        private AttachmentClickListener attachmentClickListener;
        private ResponseListener responseListener;
        private List<ChatMessage> history;
        private Map<String, List<AiAttachment>> historyAttachments;
        private SerializableSupplier<String> contextSupplier;
        private boolean contextSupplierSet;

        private Builder(LlmProvider provider, String systemPrompt) {
            Objects.requireNonNull(provider, "Provider cannot be null");
            if (systemPrompt == null || systemPrompt.isBlank()) {
                LOGGER.warn("No system prompt was provided to the "
                        + "AiOrchestrator. Pass a system prompt to "
                        + "AiOrchestrator.builder(provider, systemPrompt) "
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
         * <p>
         * While a response is pending, the assistant is shown as working on a
         * response with {@link AiMessageList#showTypingIndicator(String)}, and
         * the assistant message is added when the first part of the response
         * arrives.
         *
         * @param messageList
         *            the message list
         * @return this builder
         */
        public Builder withMessageList(AiMessageList messageList) {
            warnIfAlreadySet(this.messageList, "Message list");
            this.messageList = messageList;
            return this;
        }

        /**
         * Sets the message list component using a Flow MessageList component.
         * <p>
         * While a response is pending, the assistant is shown in the typing
         * indicator of the message list, and the assistant message is added
         * when the first part of the response arrives. A turn that produces no
         * response text, for example one that only calls tools, adds no
         * assistant message.
         * <p>
         * The orchestrator only adds and removes its own entry among the typing
         * users, so an application that shows other typing users needs to keep
         * the assistant's entry when it updates them. When the typing users are
         * bound to a signal with {@link MessageList#bindTypingUsers
         * bindTypingUsers}, the typing indicator is left to the application.
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
         * <p>
         * Submitting is disabled on the input while a request is being
         * processed or a file is being uploaded, see
         * {@link AiInput#setSubmitDisabled(boolean)}.
         *
         * @param input
         *            the input component
         * @return this builder
         */
        public Builder withInput(AiInput input) {
            warnIfAlreadySet(this.input, "Input");
            this.input = input;
            return this;
        }

        /**
         * Sets the input component using a Flow MessageInput component.
         * <p>
         * Submitting is disabled on the input while a request is being
         * processed or a file is being uploaded, see
         * {@link AiInput#setSubmitDisabled(boolean)}.
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
         * <p>
         * While {@link AiFileReceiver#isUploading()} returns {@code true},
         * prompts from the input and from {@link AiOrchestrator#prompt(String)}
         * are ignored, so that the file is not left out of the prompt.
         *
         * @param fileReceiver
         *            the file receiver
         * @return this builder
         */
        public Builder withFileReceiver(AiFileReceiver fileReceiver) {
            warnIfAlreadySet(this.fileReceiver, "File receiver");
            this.fileReceiver = fileReceiver;
            return this;
        }

        /**
         * Sets the file receiver component using a Flow UploadManager. The
         * provided manager should not have an UploadHandler set beforehand.
         * <p>
         * While a file is being uploaded, prompts from the input and from
         * {@link AiOrchestrator#prompt(String)} are ignored, so that the file
         * is not left out of the prompt.
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
         * <p>
         * While a file is being uploaded, prompts from the input and from
         * {@link AiOrchestrator#prompt(String)} are ignored, so that the file
         * is not left out of the prompt.
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
         *             if any tool name or parameters schema is invalid
         */
        public Builder withController(AiController controller) {
            Objects.requireNonNull(controller, "Controller cannot be null");
            if (controller.getTools() != null) {
                validateTools(controller.getTools());
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
         * Sets the texts the orchestrator shows in the message list on its own,
         * such as the message that replaces the assistant's answer when a turn
         * fails. Use to localize them; any text left {@code null} in the given
         * object falls back to the built-in English default.
         * <p>
         * The names shown as the authors of the messages are set with
         * {@link #withUserName(String)} and {@link #withAssistantName(String)}
         * instead.
         *
         * @param i18n
         *            the texts to use, not {@code null}
         * @return this builder
         * @since 25.5
         */
        public Builder withI18n(AiOrchestratorI18n i18n) {
            Objects.requireNonNull(i18n,
                    "The i18n properties object should not be null");
            warnIfAlreadySet(this.i18n, "I18n");
            this.i18n = i18n;
            return this;
        }

        /**
         * Sets the interceptor that is called with the user's message text and
         * attachments before the orchestrator acts on them — before the message
         * appears in the message list, before controller and request listener
         * hooks, and before the LLM request is built. The interceptor can
         * validate and {@link RequestInterceptor.RequestInterceptEvent#reject()
         * reject} the prompt, sanitize or replace the message text, replace the
         * attachments, or
         * {@link RequestInterceptor.RequestInterceptEvent#postpone(Duration)
         * postpone} the prompt to finish preprocessing asynchronously;
         * everything downstream sees only the processed content. See
         * {@link RequestInterceptor} for the full contract.
         *
         * @param requestInterceptor
         *            the interceptor to call on each prompt
         * @return this builder
         */
        public Builder withRequestInterceptor(
                RequestInterceptor requestInterceptor) {
            warnIfAlreadySet(this.requestInterceptor, "Request interceptor");
            this.requestInterceptor = requestInterceptor;
            return this;
        }

        /**
         * Sets a listener that is called on every prompt, just before the LLM
         * stream opens. The listener receives the user message, the assigned
         * {@code messageId}, and the attachments included with the message
         * (empty list when none). Same lifecycle moment and same event as
         * {@link AiController#onRequest(RequestListener.RequestEvent)}.
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
         * {@link AiOrchestrator#getHistory()}), triggering follow-up actions,
         * or surfacing errors to the user. Same lifecycle moment as
         * {@link AiController#onResponse(ResponseListener.ResponseEvent)}.
         * <p>
         * On success the response text may be empty if the model emitted only
         * tool calls or otherwise stopped without producing visible content.
         * Such turns are still successful exchanges; check
         * {@code event.getResponse().isEmpty()} if your listener should only
         * react to text-bearing responses. Empty responses are <i>not</i>
         * appended to the conversation history.
         * <p>
         * On failure {@code event.getError()} carries the cause and the
         * response text is empty, even if text was received before the failure.
         * <p>
         * The listener runs as a {@code ui.access()} task, right after
         * {@link AiController#onResponse(ResponseListener.ResponseEvent)} when
         * a controller is attached: the session is locked, so UI components can
         * be updated directly, and blocking work holds the lock for its
         * duration. See {@link ResponseListener} for the details.
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
         * The context reaches the provider as
         * {@link LlmProvider.LlmRequest#sessionContext()}, and the built-in
         * providers append it to the user message of that turn, after the
         * user's own text. This keeps the system prompt and the tool
         * definitions identical from turn to turn, so an LLM provider that
         * caches the prompt prefix keeps serving them from its cache. The
         * message list and {@link AiOrchestrator#getHistory()} carry the user's
         * text only.
         * <p>
         * The supplier may return any string the application wants the LLM to
         * have on hand — current date and time, the active tenant, the user's
         * locale, the page the user is on, feature flags. Compose multiple
         * pieces of context with plain string concatenation.
         * <p>
         * If the supplier returns {@code null} or an empty/blank string, no
         * context is added for that turn — useful for "context only when X"
         * patterns. If the supplier throws, the turn is aborted via the normal
         * error path: the assistant message shows a generic error message,
         * {@link AiController#onResponse(ResponseListener.ResponseEvent)} fires
         * with the thrown exception, and the exception propagates to the caller
         * of the prompt entry point.
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
         * A provider that manages its conversation memory outside the
         * orchestrator ignores the provider part of the restore, so the
         * application must load that memory itself. The message list, the
         * attachment mappings, and the {@link AiOrchestrator#getHistory()}
         * snapshot are restored either way.
         * <p>
         * The attachment map is keyed by {@link ChatMessage#messageId()} and
         * contains the list of {@link AiAttachment} objects for each message.
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
                Map<String, List<AiAttachment>> attachmentsByMessageId) {
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
        public AiOrchestrator build() {
            forEachClaimable(AiOrchestrator::claim);
            var orchestrator = new AiOrchestrator(provider, systemPrompt);
            orchestrator.messageList = messageList;
            orchestrator.input = input;
            orchestrator.fileReceiver = fileReceiver;
            orchestrator.tools = tools == null ? new Object[0] : tools;
            orchestrator.controller = controller;
            orchestrator.userName = userName == null ? "You" : userName;
            orchestrator.assistantName = assistantName == null ? "Assistant"
                    : assistantName;
            orchestrator.i18n = i18n;
            orchestrator.requestInterceptor = requestInterceptor;
            orchestrator.requestListener = requestListener;
            orchestrator.attachmentClickListener = attachmentClickListener;
            orchestrator.responseListener = responseListener;
            orchestrator.contextSupplier = contextSupplierSet ? contextSupplier
                    : defaultContextSupplier();
            try {
                if (input != null) {
                    input.addSubmitListener(
                            msg -> orchestrator.doPrompt(msg, null));
                    orchestrator.submitDisabler = new SubmitDisabler(
                            orchestrator.isProcessing, input, fileReceiver);
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
                forEachClaimable(AiOrchestrator::unclaim);
                throw e;
            }

            LOGGER.debug(
                    "Built AiOrchestrator with messageList={}, input={}, "
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

        private static AiFileReceiver wrapUpload(Upload upload) {
            return new UploadWrapper(upload);
        }
    }
}
