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
package com.vaadin.flow.component.menubar;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-menu-bar} component.
 */
public enum MenuBarVariant implements ThemeVariant {
    LUMO_SMALL("small"),
    LUMO_LARGE("large"),
    LUMO_TERTIARY("tertiary"),
    LUMO_TERTIARY_INLINE("tertiary-inline"),
    LUMO_PRIMARY("primary"),
    LUMO_CONTRAST("contrast"),
    LUMO_ICON("icon"),
    LUMO_END_ALIGNED("end-aligned"),
    LUMO_DROPDOWN_INDICATORS("dropdown-indicators"),
    /**
     * @deprecated Use {@link #ALIGN_END} instead.
     */
    @Deprecated
    AURA_END_ALIGNED("end-aligned"),
    /**
     * @deprecated Use {@link #PRIMARY} instead.
     */
    @Deprecated
    AURA_PRIMARY("primary"),
    /**
     * @deprecated Use {@link #TERTIARY} instead.
     */
    @Deprecated
    AURA_TERTIARY("tertiary"),
    PRIMARY("primary"),
    TERTIARY("tertiary"),
    SUCCESS("success"),
    WARNING("warning"),
    ERROR("error"),
    ALIGN_END("end-aligned"),
    SMALL("small"),
    LARGE("large");

    private final String variant;

    MenuBarVariant(String variant) {
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
