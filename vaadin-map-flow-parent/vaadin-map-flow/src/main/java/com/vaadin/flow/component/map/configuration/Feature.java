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
import com.vaadin.flow.component.map.configuration.style.TextStyle;

import java.util.Objects;

/**
 * A geographic feature to be displayed on a map. A feature represents a point
 * of interest, such as an address, a building, a vehicle, or any other entity.
 */
public abstract class Feature extends AbstractConfigurationObject {

    private SimpleGeometry geometry;
    private Style style;
    private String text;
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
     * The text that should be displayed next to the feature.
     *
     * @return the text string
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text that should be displayed next to the feature. Set to
     * {@code null} to remove the text.
     *
     * @param text
     *            the new text string, or {@code null} to remove the text
     */
    public void setText(String text) {
        this.text = text;
        markAsDirty();
    }

    /**
     * Returns the custom text style for rendering this feature's
     * {@link #getText()}. Returns {@code null} by default, which means the text
     * is rendered with a default text style.
     *
     * @return the custom text style, or {@code null} if no custom text style
     *         has been set
     */
    public TextStyle getTextStyle() {
        return style != null ? style.getTextStyle() : null;
    }

    /**
     * Sets a custom text style for rendering this feature's {@link #getText()}.
     * By default, a feature has no custom text style, which means the text is
     * rendered with a default text style. Can be set to {@code null} to remove
     * the custom text style.
     * <p>
     * This is a convenience method for {@link Style#setTextStyle(TextStyle)}.
     * If this feature does not have a style instance yet, an empty one is
     * created.
     *
     * @param textStyle
     *            the new custom text style, or {@code null} to remove the
     *            custom text style
     */
    public void setTextStyle(TextStyle textStyle) {
        if (style == null && textStyle == null) {
            return;
        }

        if (style == null) {
            setStyle(new Style());
        }

        style.setTextStyle(textStyle);
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
