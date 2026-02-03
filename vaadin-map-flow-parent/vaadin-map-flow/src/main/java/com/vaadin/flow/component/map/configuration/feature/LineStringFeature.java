/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.feature;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.geometry.LineString;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;

/**
 * A convenience class for displaying a line string with one or more segments on
 * the map.
 * <p>
 * Technically this is a {@link Feature} that uses a {@link LineString} geometry
 * for representation.
 */
public class LineStringFeature extends Feature {
    /**
     * Creates a new line string feature with the default style using the
     * provided coordinates. The provided coordinates define the vertices of the
     * line string. A line string must have at least two coordinates. Using more
     * than two coordinates creates a multi-segment line string, for example to
     * represent a path or route.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the list of coordinates that define the vertices of the line
     *            string
     * @throws IllegalArgumentException
     *             if the provided coordinate list is null or has fewer than 2
     *             coordinates
     */
    public LineStringFeature(List<Coordinate> coordinates) {
        setGeometry(new LineString(coordinates));
        setStyle(createDefaultStyle());
    }

    /**
     * Creates a new line string feature with the default style using the
     * provided coordinates. The provided coordinates define the vertices of the
     * line string. A line string must have at least two coordinates. Using more
     * than two coordinates creates a multi-segment line string, for example to
     * represent a path or route.
     * <p>
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates.
     *
     * @param coordinates
     *            the coordinates that define the vertices of the line string
     * @throws IllegalArgumentException
     *             if the provided coordinate array is null or has fewer than 2
     *             coordinates
     */
    public LineStringFeature(Coordinate... coordinates) {
        setGeometry(new LineString(coordinates));
        setStyle(createDefaultStyle());
    }

    /**
     * The coordinates that define the line string, as an array. Each coordinate
     * represents a vertex in the line string.
     *
     * @return the current coordinates
     */
    @JsonIgnore
    public Coordinate[] getCoordinates() {
        return getGeometry().getCoordinates();
    }

    /**
     * Sets the coordinates that define the line string. The provided
     * coordinates define the vertices of the line string. A line string must
     * have at least two coordinates. Using more than two coordinates creates a
     * multi-segment line string, for example to represent a path or route.
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
        Objects.requireNonNull(coordinates);
        getGeometry().setCoordinates(coordinates);
    }

    /**
     * Sets the coordinates that define the line string. The provided
     * coordinates define the vertices of the line string. A line string must
     * have at least two coordinates. Using more than two coordinates creates a
     * multi-segment line string, for example to represent a path or route.
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
        Objects.requireNonNull(coordinates);
        getGeometry().setCoordinates(coordinates);
    }

    /**
     * The {@link LineString} geometry representing this feature.
     *
     * @return the current line string geometry
     */
    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public LineString getGeometry() {
        return (LineString) super.getGeometry();
    }

    /**
     * Sets the geometry representing this feature. This must be a
     * {@link LineString} geometry.
     *
     * @param geometry
     *            the new geometry, not null
     * @throws IllegalArgumentException
     *             if the geometry is not an instance of {@link LineString}
     */
    @Override
    public void setGeometry(SimpleGeometry geometry) {
        Objects.requireNonNull(geometry);
        if (!(geometry instanceof LineString)) {
            throw new IllegalArgumentException(
                    "Geometry must be a line string");
        }
        super.setGeometry(geometry);
    }

    private static Style createDefaultStyle() {
        Style style = new Style();
        style.setStroke(new Stroke("hsl(214, 100%, 48%)", 2));
        return style;
    }
}
