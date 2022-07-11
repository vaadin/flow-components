package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;

import elemental.json.JsonArray;

@DomEvent("map-marker-drop")
public class MapFeatureDropEvent extends ComponentEvent<Map> {

    private final Feature feature;

    private final Coordinate coordinate;

    public MapFeatureDropEvent(Map source, boolean fromClient,
            @EventData("event.detail.featureId") String featureId,
            @EventData("event.detail.coordinate") JsonArray coordinate) {
        super(source, fromClient);

        FeatureEventDetails featureEventDetails = MapEventUtil
                .getFeatureEventDetails(source.getRawConfiguration(),
                        featureId);

        this.feature = featureEventDetails.getFeature();
        this.coordinate = new Coordinate(coordinate.getNumber(0),
                coordinate.getNumber(1));
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Feature getFeature() {
        return feature;
    }

}
