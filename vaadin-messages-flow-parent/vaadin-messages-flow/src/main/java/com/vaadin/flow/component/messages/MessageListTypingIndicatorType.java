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

/**
 * The styles in which {@link MessageList} can render its typing indicator.
 *
 * @see MessageList#setTypingIndicatorType(MessageListTypingIndicatorType)
 * @since 25.3
 */
public enum MessageListTypingIndicatorType {
    /**
     * Renders the typing indicator like a message, with the avatars and names
     * of the typing users in the header and the typing indicator text as the
     * message content.
     */
    DEFAULT(""),
    /**
     * Renders the typing indicator like a message, with the avatars of the
     * typing users and an animated ellipsis as the message content. The names
     * of the typing users and the typing indicator text are not shown.
     */
    ELLIPSIS("ellipsis"),
    /**
     * Renders the typing indicator as a single compact row with small avatars,
     * followed by the names of the typing users and the typing indicator text.
     */
    MINIMAL("minimal");

    private final String typeName;

    MessageListTypingIndicatorType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Gets the value that the web component uses for this type.
     *
     * @return the type name, empty for {@link #DEFAULT}
     */
    String getTypeName() {
        return typeName;
    }
}
