package com.vaadin.flow.component.map.configuration.geometry;

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

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.source.Source;

import java.util.Objects;

/**
 * Geometry that is represented by a single point and coordinate.
 */
public class Point extends SimpleGeometry {

    private Coordinate coordinates;

    @Override
    public String getType() {
        return Constants.OL_GEOMETRY_POINT;
    }

    /**
     * Creates a new point geometry located at the specified coordinates.The
     * coordinates must be in the same projection as the
     * {@link View#getProjection()} and {@link Source#getProjection()}.
     *
     * @param coordinates
     *            the coordinates that locate the point
     */
    public Point(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        this.coordinates = coordinates;
    }

    /**
     * The coordinates where the point is located
     *
     * @return the current coordinates
     */
    public Coordinate getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the coordinates that locate the point. The coordinates must be in
     * the same projection as the {@link View#getProjection()} and
     * {@link Source#getProjection()}.
     *
     * @param coordinates
     *            the new coordinates, not null
     */
    public void setCoordinates(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        this.coordinates = coordinates;
        markAsDirty();
    }
}
