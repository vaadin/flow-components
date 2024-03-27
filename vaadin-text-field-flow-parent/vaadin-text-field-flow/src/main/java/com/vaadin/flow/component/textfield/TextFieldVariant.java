/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for the {@link TextField} component, as well
 * as other components based on it.
 */
public enum TextFieldVariant implements ThemeVariant {
    //@formatter:off
    LUMO_SMALL("small"),
    LUMO_ALIGN_CENTER("align-center"),
    LUMO_ALIGN_RIGHT("align-right"),
    LUMO_HELPER_ABOVE_FIELD("helper-above-field"),
    MATERIAL_ALWAYS_FLOAT_LABEL("always-float-label");
    //@formatter:on

    private final String variant;

    TextFieldVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
