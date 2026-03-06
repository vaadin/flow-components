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
package com.vaadin.flow.component.virtuallist;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-virtual-list} component.
 */
public enum VirtualListVariant implements ThemeVariant {
    LUMO_OVERFLOW_INDICATORS("overflow-indicators"),
    LUMO_OVERFLOW_INDICATOR_TOP("overflow-indicator-top"),
    LUMO_OVERFLOW_INDICATOR_BOTTOM("overflow-indicator-bottom"),
    /**
     * @deprecated Use {@link #OVERFLOW_INDICATORS} instead.
     */
    @Deprecated
    AURA_OVERFLOW_INDICATORS("overflow-indicators"),
    /**
     * @deprecated Use {@link #OVERFLOW_INDICATOR_TOP} instead.
     */
    @Deprecated
    AURA_OVERFLOW_INDICATOR_TOP("overflow-indicator-top"),
    /**
     * @deprecated Use {@link #OVERFLOW_INDICATOR_BOTTOM} instead.
     */
    @Deprecated
    AURA_OVERFLOW_INDICATOR_BOTTOM("overflow-indicator-bottom"),
    OVERFLOW_INDICATORS("overflow-indicators"),
    OVERFLOW_INDICATOR_TOP("overflow-indicator-top"),
    OVERFLOW_INDICATOR_BOTTOM("overflow-indicator-bottom");

    private final String variant;

    VirtualListVariant(String variant) {
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
