/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.feature;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;

import java.util.Objects;

/**
 * Abstract base class for features that are represented by a single point and a
 * single coordinate.
 * <p>
 * Technically this is a {@link Feature} that uses a {@link Point} geometry for
 * representation.
 */
public abstract class PointBasedFeature extends Feature {
    protected PointBasedFeature() {
        this(new Coordinate(0, 0));
    }

    protected PointBasedFeature(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        setGeometry(new Point(coordinates));
    }

    /**
     * The coordinates that define where the feature is located on the map.
     * Coordinates are returned in the map's user projection, which by default
     * is {@code EPSG:4326}, also referred to as GPS coordinates. If the user
     * projection has been changed using {@link Map#setUserProjection(String)},
     * then coordinates must be specified in that projection instead.
     *
     * @return the current coordinates
     */
    @JsonIgnore
    public Coordinate getCoordinates() {
        return getGeometry().getCoordinates();
    }

    /**
     * Sets the coordinates that define where the feature is located on the map.
     * Coordinates must be specified in the map's user projection, which by
     * default is {@code EPSG:4326}, also referred to as GPS coordinates. If the
     * user projection has been changed using
     * {@link Map#setUserProjection(String)}, then coordinates must be specified
     * in that projection instead.
     *
     * @param coordinates
     *            the new coordinates
     */
    public void setCoordinates(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        getGeometry().setCoordinates(coordinates);
    }

    /**
     * The {@link Point} geometry representing this feature.
     *
     * @return the current point geometry
     */
    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Point getGeometry() {
        return (Point) super.getGeometry();
    }

    /**
     * Sets the geometry representing this feature. This must be a {@link Point}
     * geometry.
     *
     * @param geometry
     *            the new geometry, not null
     * @throws IllegalArgumentException
     *             if the geometry is not an instance of {@link Point}
     */
    @Override
    public void setGeometry(SimpleGeometry geometry) {
        Objects.requireNonNull(geometry);
        if (!(geometry instanceof Point)) {
            throw new IllegalArgumentException("Geometry must be a point");
        }
        super.setGeometry(geometry);
    }
}
