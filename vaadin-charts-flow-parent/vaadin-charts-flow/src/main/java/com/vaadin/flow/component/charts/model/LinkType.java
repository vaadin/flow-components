/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The type of the link shape.
 */
public enum LinkType implements ChartEnum {

    DEFAULT("default"), CURVED("curved"), STRAIGHT("straight");

    private final String type;

    LinkType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
