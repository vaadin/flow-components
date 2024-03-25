/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The shape of the pane background. When solid, the background is circular.
 * When arc, the background extends only from the min to the max of the value
 * axis.
 *
 * Defaults to solid.
 */
public enum BackgroundShape implements ChartEnum {

    SOLID("solid"), ARC("arc");

    private final String shape;

    private BackgroundShape(String shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return shape;
    }
}
