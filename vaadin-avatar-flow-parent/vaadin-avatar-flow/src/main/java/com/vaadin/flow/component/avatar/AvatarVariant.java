/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.avatar;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable for {@code vaadin-avatar} component.
 */
public enum AvatarVariant implements ThemeVariant {
    //@formatter:off
    LUMO_XLARGE("xlarge"),
    LUMO_LARGE("large"),
    LUMO_SMALL("small"),
    LUMO_XSMALL("xsmall");
    //@formatter:on

    private final String variant;

    AvatarVariant(String variant) {
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
