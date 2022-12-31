/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import com.vaadin.flow.component.map.Map;

import java.io.Serializable;

/**
 * Defines an area within a map using min/max coordinates. Coordinates must be
 * specified in the map's user projection, which by default is
 * {@code EPSG:4326}, also referred to as GPS coordinates. If the user
 * projection has been changed using {@link Map#setUserProjection(String)}, then
 * coordinates must be specified in that projection instead.
 */
public class Extent implements Serializable {
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    public Extent(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Extent() {
        this(0, 0, 0, 0);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

}
