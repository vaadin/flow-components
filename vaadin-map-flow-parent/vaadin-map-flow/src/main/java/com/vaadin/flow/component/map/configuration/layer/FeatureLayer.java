package com.vaadin.flow.component.map.configuration.layer;

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
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

import java.util.List;

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
}
