/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.select;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-select} component.
 */
public enum SelectVariant implements ThemeVariant {
    LUMO_SMALL("small"),
    LUMO_ALIGN_LEFT("align-left"),
    LUMO_ALIGN_CENTER("align-center"),
    LUMO_ALIGN_RIGHT("align-right"),
    LUMO_HELPER_ABOVE_FIELD("helper-above-field"),
    /**
     * @deprecated Since 24.7, the Material theme is deprecated and will be
     *             removed in Vaadin 25.
     */
    @Deprecated
    MATERIAL_ALWAYS_FLOAT_LABEL("always-float-label");

    private final String variant;

    SelectVariant(String variant) {
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
