package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

/**
 * Stores information about a {@link Feature} that is part of an event
 */
public class FeatureEventDetails {
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
