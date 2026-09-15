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
package com.vaadin.flow.component.messages;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The internationalization properties for {@link MessageList}.
 *
 * @author Vaadin Ltd.
 * @see MessageList#setI18n(MessageListI18n)
 * @since 25.3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListI18n implements Serializable {
    private String typingIndicatorText;

    /**
     * Gets the text that is displayed in the typing indicator next to the names
     * of the users that are currently typing.
     *
     * @return the typing indicator text, or {@code null} if not set, in which
     *         case the default text {@code Typing…} is displayed
     */
    public String getTypingIndicatorText() {
        return typingIndicatorText;
    }

    /**
     * Sets the text that is displayed in the typing indicator next to the names
     * of the users that are currently typing. By default, the text
     * {@code Typing…} is displayed.
     *
     * @param typingIndicatorText
     *            the typing indicator text, or {@code null} to use the default
     *            text
     * @return this instance for method chaining
     */
    public MessageListI18n setTypingIndicatorText(String typingIndicatorText) {
        this.typingIndicatorText = typingIndicatorText;
        return this;
    }
}
