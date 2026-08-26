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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.function.SerializableConsumer;

/**
 * Intercepts the user's input before the orchestrator acts on it. Configured
 * via
 * {@link AIOrchestrator.Builder#withRequestInterceptor(RequestInterceptor)},
 * the interceptor is invoked for every prompt — a submit through a connected
 * input component as well as the programmatic
 * {@link AIOrchestrator#prompt(String)} entry points — with the user's message
 * text and attachments. It can validate the content and
 * {@link RequestInterceptEvent#reject() reject} the prompt, sanitize or replace
 * the {@link RequestInterceptEvent#setUserMessage(String) text}, and replace
 * the {@link RequestInterceptEvent#setAttachments(List) attachments} (for
 * example to convert an uploaded file to a format the LLM accepts):
 *
 * <pre>
 * AIOrchestrator.builder(provider, systemPrompt)
 *         .withRequestInterceptor(event -&gt; {
 *             if (containsBlockedTerms(event.getUserMessage())) {
 *                 event.reject("Please rephrase your message.");
 *                 return;
 *             }
 *             event.setUserMessage(maskPii(event.getUserMessage()));
 *         }).build();
 * </pre>
 * <p>
 * The interceptor runs before the prompt has any effect: before the message
 * appears in the message list, before {@link AIController#onRequest()
 * controller} and {@link RequestListener} hooks, before the conversation
 * history entry, and before the LLM request is built. Everything downstream
 * sees only the processed content. A silently rejected prompt leaves no trace
 * in the UI or the history; rejecting with a user-facing message shows the
 * original prompt and the reason in the message list only — never in the
 * history or a request. Note that attachments pending in a configured file
 * receiver have already been taken from it when the interceptor runs, so they
 * are not resubmitted with the next prompt if this one is rejected, dropped, or
 * fails after being postponed. Prompts whose original text is blank are dropped
 * before the interceptor runs.
 * <p>
 * Throwing from the interceptor aborts the prompt the same way as a rejection,
 * except that the exception is reported to the {@link ResponseListener} and
 * {@link AIController#onResponse(Throwable)}, and propagates to the caller of
 * the prompt entry point. Throw only for failures; use
 * {@link RequestInterceptEvent#reject(String) reject} for expected validation
 * outcomes.
 * <p>
 * <b>Threading:</b> the interceptor is called on the UI thread under the
 * session lock, and unless the prompt is postponed its result is used as soon
 * as it returns — keep synchronous work short. Long-running work (e.g. heavy
 * media conversion or a remote moderation call) should instead
 * {@link RequestInterceptEvent#postpone(Duration) postpone} the prompt, run on
 * the application's own threads, and resume through the returned
 * {@link RequestContinuation}.
 * <p>
 * <b>Postponing:</b> while a prompt is postponed nothing is shown in the UI and
 * further prompts are ignored, so show a pending indicator and disable the
 * input before scheduling the work, and clean up when completing the
 * continuation. Server push must be enabled — e.g. with
 * {@link com.vaadin.flow.component.page.Push @Push} on the application shell
 * class — for the resumed turn to reach the browser without user interaction.
 * Capture the {@link com.vaadin.flow.component.UI UI} before scheduling the
 * work and wrap component changes made from the completing thread in
 * {@code ui.access(...)}:
 *
 * <pre>
 * .withRequestInterceptor(event -&gt; {
 *     var continuation = event.postpone(Duration.ofSeconds(10));
 *     var ui = UI.getCurrent();
 *     input.setEnabled(false);
 *     moderationService.checkAsync(event.getUserMessage())
 *             .whenComplete((verdict, error) -&gt; {
 *                 ui.access(() -&gt; input.setEnabled(true));
 *                 if (error != null) {
 *                     continuation.fail(error);
 *                     return;
 *                 }
 *                 if (!verdict.allowed()) {
 *                     event.reject("Please rephrase your message.");
 *                 }
 *                 continuation.proceed();
 *             });
 * })
 * </pre>
 *
 * A failure after postponing — {@link RequestContinuation#fail} or the timeout
 * — is reported to the {@link ResponseListener} and
 * {@link AIController#onResponse(Throwable)} only; it cannot propagate to the
 * caller of the prompt entry point, which has long returned.
 * <p>
 * <b>Serialization:</b> the interceptor is stored on the serializable
 * orchestrator and survives session serialization with it — unlike the LLM
 * provider, it needs no
 * {@link AIOrchestrator#reconnect(com.vaadin.flow.component.ai.provider.LLMProvider)
 * reconnect} step. A lambda implementation must therefore only capture
 * serializable state; reference non-serializable services (e.g. a moderation
 * client) indirectly instead of capturing them. A prompt that is postponed when
 * the session is serialized does not survive: completing its continuation
 * afterwards has no effect, and the deserialized orchestrator accepts new
 * prompts once reconnected.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
@FunctionalInterface
public interface RequestInterceptor extends Serializable {

    /**
     * Called with the user's input before the orchestrator acts on it. Mutate
     * the event to change what is sent, or reject it to cancel the prompt.
     *
     * @param event
     *            the event carrying the prompt content, never {@code null}
     */
    void intercept(RequestInterceptEvent event);

    /**
     * The content of one prompt, handed to a {@link RequestInterceptor} before
     * the orchestrator acts on it. Carries the user's message text and
     * attachments; both can be replaced, and the whole prompt can be rejected
     * or {@link #postpone(Duration) postponed} for asynchronous preprocessing.
     */
    class RequestInterceptEvent implements Serializable {

        private final String originalUserMessage;
        private final List<AIAttachment> originalAttachments;
        private String userMessage;
        private List<AIAttachment> attachments;
        private boolean rejected;
        private String rejectionMessage;
        private RequestContinuation continuation;

        /**
         * Creates a new event. The orchestrator creates these itself on every
         * prompt; create one manually only when invoking an interceptor without
         * an orchestrator (e.g. in tests).
         *
         * @param userMessage
         *            the user's message text, not {@code null}
         * @param attachments
         *            the attachments submitted with the message, not
         *            {@code null}; copied defensively
         * @throws NullPointerException
         *             if {@code userMessage} or {@code attachments} is
         *             {@code null}, or {@code attachments} contains
         *             {@code null} elements
         */
        public RequestInterceptEvent(String userMessage,
                List<AIAttachment> attachments) {
            this.originalUserMessage = Objects.requireNonNull(userMessage,
                    "User message must not be null");
            this.originalAttachments = List.copyOf(Objects.requireNonNull(
                    attachments, "Attachments must not be null"));
            this.userMessage = originalUserMessage;
            this.attachments = originalAttachments;
        }

        /**
         * Returns the message text as originally submitted, unaffected by
         * {@link #setUserMessage(String)}. Consulted by the orchestrator when
         * displaying a prompt rejected with a user-facing message.
         */
        String getOriginalUserMessage() {
            return originalUserMessage;
        }

        /**
         * Returns the attachments as originally submitted, unaffected by
         * {@link #setAttachments(List)}. Consulted by the orchestrator when
         * displaying a prompt rejected with a user-facing message.
         */
        List<AIAttachment> getOriginalAttachments() {
            return originalAttachments;
        }

        /**
         * Gets the message text as it currently stands — the user's original
         * text until {@link #setUserMessage(String)} replaces it.
         *
         * @return the message text, never {@code null}
         */
        public String getUserMessage() {
            return userMessage;
        }

        /**
         * Replaces the message text sent to the LLM. The replacement is also
         * what appears in the message list and the conversation history. A
         * blank replacement drops the prompt; prefer {@link #reject()} to
         * cancel explicitly.
         *
         * @param userMessage
         *            the new message text, not {@code null}
         * @throws NullPointerException
         *             if {@code userMessage} is {@code null}
         * @throws IllegalStateException
         *             if a postponed prompt has already been completed
         */
        public void setUserMessage(String userMessage) {
            checkNotCompleted();
            this.userMessage = Objects.requireNonNull(userMessage,
                    "User message must not be null");
        }

        /**
         * Gets the attachments as they currently stand — the submitted ones
         * until {@link #setAttachments(List)} replaces them.
         *
         * @return unmodifiable list of attachments with full data; empty when
         *         the message has no attachments, never {@code null}
         */
        public List<AIAttachment> getAttachments() {
            return attachments;
        }

        /**
         * Replaces the attachments sent to the LLM. The replacements are also
         * what appears in the message list. Pass an empty list to strip all
         * attachments.
         *
         * @param attachments
         *            the new attachments, not {@code null}; copied defensively
         * @throws NullPointerException
         *             if {@code attachments} is {@code null} or contains
         *             {@code null} elements
         * @throws IllegalStateException
         *             if a postponed prompt has already been completed
         */
        public void setAttachments(List<AIAttachment> attachments) {
            checkNotCompleted();
            this.attachments = List.copyOf(Objects.requireNonNull(attachments,
                    "Attachments must not be null"));
        }

        /**
         * Rejects the prompt without user-facing feedback: nothing is sent to
         * the LLM, nothing is added to the message list or the conversation
         * history, and neither {@link AIController#onRequest() controller} nor
         * {@link RequestListener} hooks fire. Rejection is final — later
         * content changes do not undo it.
         *
         * @throws IllegalStateException
         *             if a postponed prompt has already been completed
         */
        public void reject() {
            checkNotCompleted();
            rejected = true;
        }

        /**
         * Rejects the prompt like {@link #reject()} and shows the exchange in
         * the message list: the user's message and attachments as originally
         * submitted (unaffected by any replacements), followed by the given
         * message under the assistant name, so the user sees what was rejected
         * and why. Neither entry is added to the conversation history and
         * nothing is sent to the LLM. Without a configured message list nothing
         * is shown anywhere. When called multiple times, the last message wins.
         * <p>
         * Because both entries live only in the message list component, they
         * survive session serialization together with the rest of the UI, but
         * are absent from {@link AIOrchestrator#getHistory()} — a UI rebuilt
         * from saved history does not show past rejected exchanges.
         *
         * @param userFacingMessage
         *            the message to show to the user, not {@code null}
         * @throws NullPointerException
         *             if {@code userFacingMessage} is {@code null}
         * @throws IllegalStateException
         *             if a postponed prompt has already been completed
         */
        public void reject(String userFacingMessage) {
            checkNotCompleted();
            this.rejectionMessage = Objects.requireNonNull(userFacingMessage,
                    "User-facing message must not be null");
            rejected = true;
        }

        /**
         * Gets whether the prompt has been rejected.
         *
         * @return {@code true} when {@link #reject()} or
         *         {@link #reject(String)} has been called, {@code false}
         *         otherwise
         */
        public boolean isRejected() {
            return rejected;
        }

        /**
         * Gets the user-facing rejection message.
         *
         * @return the message passed to {@link #reject(String)}, or
         *         {@code null} when the prompt is not rejected or was rejected
         *         silently
         */
        public String getRejectionMessage() {
            return rejectionMessage;
        }

        /**
         * Postpones the prompt so preprocessing can finish asynchronously. The
         * prompt stays suspended — nothing is shown in the UI, no hooks fire,
         * and further prompts are ignored — until the returned continuation is
         * completed. The event can keep being changed after this method
         * returns; the verdict, including an earlier {@link #reject()}, is
         * applied when the prompt resumes. Make all changes on the thread that
         * completes the continuation, before completing it — a change racing a
         * concurrent completion (such as the timeout) is not reliably detected.
         * <p>
         * When the timeout elapses before the continuation is completed, the
         * prompt fails as if {@link RequestContinuation#fail(Throwable) failed}
         * with a {@link TimeoutException}. If the UI the prompt was submitted
         * from is detached before completion, the prompt is abandoned. The
         * prompt's content, including attachment data, stays referenced until
         * the continuation completes or the timeout fires — also when the UI is
         * detached in the meantime — so keep the timeout as tight as the work
         * allows. Throwing from the interceptor after postponing aborts the
         * prompt like any other interceptor failure and the returned
         * continuation becomes inert — completing it has no effect.
         *
         * @param timeout
         *            the maximum time to wait for the continuation to be
         *            completed, not {@code null}, positive
         * @return the continuation to complete, never {@code null}
         * @throws NullPointerException
         *             if {@code timeout} is {@code null}
         * @throws IllegalArgumentException
         *             if {@code timeout} is zero or negative
         * @throws IllegalStateException
         *             if the prompt is already postponed
         */
        public RequestContinuation postpone(Duration timeout) {
            Objects.requireNonNull(timeout, "Timeout must not be null");
            if (timeout.isZero() || timeout.isNegative()) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            if (continuation != null) {
                throw new IllegalStateException(
                        "The prompt is already postponed");
            }
            continuation = new RequestContinuation(timeout);
            return continuation;
        }

        /**
         * Returns the continuation created by {@link #postpone(Duration)}, or
         * {@code null} when the prompt is not postponed. Consulted by the
         * orchestrator after the interceptor returns.
         */
        RequestContinuation getContinuation() {
            return continuation;
        }

        private void checkNotCompleted() {
            if (continuation != null && continuation.isCompleted()) {
                throw new IllegalStateException("The postponed prompt has "
                        + "already been completed; complete all changes "
                        + "before proceed()");
            }
        }
    }

    /**
     * Handle for completing a prompt postponed via
     * {@link RequestInterceptEvent#postpone(Duration)}. Complete it from any
     * thread; completion is first-wins — once the prompt has proceeded, failed,
     * or timed out, later completions are ignored.
     */
    final class RequestContinuation implements Serializable {

        private static final Logger LOGGER = LoggerFactory
                .getLogger(RequestContinuation.class);

        private final Duration timeout;
        private boolean completed;
        private Throwable failure;
        private transient SerializableConsumer<Throwable> completionHandler;

        RequestContinuation(Duration timeout) {
            this.timeout = timeout;
        }

        /**
         * Resumes the postponed prompt with the event's current content. The
         * event must not be changed afterwards. A no-op when the prompt has
         * already been completed or timed out.
         */
        public void proceed() {
            complete(null);
        }

        /**
         * Aborts the postponed prompt: nothing is sent or shown, and the cause
         * is reported to the {@link ResponseListener} and
         * {@link AIController#onResponse(Throwable)}. Safe to call when the UI
         * is already detached: the {@link ResponseListener} is still notified,
         * only the UI-bound controller hook is skipped. A no-op when the prompt
         * has already been completed or timed out.
         *
         * @param cause
         *            the failure to report, not {@code null}
         * @throws NullPointerException
         *             if {@code cause} is {@code null}
         */
        public void fail(Throwable cause) {
            Objects.requireNonNull(cause, "Cause must not be null");
            complete(cause);
        }

        private void complete(Throwable cause) {
            SerializableConsumer<Throwable> handler;
            synchronized (this) {
                if (completed) {
                    LOGGER.debug("Ignoring completion of an already "
                            + "completed prompt continuation");
                    return;
                }
                completed = true;
                failure = cause;
                handler = completionHandler;
                completionHandler = null;
            }
            if (handler != null) {
                handler.accept(cause);
            }
        }

        synchronized boolean isCompleted() {
            return completed;
        }

        Duration getTimeout() {
            return timeout;
        }

        /**
         * Registers the orchestrator's resume callback, invoked exactly once
         * with the failure cause ({@code null} on {@link #proceed()}). Invoked
         * immediately on the current thread when the continuation is already
         * completed — the interceptor may complete it before the orchestrator
         * gets to attach; otherwise runs on whichever thread completes the
         * continuation.
         */
        void onComplete(SerializableConsumer<Throwable> handler) {
            synchronized (this) {
                if (!completed) {
                    completionHandler = handler;
                    return;
                }
            }
            handler.accept(failure);
        }
    }
}
