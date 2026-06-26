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

/**
 * Listener for LLM response events.
 * <p>
 * The listener is called once per turn — when the assistant's stream has
 * completed, whether successfully or with an error. The same lifecycle moment
 * as {@link AIController#onResponse(Throwable)}. Use it to persist conversation
 * state (via {@link AIOrchestrator#getHistory()}), trigger follow-up actions,
 * or surface errors to the user.
 * <p>
 * On success the response text may still be empty if the model emitted only
 * tool calls or stopped without producing visible content. Such turns are
 * successful exchanges; check {@code event.getResponse().isEmpty()} if the
 * listener should only react to text-bearing responses. Empty responses are
 * <i>not</i> appended to {@link AIOrchestrator#getHistory()}.
 * <p>
 * On failure {@link ResponseEvent#getError()} carries the cause (timeout,
 * stream error, or any throw between {@link AIController#onRequest()} and the
 * start of the stream); the response text is either empty or a partial stream
 * that was received before the failure.
 * <p>
 * The listener is <b>not</b> called when history is restored via
 * {@code Builder.withHistory()}.
 * <p>
 * <b>Threading:</b> the listener is called from a background thread (Reactor
 * scheduler). Blocking I/O (e.g. database writes) is safe directly. To update
 * Vaadin UI components from this listener, use {@code ui.access()}.
 * 
 * @since 25.2
 */
@FunctionalInterface
public interface ResponseListener extends Serializable {

    /**
     * Called when the assistant's stream has completed.
     *
     * @param event
     *            the response event
     */
    void onResponse(ResponseEvent event);

    /**
     * Event fired after the assistant's stream has completed, on success or
     * failure.
     */
    class ResponseEvent implements Serializable {
        private final String response;
        private final Throwable error;

        ResponseEvent(String response, Throwable error) {
            this.response = response;
            this.error = error;
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
    }
}
