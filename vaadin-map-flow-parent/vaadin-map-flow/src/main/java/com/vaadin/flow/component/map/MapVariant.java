/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for the {@code vaadin-map} component.
 */
public enum MapVariant implements ThemeVariant {
    /**
     * @deprecated Use {@link #LUMO_NO_BORDER} or {@link #AURA_NO_BORDER}
     *             instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    BORDERLESS("no-border"),
    LUMO_NO_BORDER("no-border"),
    AURA_NO_BORDER("no-border");

    private final String variant;

    MapVariant(String variant) {
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
