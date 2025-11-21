/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.messagelist;

import java.io.Serializable;

/**
 * Interface for components that can display a list of messages for AI
 * orchestrators.
 * <p>
 * Components implementing this interface can display messages in a conversation
 * format, allowing the AI orchestrator to add, update, and manage messages.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface AiMessageList extends Serializable {

    /**
     * Adds a message to the list.
     *
     * @param message
     *            the message to add
     */
    void addMessage(AiMessage message);

    /**
     * Updates an existing message in the list.
     *
     * @param message
     *            the message to update
     */
    void updateMessage(AiMessage message);

    /**
     * Creates a new message with the given parameters.
     *
     * @param text
     *            the message text
     * @param userName
     *            the user name
     * @return the created message
     */
    AiMessage createMessage(String text, String userName);
    
    /**
     * Sets whether the message list should render messages in Markdown format.
     *
     * @param b
     *            true to enable Markdown rendering, false to disable
     */
    void setMarkdown(boolean markdown);
}
