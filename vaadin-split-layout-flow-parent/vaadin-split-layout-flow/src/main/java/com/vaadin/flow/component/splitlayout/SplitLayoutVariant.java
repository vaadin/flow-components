/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.splitlayout;

/**
 * Set of theme variants applicable for {@code vaadin-split-layout} component.
 */
public enum SplitLayoutVariant {
    LUMO_SMALL("small"), LUMO_MINIMAL("minimal");

    private final String variant;

    SplitLayoutVariant(String variant) {
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