/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

import java.io.Serializable;

/**
 * Stores information about a {@link Feature} that is part of an event
 */
public class FeatureEventDetails implements Serializable {
    private final Feature feature;
    private final VectorSource source;
    private final VectorLayer layer;

    public FeatureEventDetails(Feature feature, VectorSource source,
            VectorLayer layer) {
        this.feature = feature;
        this.source = source;
        this.layer = layer;
    }

    /**
     * The feature of the event
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * The {@link VectorSource} that contains the feature
     */
    public VectorSource getSource() {
        return source;
    }

    /**
     * The {@link VectorLayer} that contains the feature
     */
    public VectorLayer getLayer() {
        return layer;
    }
}
