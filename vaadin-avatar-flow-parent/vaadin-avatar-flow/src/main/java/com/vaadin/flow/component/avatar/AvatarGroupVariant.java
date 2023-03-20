/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.avatar;

/**
 * Set of theme variants applicable for {@code vaadin-avatar-group} component.
 */
public enum AvatarGroupVariant {
    LUMO_XLARGE("xlarge"), LUMO_LARGE("large"), LUMO_SMALL(
            "small"), LUMO_XSMALL("xsmall");

    private final String variant;

    AvatarGroupVariant(String variant) {
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
