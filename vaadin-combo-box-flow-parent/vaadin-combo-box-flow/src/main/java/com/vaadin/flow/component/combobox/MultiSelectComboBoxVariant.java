/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-multi-select-combo-box}
 * component.
 */
public enum MultiSelectComboBoxVariant implements ThemeVariant {
    //@formatter:off
    LUMO_SMALL("small"),
    LUMO_ALIGN_LEFT("align-left"),
    LUMO_ALIGN_CENTER("align-center"),
    LUMO_ALIGN_RIGHT("align-right"),
    LUMO_HELPER_ABOVE_FIELD("helper-above-field"),
    MATERIAL_ALWAYS_FLOAT_LABEL("always-float-label");
    //@formatter:on

    private final String variant;

    MultiSelectComboBoxVariant(String variant) {
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
