/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;
import com.vaadin.flow.component.map.configuration.style.Style;

import java.util.Objects;

/**
 * A geographic feature to be displayed on a map. A feature represents a point
 * of interest, such as an address, a building, a vehicle, or any other entity.
 */
public abstract class Feature extends AbstractConfigurationObject {

    private SimpleGeometry geometry;
    private Style style;
    private String label;
    private boolean draggable;

    @Override
    public String getType() {
        return Constants.OL_FEATURE;
    }

    /**
     * The geometry representing the feature, for example a {@link Point} or a
     * polygon.
     *
     * @return the current geometry
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public SimpleGeometry getGeometry() {
        return geometry;
    }

    /**
     * Sets the geometry representing the feature.
     *
     * @param geometry
     *            the new geometry, not null
     */
    public void setGeometry(SimpleGeometry geometry) {
        Objects.requireNonNull(geometry);
        removeChild(this.geometry);
        this.geometry = geometry;
        addChild(geometry);
    }

    /**
     * The {@link Style} defines how the feature should be visually displayed.
     *
     * @return the current style
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the style that defines how the feature should be visually displayed.
     *
     * @param style
     *            the new style, not null
     */
    public void setStyle(Style style) {
        Objects.requireNonNull(style);
        removeChild(this.style);
        this.style = style;
        addChild(style);
    }

    /**
     * The label that should be displayed next to the feature.
     *
     * @return the label string
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label that should be displayed next to the feature. Set to
     * {@code null} to remove the label.
     *
     * @param label
     *            the new label string, or {@code null} to remove the label
     */
    public void setLabel(String label) {
        this.label = label;
        markAsDirty();
    }

    /**
     * Whether the feature can be dragged on the map using pointing devices or
     * not
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Sets whether the feature can be dragged on the map using pointing devices
     * or not. Enabling this will make the feature draggable on the map,
     * indicated by a pointer cursor when hovering over the feature. The
     * feature's position / geometry is automatically updated after dropping the
     * feature. Use {@link Map#addFeatureDropListener(ComponentEventListener)}
     * to get notified when a feature has been moved.
     *
     * @param draggable
     *            whether the feature can be dragged or not
     */
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        markAsDirty();
    }
}
