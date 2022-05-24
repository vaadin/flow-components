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
