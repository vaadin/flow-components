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
 * Listener for attachment submit events.
 */
@FunctionalInterface
public interface AttachmentSubmitListener extends Serializable {
    /**
     * Called when a message with attachments is submitted to the LLM provider.
     * Use this to store attachment data in your own storage.
     *
     * @param event
     *            the attachment submit event
     */
    void onAttachmentSubmit(AttachmentSubmitEvent event);

    /**
     * Event fired when a message with attachments is submitted to the LLM
     * provider. Contains full attachment data and a unique message ID that can
     * be used to identify the message later, both when an attachment is clicked
     * ({@link AttachmentClickListener}) and when restoring conversation history
     * ({@link com.vaadin.flow.component.ai.common.ChatMessage#messageId()}).
     */
    class AttachmentSubmitEvent implements Serializable {
        private final String messageId;
        private final List<AIAttachment> attachments;

        AttachmentSubmitEvent(String messageId,
                List<AIAttachment> attachments) {
            this.messageId = messageId;
            this.attachments = attachments;
        }

        /**
         * Gets the unique identifier for the message that these attachments
         * belong to. This is the same ID available via
         * {@link com.vaadin.flow.component.ai.common.ChatMessage#messageId()}
         * in the conversation history and via
         * {@link AttachmentClickListener.AttachmentClickEvent#getMessageId()}
         * on attachment click.
         *
         * @return the message ID
         */
        public String getMessageId() {
            return messageId;
        }

        /**
         * Gets the attachments included with the message.
         *
         * @return unmodifiable list of attachments with full data
         */
        public List<AIAttachment> getAttachments() {
            return attachments;
        }
    }
}
