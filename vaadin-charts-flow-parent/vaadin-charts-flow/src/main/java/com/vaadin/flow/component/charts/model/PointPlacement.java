package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
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
