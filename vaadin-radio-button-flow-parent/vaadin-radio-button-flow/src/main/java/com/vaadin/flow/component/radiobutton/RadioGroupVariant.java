/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton;

/**
 * Set of theme variants applicable for {@code vaadin-radio-group} component.
 */
public enum RadioGroupVariant {
    LUMO_VERTICAL("vertical"), LUMO_HELPER_ABOVE_FIELD(
            "helper-above-field"), MATERIAL_VERTICAL("vertical");

    private final String variant;

    RadioGroupVariant(String variant) {
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
