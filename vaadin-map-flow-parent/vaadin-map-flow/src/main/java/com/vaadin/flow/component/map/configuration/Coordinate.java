package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.map.Map;

import java.util.Objects;

/**
 * Represents map coordinates in a specific projection. Coordinates must be
 * specified in the map's user projection, which by default is
 * {@code EPSG:4326}, also referred to as GPS coordinates. If the user
 * projection has been changed using {@link Map#setUserProjection(String)}, then
 * coordinates must be specified in that projection instead.
 */
public class Coordinate {
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

    /**
     * Creates a coordinate from a longitude and latitude, and converts it into
     * {@link Projection#EPSG_3857} projection.
     *
     * @param longitude
     *            longitude value
     * @param latitude
     *            latitude value
     * @return coordinate in {@link Projection#EPSG_3857} projection
     */
    public static Coordinate fromLonLat(double longitude, double latitude) {
        return fromLonLat(longitude, latitude, Projection.EPSG_3857);
    }

    /**
     * Creates a coordinate from a longitude and latitude, and converts it into
     * the specified projection.
     * <p>
     * Currently, only converting into {@link Projection#EPSG_3857} projection
     * is supported.
     *
     * @param longitude
     *            longitude value
     * @param latitude
     *            latitude value
     * @param targetProjection
     *            the projection of the resulting coordinate
     * @return coordinate in the specified projection
     */
    public static Coordinate fromLonLat(double longitude, double latitude,
            Projection targetProjection) {
        Objects.requireNonNull(targetProjection);
        switch (targetProjection) {
        case EPSG_3857:
            return Converters.epsg_4326_to_epsg_3857(
                    new Coordinate(longitude, latitude));
        default:
            throw new IllegalArgumentException("Unsupported projection: "
                    + targetProjection.stringValue());
        }
    }

    private static class Converters {
        // Radius of WGS84 sphere
        private static final double RADIUS = 6378137;
        private static final double HALF_SIZE = Math.PI * RADIUS;
        // Maximum safe value in y direction
        private static final double MAX_SAFE_Y = RADIUS
                * Math.log(Math.tan(Math.PI / 2));

        // Adapted from `ol/proj/epsg3857.js`
        private static Coordinate epsg_4326_to_epsg_3857(
                Coordinate coordinate) {
            double x = (HALF_SIZE * coordinate.x) / 180;
            double y = RADIUS
                    * Math.log(Math.tan((Math.PI * (coordinate.y + 90)) / 360));
            if (y > MAX_SAFE_Y) {
                y = MAX_SAFE_Y;
            } else if (y < -MAX_SAFE_Y) {
                y = -MAX_SAFE_Y;
            }
            return new Coordinate(x, y);
        }
    }
}
