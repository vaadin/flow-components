package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
