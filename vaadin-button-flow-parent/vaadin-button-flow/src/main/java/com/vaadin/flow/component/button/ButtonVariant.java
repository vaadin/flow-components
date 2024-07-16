/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.button;

/**
 * Set of theme variants applicable for {@code vaadin-button} component.
 */
public enum ButtonVariant {
    LUMO_SMALL("small"), LUMO_LARGE("large"), LUMO_TERTIARY(
            "tertiary"), LUMO_TERTIARY_INLINE("tertiary-inline"), LUMO_PRIMARY(
                    "primary"), LUMO_SUCCESS("success"), LUMO_ERROR(
                            "error"), LUMO_CONTRAST("contrast"), LUMO_ICON(
                                    "icon"), MATERIAL_CONTAINED(
                                            "contained"), MATERIAL_OUTLINED(
                                                    "outlined");

    private final String variant;

    ButtonVariant(String variant) {
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