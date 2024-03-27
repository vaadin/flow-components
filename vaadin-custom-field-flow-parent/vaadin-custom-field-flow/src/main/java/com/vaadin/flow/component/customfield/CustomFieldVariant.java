/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.customfield;

/**
 * The set of theme variants applicable to the {@code vaadin-custom-field}
 * component.
 */
public enum CustomFieldVariant {

    LUMO_SMALL("small"), LUMO_HELPER_ABOVE_FIELD(
            "helper-above-field"), LUMO_WHITESPACE("whitespace");

    private final String variant;

    CustomFieldVariant(String variant) {
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
