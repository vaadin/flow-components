/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification;

/**
 * Set of theme variants applicable for {@code vaadin-notification} component.
 */
public enum NotificationVariant {
    LUMO_PRIMARY("primary"), LUMO_CONTRAST("contrast"), LUMO_SUCCESS(
            "success"), LUMO_ERROR("error");

    private final String variant;

    NotificationVariant(String variant) {
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
