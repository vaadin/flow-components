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
import java.util.Optional;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.ResponseMetadata;

/**
 * Listener for LLM response events.
 * <p>
 * The listener is called when the turn ends — normally when the assistant's
 * stream has completed, whether successfully or with an error, but also when
 * the turn fails before a stream ever opens. It fires at most once per prompt:
 * a prompt rejected by the {@link RequestInterceptor} and a postponed prompt
 * abandoned because its UI was detached end without firing it. The same
 * lifecycle moment as {@link AIController#onResponse(Throwable)}. Use it to
 * persist conversation state (via {@link AIOrchestrator#getHistory()}), trigger
 * follow-up actions, or surface errors to the user.
 * <p>
 * On success the response text may still be empty if the model emitted only
 * tool calls or stopped without producing visible content. Such turns are
 * successful exchanges; check {@code event.getResponse().isEmpty()} if the
 * listener should only react to text-bearing responses. Empty responses are
 * <i>not</i> appended to {@link AIOrchestrator#getHistory()}.
 * <p>
 * On failure {@link ResponseEvent#getError()} carries the cause (timeout,
 * stream error, any throw between {@link AIController#onRequest()} and the
 * start of the stream, or a {@link RequestInterceptor} failure — a throw, a
 * {@link RequestInterceptor.RequestContinuation#fail(Throwable) fail}, or an
 * interception timeout); the response text is either empty or a partial stream
 * that was received before the failure. An interceptor failure fires the
 * listener without a preceding {@link AIController#onRequest()}, so an error
 * does not imply that per-turn setup has happened.
 * <p>
 * The listener is <b>not</b> called when history is restored via
 * {@code Builder.withHistory()}.
 * <p>
 * <b>Threading:</b> the listener is called from whichever thread ends the turn.
 * Stream completion and stream errors arrive on a background thread — where
 * blocking I/O (e.g. database writes) is safe — unless the provider runs the
 * turn on the thread that triggered the prompt, in which case the listener runs
 * there too and blocking prolongs the current request. That is the case for a
 * non-streaming provider with background execution disabled, which is the
 * default. Interception timeouts arrive on a blocking-tolerant Reactor thread,
 * synchronous failures on the UI thread, and a postponed prompt completes on
 * the application's own thread. To update Vaadin UI components from this
 * listener, use {@code ui.access()}.
 * 
 * @since 25.2
 */
@FunctionalInterface
public interface ResponseListener extends Serializable {

    /**
     * Called when the turn has ended (see the class documentation for the exact
     * moments).
     *
     * @param event
     *            the response event
     */
    void onResponse(ResponseEvent event);

    /**
     * Event fired when a turn ends, on success or failure.
     */
    class ResponseEvent implements Serializable {
        private final String response;
        private final Throwable error;
        private final ResponseMetadata metadata;

        ResponseEvent(String response, Throwable error,
                ResponseMetadata metadata) {
            this.response = response;
            this.error = error;
            this.metadata = metadata;
        }

        /**
         * Gets the assistant's response text. On success this is the full text
         * (may be empty when the model emitted only tool calls); on failure
         * this is whatever partial stream was received before the error,
         * possibly empty.
         *
         * @return the response text, never {@code null}
         */
        public String getResponse() {
            return response;
        }

        /**
         * Gets the failure cause if the turn ended with an error. Returns an
         * empty optional on a successful turn.
         *
         * @return the failure cause, or empty on success
         */
        public Optional<Throwable> getError() {
            return Optional.ofNullable(error);
        }

        /**
         * Gets the metadata the provider reported for this turn, such as the
         * finish reason and token usage. On a failed turn this is what was
         * observed before the failure. Returns an empty optional when the
         * provider reported none — a custom {@link LLMProvider} that does not
         * publish metadata, or a turn that failed before any was observed.
         *
         * @return the response metadata, or empty when the provider reported
         *         none
         * @since 25.3
         */
        public Optional<ResponseMetadata> getMetadata() {
            return Optional.ofNullable(metadata);
        }
    }
}
