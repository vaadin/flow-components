package com.vaadin.flow.component.map.configuration;

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
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
}
