package com.vaadin.flow.component.map.events;

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
@DomEvent("map-feature-drag-drop")
public class MapFeatureDragDropEvent extends ComponentEvent<Map> {

    private final Feature feature;
    private final VectorLayer layer;
    private final VectorSource vectorSource;
    private final Coordinate coordinate;
    private final Coordinate startCoordinate;

    public MapFeatureDragDropEvent(Map source, boolean fromClient,
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
        this.coordinate = new Coordinate(coordinate.get(0).asNumber(),
                coordinate.get(1).asNumber());
        this.startCoordinate = new Coordinate(startCoordinate.get(0).asNumber(),
                startCoordinate.get(1).asNumber());
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

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Coordinate getStartCoordinate() {
        return startCoordinate;
    }
}
