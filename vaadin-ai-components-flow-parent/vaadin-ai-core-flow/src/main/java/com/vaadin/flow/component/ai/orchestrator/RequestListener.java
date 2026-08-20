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
import java.util.List;

import com.vaadin.flow.component.ai.common.AIAttachment;

/**
 * Listener for request submission events.
 * <p>
 * The listener is called on every prompt, just before the LLM stream opens —
 * the same lifecycle moment as {@link AIController#onRequest()}. Use it to
 * persist the outbound request (the user message, any attachments) or to
 * correlate the assigned {@code messageId} with downstream events such as
 * {@link AttachmentClickListener} and the {@code messageId} stored in
 * {@link com.vaadin.flow.component.ai.common.ChatMessage#messageId()}.
 * <p>
 * <b>Threading:</b> the listener is called on the UI thread under the session
 * lock. Long-running work should be offloaded to a worker; UI updates do not
 * need an extra {@code ui.access(...)} hop.
 * 
 * @since 25.2
 */
@FunctionalInterface
public interface RequestListener extends Serializable {

    /**
     * Called just before the LLM stream opens for a prompt.
     *
     * @param event
     *            the request event
     */
    void onRequest(RequestEvent event);

    /**
     * Event fired just before the LLM stream opens for a prompt. Carries the
     * user message, the assigned {@code messageId} that will be stored on the
     * resulting {@link com.vaadin.flow.component.ai.common.ChatMessage}, and
     * the attachments included with the message (empty list when none).
     */
    class RequestEvent implements Serializable {
        private final String userMessage;
        private final String messageId;
        private final List<AIAttachment> attachments;

        RequestEvent(String userMessage, String messageId,
                List<AIAttachment> attachments) {
            this.userMessage = userMessage;
            this.messageId = messageId;
            this.attachments = attachments;
        }

        /**
         * Gets the user message being submitted to the LLM.
         *
         * @return the user message, never {@code null}
         */
        public String getUserMessage() {
            return userMessage;
        }

        /**
         * Gets the unique identifier assigned to this message. The same id is
         * available via
         * {@link com.vaadin.flow.component.ai.common.ChatMessage#messageId()}
         * in the conversation history and via
         * {@link AttachmentClickListener.AttachmentClickEvent#getMessageId()}
         * on attachment click.
         *
         * @return the message id, never {@code null}
         */
        public String getMessageId() {
            return messageId;
        }

        /**
         * Gets the attachments included with the request.
         *
         * @return unmodifiable list of attachments with full data; empty when
         *         the message has no attachments, never {@code null}
         */
        public List<AIAttachment> getAttachments() {
            return attachments;
        }
    }
}
