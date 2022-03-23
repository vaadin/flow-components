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
 * Possible values: null, ON, BETWEEN.
 *
 * In a column chart, when pointPlacement is ON, the point will not create any
 * padding of the X-axis. In a polar column chart this means that the first
 * column points directly north. If the pointPlacement is BETWEEN, the columns
 * will be laid out between ticks. This is useful for example for visualizing an
 * amount between two points in time or in a certain sector of a polar chart.
 *
 * Defaults to null in Cartesian charts, BETWEEN in polar charts.
 */
public enum PointPlacement implements ChartEnum {
    ON("on"), BETWEEN("between");

    private final String pointPlacement;

    private PointPlacement(String pointPlacement) {
        this.pointPlacement = pointPlacement;
    }

    @Override
    public String toString() {
        return pointPlacement;
    }
}
