/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The name of a symbol to use for the border around the {@link Datalabels} or
 * {@link Tooltip}.
 */
public enum Shape implements ChartEnum {

    CALLOUT("callout"), CIRCLE("circle"), DIAMOND("diamond"), SQUARE(
            "square"), TRIANGLE("triangle"), TRIANGLE_DOWN("triangle-down");

    Shape(String type) {
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return type;
    }
}
