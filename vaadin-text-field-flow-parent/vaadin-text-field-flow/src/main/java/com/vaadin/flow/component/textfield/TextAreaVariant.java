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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-text-area} component.
 */
public enum TextAreaVariant implements ThemeVariant {
    LUMO_SMALL("small"),
    LUMO_ALIGN_CENTER("align-center"),
    LUMO_ALIGN_RIGHT("align-right"),
    LUMO_HELPER_ABOVE_FIELD("helper-above-field"),
    /**
     * @deprecated Use {@link #ALIGN_LEFT} instead.
     */
    @Deprecated
    AURA_ALIGN_LEFT("align-left"),
    /**
     * @deprecated Use {@link #ALIGN_CENTER} instead.
     */
    @Deprecated
    AURA_ALIGN_CENTER("align-center"),
    /**
     * @deprecated Use {@link #ALIGN_RIGHT} instead.
     */
    @Deprecated
    AURA_ALIGN_RIGHT("align-right"),
    /**
     * @deprecated Use {@link #ALIGN_START} instead.
     */
    @Deprecated
    AURA_ALIGN_START("align-start"),
    /**
     * @deprecated Use {@link #ALIGN_END} instead.
     */
    @Deprecated
    AURA_ALIGN_END("align-end"),
    /**
     * @deprecated Use {@link #HELPER_ABOVE} instead.
     */
    @Deprecated
    AURA_HELPER_ABOVE_FIELD("helper-above-field"),
    ALIGN_LEFT("align-left"),
    ALIGN_CENTER("align-center"),
    ALIGN_RIGHT("align-right"),
    ALIGN_START("align-start"),
    ALIGN_END("align-end"),
    HELPER_ABOVE("helper-above-field"),
    SMALL("small");

    private final String variant;

    TextAreaVariant(String variant) {
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
