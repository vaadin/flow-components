/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.messages;

import java.io.Serializable;

/**
 * The internationalization properties for {@link MessageInput}. This can be
 * used to customize and translate the language used in the message input
 * component.
 *
 * @see MessageInput#setI18n(MessageInputI18n)
 *
 * @author Vaadin Ltd.
 */
public class MessageInputI18n implements Serializable {
    private String message;
    private String send;

    /**
     * Gets the translated word for {@code message}.
     *
     * @return the translated word for message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the translated word for {@code message}.
     * <p>
     * This word is used as the placeholder and aria-label for the input field
     * where the user can type a new message.
     *
     * @param message
     *            the translated word for message
     * @return this instance for method chaining
     */
    public MessageInputI18n setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the translated word for {@code send}.
     *
     * @return the translated word for send
     */
    public String getSend() {
        return send;
    }

    /**
     * Sets the translated word for {@code send}.
     * <p>
     * This word is used as the text content of the button for submitting new
     * messages.
     *
     * @param send
     *            the translated word for send
     * @return this instance for method chaining
     */
    public MessageInputI18n setSend(String send) {
        this.send = send;
        return this;
    }
}
