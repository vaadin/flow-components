/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
