/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PointBasedFeature;
import com.vaadin.flow.component.map.configuration.source.ClusterSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.component.map.configuration.style.Style;

/**
 * Layer for rendering clusters of features.
 * <p>
 * Currently only point-based features such as {@link MarkerFeature} are
 * supported.
 */
public class ClusterLayer extends FeatureLayer {
    private Style style;

    @Override
    protected VectorSource createDefaultSource() {
        return new ClusterSource();
    }

    @Override
    public String getType() {
        return Constants.OL_LAYER_CLUSTER;
    }

    /**
     * The cluster source for this layer.
     *
     * @return cluster source for this layer, null by default
     */
    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public ClusterSource getSource() {
        return (ClusterSource) super.getSource();
    }

    /**
     * Sets the cluster source for this layer. Must be a {@link ClusterSource}.
     * Throws an exception if the source is of a different type.
     *
     * @param source
     *            the new cluster source for the layer, not null
     */
    @Override
    public void setSource(VectorSource source) {
        if (!(source instanceof ClusterSource)) {
            throw new IllegalArgumentException(
                    "Source must be a ClusterSource");
        }

        super.setSource(source);
    }

    /**
     * Adds a feature to the layer's source. Must be a point-based feature such
     * as {@link MarkerFeature}. Throws an exception if the feature is of a
     * different type.
     *
     * @param feature
     *            the feature to be added
     */
    @Override
    public void addFeature(Feature feature) {
        if (!(feature instanceof PointBasedFeature)) {
            throw new IllegalArgumentException(
                    "Only point-based features such as MarkerFeature are supported");
        }
        super.addFeature(feature);
    }

    /**
     * The {@link Style} defines how individual clusters should be visually
     * displayed. {code null} by default, which means that clusters are rendered
     * using a default style.
     *
     * @return the current style
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the style for individual clusters.
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
