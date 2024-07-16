/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar;

/**
 * Set of theme variants applicable for {@code vaadin-menu-bar} component.
 */
public enum MenuBarVariant {
    LUMO_SMALL("small"), LUMO_LARGE("large"), LUMO_TERTIARY(
            "tertiary"), LUMO_TERTIARY_INLINE(
                    "tertiary-inline"), LUMO_PRIMARY("primary"), LUMO_CONTRAST(
                            "contrast"), LUMO_ICON("icon"), MATERIAL_CONTAINED(
                                    "contained"), MATERIAL_OUTLINED("outlined");

    private final String variant;

    MenuBarVariant(String variant) {
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
