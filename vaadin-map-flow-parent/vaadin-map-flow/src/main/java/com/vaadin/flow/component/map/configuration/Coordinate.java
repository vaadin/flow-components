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

import java.util.Objects;

/**
 * Represents map coordinates in a specific projection. Which projection the
 * coordinates are in is not known by the coordinate itself, and developers must
 * ensure themselves to use the same projection between coordinates, the map
 * viewport, and map sources.
 */
public class Coordinate {
    private final double x;
    private final double y;

    public Coordinate() {
        this(0, 0);
    }

    /**
     * Constructs a new coordinate instance from x and y coordinates. Unless the
     * map's view uses a custom projection, it is assumed that the coordinates
     * are in {@code EPSG:3857} / Web Mercator Sphere projection. To create
     * coordinates from latitude and longitude, see
     * {@link #fromLonLat(double, double)}.
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
     * Creates a coordinate from {@link Projection#EPSG_3857} projection and
     * converts it into longitude and latitude
     *
     * @param x
     *            longitude value in EPSG_3857 format
     * @param y
     *            latitude value EPSG_3857 format
     * @return coordinate in {@link Projection#EPSG_4326} projection
     */
    public static Coordinate toLonLat(double x, double y) {
        return fromLonLat(x, y, Projection.EPSG_4326);
    }

    /**
     * Creates a coordinate from {@link Projection#EPSG_3857} projection and
     * converts it into longitude and latitude
     *
     * @param espg3857
     *            coordinates in EPSG_3857 format
     * @return coordinate in {@link Projection#EPSG_4326} projection
     */
    public static Coordinate toLonLat(Coordinate espg3857) {
        return fromLonLat(espg3857.getX(), espg3857.getY(),
                Projection.EPSG_4326);
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
        case EPSG_4326:
            return Converters.epsg_3857_to_epsg_4326(
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

        // Adapted from `ol/proj/epsg3857.js`
        private static Coordinate epsg_3857_to_epsg_4326(
                Coordinate coordinate) {
            double x = 180 * coordinate.x / HALF_SIZE;
            double y = (360 * Math.atan(Math.exp(coordinate.y / RADIUS)))
                    / Math.PI - 90;

            return new Coordinate(x, y);
        }

    }
}
