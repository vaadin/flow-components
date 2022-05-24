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
