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
 * Listener for attachment click events.
 */
@FunctionalInterface
public interface AttachmentClickListener extends Serializable {
    /**
     * Called when an attachment in the message list is clicked.
     *
     * @param event
     *            the attachment click event
     */
    void onAttachmentClick(AttachmentClickEvent event);

    /**
     * Event fired when an attachment in the message list is clicked.
     */
    class AttachmentClickEvent implements Serializable {
        private final String messageId;
        private final int attachmentIndex;

        AttachmentClickEvent(String messageId, int attachmentIndex) {
            this.messageId = messageId;
            this.attachmentIndex = attachmentIndex;
        }

        /**
         * Gets the unique identifier for the message containing the clicked
         * attachment. This is the same ID provided in
         * {@link AttachmentSubmitListener.AttachmentSubmitEvent#getMessageId()}.
         *
         * @return the message ID
         */
        public String getMessageId() {
            return messageId;
        }

        /**
         * Gets the index of the clicked attachment within the message.
         *
         * @return the 0-based attachment index
         */
        public int getAttachmentIndex() {
            return attachmentIndex;
        }
    }
}
