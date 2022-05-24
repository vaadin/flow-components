package com.vaadin.flow.component.map.configuration.feature;

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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;
import com.vaadin.flow.component.map.configuration.source.Source;

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
     * The coordinates that define where the feature is located on the map. The
     * coordinates must be in the same projection as the
     * {@link View#getProjection()} and {@link Source#getProjection()}.
     *
     * @return the current coordinates
     */
    @JsonIgnore
    public Coordinate getCoordinates() {
        return getGeometry().getCoordinates();
    }

    /**
     * Sets the coordinates that define where the feature is located on the map.
     * The coordinates must be in the same projection as the
     * {@link View#getProjection()} and {@link Source#getProjection()}.
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
