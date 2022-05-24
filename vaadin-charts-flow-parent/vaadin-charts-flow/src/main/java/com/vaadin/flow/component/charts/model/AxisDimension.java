package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
