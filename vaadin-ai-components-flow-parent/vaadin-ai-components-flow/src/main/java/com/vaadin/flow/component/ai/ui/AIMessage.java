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
import java.time.Instant;

/**
 * Represents a message in an AI conversation.
 *
 * @author Vaadin Ltd
 * @see AIMessageList
 */
public interface AIMessage extends Serializable {

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
     *            the message text to set
     */
    void setText(String text);

    /**
     * Gets the timestamp of the message.
     *
     * @return the timestamp when the message was created
     */
    Instant getTime();

    /**
     * Sets the timestamp of the message.
     *
     * @param time
     *            the timestamp to set
     */
    void setTime(Instant time);

    /**
     * Gets the name of the message sender.
     *
     * @return the sender name
     */
    String getUserName();

    /**
     * Appends text to the existing message content.
     * <p>
     * This method is used for streaming AI responses where tokens are appended
     * incrementally as they arrive.
     *
     * @param token
     *            the text to append
     */
    void appendText(String token);
}
