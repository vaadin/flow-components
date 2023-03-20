/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.dialog;

/**
 * Set of theme variants applicable for {@code vaadin-dialog} component.
 */
public enum DialogVariant {
    LUMO_NO_PADDING("no-padding"), MATERIAL_NO_PADDING("no-padding");

    private final String variant;

    DialogVariant(String variant) {
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
