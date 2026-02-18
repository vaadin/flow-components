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
package com.vaadin.flow.component.ai.ui;

import java.io.Serializable;
import java.util.List;

import com.vaadin.flow.component.ai.common.AIAttachment;

/**
 * Interface for message list components that can display AI conversation
 * messages.
 *
 * @author Vaadin Ltd
 * @see AIMessage
 */
public interface AIMessageList extends Serializable {

    /**
     * Callback for attachment click events in the message list.
     */
    @FunctionalInterface
    interface AttachmentClickCallback extends Serializable {
        /**
         * Called when an attachment in the message list is clicked.
         *
         * @param message
         *            the message containing the clicked attachment
         * @param attachmentIndex
         *            the 0-based index of the clicked attachment within the
         *            message
         */
        void onAttachmentClick(AIMessage message, int attachmentIndex);
    }

    /**
     * Creates a new message with the given parameters and attachments. Creates
     * a new message with the given parameters and attachments and adds it to
     * the list.
     *
     * @param text
     *            the initial message text
     * @param userName
     *            the name of the message sender, not {@code null}
     * @param attachments
     *            the list of attachments to include with the message
     * @return the created message instance, not {@code null}
     */
    AIMessage addMessage(String text, String userName,
            List<AIAttachment> attachments);

    /**
     * Adds a listener that is called when an attachment in the message list is
     * clicked.
     *
     * @param callback
     *            the callback to invoke on attachment click
     */
    void addAttachmentClickListener(AttachmentClickCallback callback);
}
