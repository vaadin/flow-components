/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

/**
 * The set of theme variants applicable to the {@code vaadin-crud} component.
 */
public enum CrudVariant {
    /**
     * @deprecated Use {@link #LUMO_NO_BORDER} or {@link #AURA_NO_BORDER}
     *             instead.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    NO_BORDER("no-border"),
    LUMO_NO_BORDER("no-border"),
    AURA_NO_BORDER("no-border");

    private final String variant;

    CrudVariant(String variant) {
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
