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
import java.util.Objects;

/**
 * Represents map coordinates in a specific projection. Coordinates must be
 * specified in the map's user projection, which by default is
 * {@code EPSG:4326}, also referred to as GPS coordinates. If the user
 * projection has been changed using {@link Map#setUserProjection(String)}, then
 * coordinates must be specified in that projection instead.
 */
public class Coordinate implements Serializable {
    private final double x;
    private final double y;

    public Coordinate() {
        this(0, 0);
    }

    /**
     * Constructs a new coordinate instance from x and y coordinates.
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates. If the
     * user projection has been changed using
     * {@link Map#setUserProjection(String)}, then coordinates must be specified
     * in that projection instead.
     *
     * @param x
     * @param y
     */
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinate{" + "x=" + x + ", y=" + y + '}';
    }
}
