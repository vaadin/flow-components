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
package com.vaadin.flow.component.orderedlayout;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-vertical-layout}
 * component.
 */
public enum VerticalLayoutVariant implements ThemeVariant {
    LUMO_MARGIN("margin"),
    LUMO_PADDING("padding"),
    LUMO_SPACING_XS("spacing-xs"),
    LUMO_SPACING_S("spacing-s"),
    LUMO_SPACING("spacing"),
    LUMO_SPACING_L("spacing-l"),
    LUMO_SPACING_XL("spacing-xl"),
    LUMO_WRAP("wrap"),
    /**
     * @deprecated Use {@link #MARGIN} instead.
     */
    @Deprecated
    AURA_MARGIN("margin"),
    /**
     * @deprecated Use {@link #PADDING} instead.
     */
    @Deprecated
    AURA_PADDING("padding"),
    /**
     * @deprecated Use {@link #SPACING} instead.
     */
    @Deprecated
    AURA_SPACING("spacing"),
    /**
     * @deprecated Use {@link #WRAP} instead.
     */
    @Deprecated
    AURA_WRAP("wrap"),
    MARGIN("margin"),
    PADDING("padding"),
    SPACING("spacing"),
    WRAP("wrap");

    private final String variant;

    VerticalLayoutVariant(String variant) {
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
