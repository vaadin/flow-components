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
package com.vaadin.flow.component.details;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * The set of theme variants applicable to the {@code vaadin-details} component.
 */
public enum DetailsVariant implements ThemeVariant {
    /**
     * @deprecated Use {@link #LUMO_FILLED} or {@link #AURA_FILLED} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    FILLED("filled"),
    /**
     * @deprecated Use {@link #LUMO_REVERSE} or {@link #AURA_REVERSE} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    REVERSE("reverse"),
    /**
     * @deprecated Use {@link #LUMO_SMALL} or {@link #AURA_SMALL} instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    SMALL("small"),
    LUMO_FILLED("filled"),
    AURA_FILLED("filled"),
    LUMO_REVERSE("reverse"),
    AURA_REVERSE("reverse"),
    LUMO_SMALL("small"),
    AURA_SMALL("small"),
    AURA_NO_PADDING("no-padding");

    private final String variant;

    DetailsVariant(String variant) {
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
