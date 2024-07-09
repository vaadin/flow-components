/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.progressbar;

/**
 * Set of theme variants applicable for {@code vaadin-progress-bar} component.
 */
public enum ProgressBarVariant {
    LUMO_CONTRAST("contrast"), LUMO_ERROR("error"), LUMO_SUCCESS("success");

    private final String variant;

    ProgressBarVariant(String variant) {
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