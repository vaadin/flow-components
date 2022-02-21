package com.vaadin.flow.component.map;

/**
 * Set of theme variants applicable for the {@code vaadin-map} component.
 */
public enum MapVariant {
    BORDERLESS("borderless");

    private final String variant;

    MapVariant(String variant) {
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
