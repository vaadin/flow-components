/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.details;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * The set of theme variants applicable to the {@code vaadin-details} component.
 */
public enum DetailsVariant implements ThemeVariant {

    FILLED("filled"), REVERSE("reverse"), SMALL("small");

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
