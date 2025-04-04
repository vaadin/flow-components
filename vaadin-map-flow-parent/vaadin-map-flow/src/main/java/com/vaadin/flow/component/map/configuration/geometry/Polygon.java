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
 * Polygon geometry.
 *
 * @apiNote <a href=
 *          "https://openlayers.org/en/latest/apidoc/module-ol_geom_Polygon-Polygon.html">ol/geom/Polygon~Polygon
 *          </a>
 */
public class Polygon extends SimpleGeometry {

    private Coordinate[][] coordinates;

    /**
     * Constructs a new {@code Polygon} geometry based on the provided list of
     * coordinates. The polygon is defined in the map's user projection, which
     * defaults to {@code EPSG:4326}. The provided coordinate list defines a
     * linear ring, where the first coordinate and the last should be equivalent
     * to ensure a closed ring. This ring specifies the outer boundary or
     * surface of the polygon without any holes.
     *
     * @param coordinates
     *            the coordinates that locate the polygon
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
     * where the first and the last coordinates are identical. Coordinates must
     * be specified in the map's user projection, which defaults to
     * {@code EPSG:4326}.
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
     * Sets the coordinates that define the polygon without a hole in the
     * surface. Coordinates must be specified in the map's user projection,
     * which by default is {@code EPSG:4326}.
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
     * Array of linear rings that define the polygon. The first linear ring of
     * the array defines the outer-boundary or surface of the polygon. Each
     * subsequent linear ring defines a hole in the surface of the polygon. A
     * linear ring is an array of vertices' coordinates where the first
     * coordinate and the last are equivalent.
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
     * The coordinates where the polygon is located
     *
     * @return the current coordinates
     */
    public Coordinate[][] getCoordinates() {
        return coordinates;
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        this.coordinates = Arrays.stream(coordinates).map(
                linearRing -> translateCoordinates(deltaX, deltaY, linearRing))
                .toArray(Coordinate[][]::new);
    }

    private static Coordinate[] translateCoordinates(double deltaX,
            double deltaY, Coordinate[] source) {
        return Arrays.stream(source).map(
                coordinate -> translateCoordinates(deltaX, deltaY, coordinate))
                .toArray(Coordinate[]::new);
    }

    /**
     * Translates a given coordinate by the specified delta values along the
     * x-axis and y-axis.
     *
     * @param deltaX
     *            the amount to move the coordinate along the x-axis
     * @param deltaY
     *            the amount to move the coordinate along the y-axis
     * @param source
     *            the original coordinate to be translated
     * @return a new {@code Coordinate} representing the translated position
     */
    private static Coordinate translateCoordinates(double deltaX, double deltaY,
            Coordinate source) {
        return new Coordinate(source.getX() + deltaX, source.getY() + deltaY);
    }

    /**
     * Validates the structure and content of the provided two-dimensional array
     * of {@code Coordinate} objects. Ensures that the array and its nested
     * arrays are not null or empty. Throws an {@code IllegalArgumentException}
     * if these conditions are not met.
     *
     * @param coordinates
     *            a two-dimensional array of {@code Coordinate} objects, where
     *            each nested array represents a linear ring of a polygon; must
     *            not be null or empty
     * @throws IllegalArgumentException
     *             if the input array or any of its nested arrays are null or
     *             empty
     */
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
