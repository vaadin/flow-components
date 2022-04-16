package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
