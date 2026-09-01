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

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for a single message inside a
 * {@link MessageList}.
 *
 * @see MessageListItem#addThemeVariants(MessageListItemVariant...)
 * @since 25.3
 */
public enum MessageListItemVariant implements ThemeVariant {
    /**
     * Shows the message as sent by the current user, and hides the avatar and
     * the name of the message. Only takes effect when the message list uses
     * {@link MessageListVariant#BUBBLE}.
     */
    SELF("self"),
    /**
     * Removes the bubble and the width restriction from the message, for an
     * assistant or AI response. Only takes effect when the message list uses
     * {@link MessageListVariant#BUBBLE}.
     */
    FULL_WIDTH("full-width");

    private final String variant;

    MessageListItemVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Gets the variant name.
     *
     * @return variant name
     */
    public String getVariantName() {
        return variant;
    }
}
