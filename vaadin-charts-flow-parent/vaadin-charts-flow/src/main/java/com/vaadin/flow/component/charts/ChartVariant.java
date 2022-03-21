/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
