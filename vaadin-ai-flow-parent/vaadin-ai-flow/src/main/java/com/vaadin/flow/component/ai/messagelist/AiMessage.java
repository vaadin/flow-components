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
import java.time.Instant;

import com.vaadin.flow.component.Component;

/**
 * Represents a message in an AI conversation.
 *
 * @author Vaadin Ltd
 */
public interface AiMessage extends Serializable {

    /**
     * Gets the message text.
     *
     * @return the message text
     */
    String getText();

    /**
     * Sets the message text.
     *
     * @param text
     *            the message text
     */
    void setText(String text);

    /**
     * Gets the timestamp of the message.
     *
     * @return the timestamp
     */
    Instant getTime();

    /**
     * Gets the name of the message sender.
     *
     * @return the sender name
     */
    String getUserName();

    /**
     * Appends text to the existing message content.
     *
     * @param token
     *            the text to append
     */
    void appendText(String token);

    void setPrefix(Component component);
}
