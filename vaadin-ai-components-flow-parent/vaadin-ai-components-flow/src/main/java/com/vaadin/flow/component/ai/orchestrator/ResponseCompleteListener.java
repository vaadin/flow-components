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

/**
 * Listener for LLM response completion events.
 * <p>
 * The listener is called after each successful exchange — when the assistant's
 * stream has completed without error. This is the recommended hook for
 * persisting conversation state (via {@link AIOrchestrator#getHistory()}),
 * triggering follow-up actions, or updating UI elements.
 * <p>
 * The response text may be empty if the model emitted only tool calls or
 * stopped without producing visible content. Such turns are still successful
 * exchanges; check {@code event.getResponse().isEmpty()} if your listener
 * should only react to text-bearing responses. Empty responses are <i>not</i>
 * appended to {@link AIOrchestrator#getHistory()}.
 * <p>
 * The listener is <b>not</b> called when:
 * <ul>
 * <li>The LLM response fails with an error or times out</li>
 * <li>History is restored via {@code Builder.withHistory()}</li>
 * </ul>
 * <p>
 * <b>Threading:</b> This listener is called from a background thread (Reactor
 * scheduler). It is safe to perform blocking I/O (e.g. database writes)
 * directly. To update Vaadin UI components from this listener, use
 * {@code ui.access()}.
 */
@FunctionalInterface
public interface ResponseCompleteListener extends Serializable {
    /**
     * Called when the assistant's stream has completed without error.
     *
     * @param event
     *            the response complete event
     */
    void onResponseComplete(ResponseCompleteEvent event);

    /**
     * Event fired after the assistant's stream has completed without error.
     */
    class ResponseCompleteEvent implements Serializable {
        private final String response;

        ResponseCompleteEvent(String response) {
            this.response = response;
        }

        /**
         * Gets the full text of the assistant's response. May be empty when the
         * model emitted only tool calls or otherwise produced no visible
         * content.
         *
         * @return the response text, never {@code null}
         */
        public String getResponse() {
            return response;
        }
    }
}
