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
package com.vaadin.flow.component.card;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-card} component.
 */
public enum CardVariant implements ThemeVariant {
    LUMO_ELEVATED("elevated"),
    LUMO_OUTLINED("outlined"),
    LUMO_HORIZONTAL("horizontal"),
    LUMO_STRETCH_MEDIA("stretch-media"),
    LUMO_COVER_MEDIA("cover-media"),
    /**
     * @deprecated Use {@link #ELEVATED} instead.
     */
    @Deprecated
    AURA_ELEVATED("elevated"),
    /**
     * @deprecated Use {@link #OUTLINED} instead.
     */
    @Deprecated
    AURA_OUTLINED("outlined"),
    /**
     * @deprecated Use {@link #HORIZONTAL} instead.
     */
    @Deprecated
    AURA_HORIZONTAL("horizontal"),
    /**
     * @deprecated Use {@link #STRETCH_MEDIA} instead.
     */
    @Deprecated
    AURA_STRETCH_MEDIA("stretch-media"),
    /**
     * @deprecated Use {@link #COVER_MEDIA} instead.
     */
    @Deprecated
    AURA_COVER_MEDIA("cover-media"),
    ELEVATED("elevated"),
    OUTLINED("outlined"),
    HORIZONTAL("horizontal"),
    STRETCH_MEDIA("stretch-media"),
    COVER_MEDIA("cover-media");

    private final String variant;

    CardVariant(String variant) {
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
