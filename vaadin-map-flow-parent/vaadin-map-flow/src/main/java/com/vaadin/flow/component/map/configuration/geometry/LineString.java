/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;

/**
 * Geometry that represents a line string, which is a series of connected line
 * segments.
 */
public class LineString extends SimpleGeometry {

    private Coordinate[] coordinates;

    /**
     * Constructs a new {@code LineString} geometry based on the provided
     * coordinates. The provided coordinates define the vertices of the line. A
     * line string must have at least two coordinates. Using more than two
     * coordinates creates a multi-segment line, for example to represent a path
     * or route.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the coordinates that define the line string
     * @throws IllegalArgumentException
     *             if the provided coordinate list is null or has fewer than 2
     *             coordinates
     */
    public LineString(List<Coordinate> coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates.toArray(new Coordinate[0]);
    }

    /**
     * Constructs a new {@code LineString} geometry based on the provided
     * coordinates. The provided coordinates define the vertices of the line. A
     * line string must have at least two coordinates. Using more than two
     * coordinates creates a multi-segment line, for example to represent a path
     * or route.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the coordinates that define the line string
     * @throws IllegalArgumentException
     *             if the provided coordinate array is null or has fewer than 2
     *             coordinates
     */
    public LineString(Coordinate... coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates;
    }

    @Override
    public String getType() {
        return Constants.OL_GEOMETRY_LINESTRING;
    }

    /**
     * Sets the coordinates that define the line string. The provided
     * coordinates define the vertices of the line. A line string must have at
     * least two coordinates. Using more than two coordinates creates a
     * multi-segment line, for example to represent a path or route.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the new coordinates
     * @throws IllegalArgumentException
     *             if the provided coordinate list is null or has fewer than 2
     *             coordinates
     */
    public void setCoordinates(List<Coordinate> coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates.toArray(new Coordinate[0]);
        markAsDirty();
    }

    /**
     * Sets the coordinates that define the line string. The provided
     * coordinates define the vertices of the line. A line string must have at
     * least two coordinates. Using more than two coordinates creates a
     * multi-segment line, for example to represent a path or route.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the new coordinates
     * @throws IllegalArgumentException
     *             if the provided coordinate array is null or has fewer than 2
     *             coordinates
     */
    public void setCoordinates(Coordinate... coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates;
        markAsDirty();
    }

    /**
     * The coordinates that define the line string, as an array.
     *
     * @return the current coordinates
     */
    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        Coordinate[] nextCoordinates = Arrays.stream(coordinates)
                .map(coordinate -> new Coordinate(coordinate.getX() + deltaX,
                        coordinate.getY() + deltaY))
                .toArray(Coordinate[]::new);
        setCoordinates(nextCoordinates);
    }

    private static void validateCoordinates(Coordinate[] coordinates) {
        if (coordinates == null || coordinates.length < 2) {
            throw new IllegalArgumentException(
                    "LineString must have at least 2 coordinates");
        }
    }

    private static void validateCoordinates(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException(
                    "LineString must have at least 2 coordinates");
        }
    }
}
