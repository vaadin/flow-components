/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

/**
 * Layer that allows to conveniently display a number of geographic features. A
 * {@link Feature} can be anything that should be displayed on top of a map,
 * such as points of interest, vehicles or people.
 * <p>
 * The layer is a high-level abstraction built on top of {@link VectorLayer},
 * and uses a {@link VectorSource} by default.
 */
public class FeatureLayer extends VectorLayer {

    public FeatureLayer() {
        this.setSource(new VectorSource());
    }

    /**
     * The source for this layer. For the feature layer this must always be a
     * {@link VectorSource}
     *
     * @return the source of the layer
     */
    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public VectorSource getSource() {
        return (VectorSource) super.getSource();
    }

    /**
     * The features managed by this layer. This returns an immutable collection,
     * which means it can not be modified. Use {@link #addFeature(Feature)} and
     * {@link #removeFeature(Feature)} instead.
     *
     * @return the features managed by the layer, immutable
     */
    @JsonIgnore
    public List<Feature> getFeatures() {
        return getSource().getFeatures();
    }

    /**
     * Adds a feature to the layer
     *
     * @param feature
     *            the feature to be added
     */
    public void addFeature(Feature feature) {
        this.getSource().addFeature(feature);
    }

    /**
     * Removes a feature from the layer
     *
     * @param feature
     *            the feature to be removed
     */
    public void removeFeature(Feature feature) {
        this.getSource().removeFeature(feature);
    }

    /**
     * Removes all features from the layer
     */
    public void removeAllFeatures() {
        this.getSource().removeAllFeatures();
    }
}
