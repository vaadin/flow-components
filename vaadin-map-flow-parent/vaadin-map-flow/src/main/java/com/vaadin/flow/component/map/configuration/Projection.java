/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

/**
 * Defines constants for map coordinate projections. This is not an exhaustive
 * list of projections that can be used with the map component, it is possible
 * to use other projections as well.
 */
public enum Projection {
    EPSG_3857("EPSG:3857");

    private final String value;

    public String stringValue() {
        return value;
    }

    Projection(String value) {
        this.value = value;
    }
}
