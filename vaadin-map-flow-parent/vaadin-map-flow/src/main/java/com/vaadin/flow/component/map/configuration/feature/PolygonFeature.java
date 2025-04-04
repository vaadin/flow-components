/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.map.configuration.feature;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.geometry.Polygon;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import java.util.List;
import java.util.Objects;

/**
 * Class for features that are represented by a single polygon.
 * <p>
 * Technically this is a {@link Feature} that uses a {@link Polygon} geometry
 * for representation.
 */
public class PolygonFeature extends Feature {

    private static final Style DEFAULT_STYLE;
    static {
        final Style style = new Style();
        style.setStroke(new Stroke("hsl(214, 100%, 48%)", 2));
        style.setFill(new Fill("hsla(214, 100%, 60%, 0.13)"));
        DEFAULT_STYLE = style;
    }

    /**
     * Creates a new polygon feature with the default style.
     */
    public PolygonFeature() {
        this(List.of(new Coordinate(0, 0)));
    }

    /**
     * Creates a new polygon-based feature with the default style using the
     * provided coordinates. The polygon is defined in the map's user
     * projection, which defaults to {@code EPSG:4326}. The provided coordinate
     * list defines a linear ring, where the first coordinate and the last
     * should be equivalent to ensure a closed ring. This ring specifies the
     * outer boundary or surface of the polygon without any holes.
     *
     * @param coordinates
     *            the list of coordinates that define the vertices of the
     *            polygon
     */
    public PolygonFeature(List<Coordinate> coordinates) {
        setGeometry(new Polygon(coordinates));
        // Let the polygon be visible on the map
        setStyle(DEFAULT_STYLE);
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
    public Coordinate[][] getCoordinates() {
        return getGeometry().getCoordinates();
    }

    /**
     * @see #setCoordinates(Coordinate[][])
     */
    public void setCoordinates(List<Coordinate> coordinates) {
        Objects.requireNonNull(coordinates);
        getGeometry().setCoordinates(coordinates);
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
    public void setCoordinates(Coordinate[][] coordinates) {
        Objects.requireNonNull(coordinates);
        getGeometry().setCoordinates(coordinates);
    }

    /**
     * The {@link Polygon} geometry representing this feature.
     *
     * @return the current polygon geometry
     */
    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Polygon getGeometry() {
        return (Polygon) super.getGeometry();
    }

    /**
     * Sets the geometry representing this feature. This must be a
     * {@link Polygon} geometry.
     *
     * @param geometry
     *            the new geometry, not null
     * @throws IllegalArgumentException
     *             if the geometry is not an instance of {@link Polygon}
     */
    @Override
    public void setGeometry(SimpleGeometry geometry) {
        Objects.requireNonNull(geometry);
        if (!(geometry instanceof Polygon)) {
            throw new IllegalArgumentException("Geometry must be a polygon");
        }
        super.setGeometry(geometry);
    }
}
