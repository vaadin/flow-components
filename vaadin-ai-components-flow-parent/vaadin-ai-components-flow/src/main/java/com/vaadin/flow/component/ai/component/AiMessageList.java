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
package com.vaadin.flow.component.ai.component;

import java.io.Serializable;
import java.util.List;

import com.vaadin.flow.component.ai.common.AiAttachment;

/**
 * Interface for message list components that can display AI conversation
 * messages.
 *
 * @author Vaadin Ltd
 * @see AiMessage
 */
public interface AiMessageList extends Serializable {

    /**
     * Adds a message to the list.
     *
     * @param message
     *            the message to add, not {@code null}
     */
    void addMessage(AiMessage message);

    /**
     * Creates a new message with the given parameters and attachments.
     *
     * @param text
     *            the initial message text
     * @param userName
     *            the name of the message sender, not {@code null}
     * @param attachments
     *            the list of attachments to include with the message
     * @return the created message instance, not {@code null}
     */
    AiMessage createMessage(String text, String userName,
            List<AiAttachment> attachments);
}
