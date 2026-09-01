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
 * Set of theme variants applicable for {@code vaadin-message-list} component.
 *
 * @since 25.3
 */
public enum MessageListVariant implements ThemeVariant {
    /**
     * Shows the messages as chat bubbles.
     */
    BUBBLE("bubble"),
    /**
     * Hides the avatar and the name of every message, for a chat between two
     * participants. Only takes effect together with {@link #BUBBLE}.
     */
    ONE_TO_ONE("one-to-one");

    private final String variant;

    MessageListVariant(String variant) {
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
