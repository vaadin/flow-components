/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts;

/**
 * Set of theme variants applicable for {@code vaadin-chart} component.
 */
public enum ChartVariant {
    LUMO_GRADIENT("gradient"), LUMO_MONOTONE("monotone"), LUMO_CLASSIC(
            "classic"), MATERIAL_GRADIENT("gradient"), MATERIAL_MONOTONE(
                    "monotone"), MATERIAL_CLASSIC("classic");

    private final String variant;

    ChartVariant(String variant) {
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
