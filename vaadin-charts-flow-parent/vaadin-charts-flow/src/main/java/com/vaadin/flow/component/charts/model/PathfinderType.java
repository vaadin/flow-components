/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Pathfinder algorithm to use for chart that can connect two points (e.g.
 * series items_.
 */
public enum PathfinderType implements ChartEnum {

    /**
     * Draws a straight line between the connecting points. Does not avoid other
     * points when drawing.
     */
    STRAIGHT("straight"),

    /**
     * Finds a path between the points using right angles only. Takes only
     * starting/ending points into account, and will not avoid other points.
     */
    SIMPLE_CONNECT("simpleConnect"),

    /**
     * Finds a path between the points using right angles only. Will attempt to
     * avoid other points, but its focus is performance over accuracy. Works
     * well with less dense datasets.
     */
    FAST_AVOID("fastAvoid");

    PathfinderType(String type) {
        this.type = type;
    }

    private final String type;

    @Override
    public String toString() {
        return type;
    }
}
