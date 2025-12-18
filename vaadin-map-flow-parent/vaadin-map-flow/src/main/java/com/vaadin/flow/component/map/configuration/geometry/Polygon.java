/**
 * Copyright 2000-2025 Vaadin Ltd.
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
 * Geometry that represents a polygon.
 *
 * @apiNote <a href=
 *          "https://openlayers.org/en/latest/apidoc/module-ol_geom_Polygon-Polygon.html">ol/geom/Polygon~Polygon
 *          </a>
 */
public class Polygon extends SimpleGeometry {

    private Coordinate[][] coordinates;

    /**
     * Constructs a new {@code Polygon} geometry based on the provided list of
     * coordinates. The provided coordinate list defines a linear ring, where
     * the first coordinate and the last should be equivalent to ensure a closed
     * ring. This ring specifies the outer boundary or surface of the polygon
     * without any holes.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the coordinates that define the polygon
     * @throws IllegalArgumentException
     *             if the provided coordinate list is null or empty
     */
    public Polygon(List<Coordinate> coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = new Coordinate[][] {
                coordinates.toArray(new Coordinate[0]) };
    }

    /**
     * Constructs a new {@code Polygon} geometry based on the provided
     * two-dimensional array of coordinates. The first array represents the
     * outer boundary of the polygon as a linear ring of coordinates. Each
     * subsequent array represents a linear ring defining a hole in the
     * polygon's surface. A linear ring is defined as an array of coordinates
     * where the first and the last coordinates are identical.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the coordinates that locate the polygon
     * @throws IllegalArgumentException
     *             if any of the arrays in the provided coordinates array are
     *             null or empty
     */
    public Polygon(Coordinate[][] coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates;
    }

    @Override
    public String getType() {
        return Constants.OL_GEOMETRY_POLYGON;
    }

    /**
     * Sets the coordinates that define the polygon. The provided coordinate
     * list defines a linear ring, where the first coordinate and the last
     * should be equivalent to ensure a closed ring. This ring specifies the
     * outer boundary or surface of the polygon without any holes.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the new coordinates
     */
    public void setCoordinates(List<Coordinate> coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = new Coordinate[][] {
                coordinates.toArray(new Coordinate[0]) };
        markAsDirty();
    }

    /**
     * Sets the coordinates that define the polygon as a two-dimensional array.
     * The first array represents the outer boundary of the polygon as a linear
     * ring of coordinates. Each subsequent array represents a linear ring
     * defining a hole in the polygon's surface. A linear ring is defined as an
     * array of coordinates where the first and the last coordinates are
     * identical.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the new coordinates
     */
    public void setCoordinates(Coordinate[][] coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates;
        markAsDirty();
    }

    /**
     * The coordinates where the polygon is located, as a two-dimensional array.
     * The first array represents the outer boundary of the polygon as a linear
     * ring of coordinates. Each subsequent array represents a linear ring
     * defining a hole in the polygon's surface. A linear ring is defined as an
     * array of coordinates where the first and the last coordinates are
     * identical.
     *
     * @return the current coordinates
     */
    public Coordinate[][] getCoordinates() {
        return coordinates;
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        Coordinate[][] nextCoordinates = Arrays.stream(coordinates).map(
                linearRing -> translateLinearRing(deltaX, deltaY, linearRing))
                .toArray(Coordinate[][]::new);
        setCoordinates(nextCoordinates);
    }

    private static Coordinate[] translateLinearRing(double deltaX,
            double deltaY, Coordinate[] source) {
        return Arrays.stream(source).map(
                coordinate -> translateCoordinates(deltaX, deltaY, coordinate))
                .toArray(Coordinate[]::new);
    }

    private static Coordinate translateCoordinates(double deltaX, double deltaY,
            Coordinate source) {
        return new Coordinate(source.getX() + deltaX, source.getY() + deltaY);
    }

    private static void validateCoordinates(Coordinate[][] coordinates) {
        if (coordinates == null || coordinates.length == 0) {
            throw new IllegalArgumentException(
                    "Coordinates must not be null or empty");
        }

        for (Coordinate[] linearRing : coordinates) {
            if (linearRing == null || linearRing.length == 0) {
                throw new IllegalArgumentException(
                        "Coordinates of the linearRing must not be null or empty");
            }
        }
    }

    private static void validateCoordinates(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Coordinates must not be null or empty");
        }
    }
}
