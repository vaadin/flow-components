/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * The set of theme variants applicable to the {@code vaadin-dashboard} and
 * {@code vaadin-dashboard-layout} components.
 */
public enum DashboardVariant implements ThemeVariant {

    LUMO_SHADED_BACKGROUND("shaded-background"),
    LUMO_ELEVATED_WIDGETS("elevated-widgets"),
    LUMO_FLAT_WIDGETS("flat-widgets");

    private final String variant;

    DashboardVariant(String variant) {
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
