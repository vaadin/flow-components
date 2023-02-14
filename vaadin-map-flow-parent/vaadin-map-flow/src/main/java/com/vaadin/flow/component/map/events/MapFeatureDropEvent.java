/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import elemental.json.JsonArray;

/**
 * Provides data for when a feature is dropped after a drag operation
 */
@DomEvent("map-feature-drop")
public class MapFeatureDropEvent extends ComponentEvent<Map> {

    private final Feature feature;
    private final VectorLayer layer;
    private final VectorSource vectorSource;
    private final Coordinate coordinate;
    private final Coordinate startCoordinate;

    public MapFeatureDropEvent(Map source, boolean fromClient,
            @EventData("event.detail.feature.id") String featureId,
            @EventData("event.detail.layer.id") String layerId,
            @EventData("event.detail.coordinate") JsonArray coordinate,
            @EventData("event.detail.startCoordinate") JsonArray startCoordinate) {
        super(source, fromClient);

        FeatureEventDetails featureEventDetails = MapEventUtil
                .getFeatureEventDetails(source.getRawConfiguration(), layerId,
                        featureId);
        this.layer = featureEventDetails.getLayer();
        this.vectorSource = featureEventDetails.getSource();
        this.feature = featureEventDetails.getFeature();
        this.coordinate = MapEventUtil.getCoordinate(coordinate);
        this.startCoordinate = MapEventUtil.getCoordinate(startCoordinate);
    }

    /**
     * The feature that was moved using drag and drop
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * The layer that contains the feature
     */
    public VectorLayer getLayer() {
        return layer;
    }

    /**
     * The source that contains the feature
     */
    public VectorSource getVectorSource() {
        return vectorSource;
    }

    /**
     * The coordinates that the feature has been dragged to
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * The coordinates that the feature has been dragged from
     */
    public Coordinate getStartCoordinate() {
        return startCoordinate;
    }
}
