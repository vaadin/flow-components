/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Axial dimension.
 */
public enum Dimension implements ChartEnum {

    X("x"), Y("y"), XY("xy");

    private final String dimension;

    private Dimension(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return dimension;
    }
}
