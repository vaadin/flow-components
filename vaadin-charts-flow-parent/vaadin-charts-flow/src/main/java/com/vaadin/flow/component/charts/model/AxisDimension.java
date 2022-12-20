/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Possible axis dimensions with their indexes in client-side
 */
public enum AxisDimension {
    X_AXIS(0), Y_AXIS(1), Z_AXIS(2), COLOR_AXIS(3);

    private final int index;

    private AxisDimension(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
