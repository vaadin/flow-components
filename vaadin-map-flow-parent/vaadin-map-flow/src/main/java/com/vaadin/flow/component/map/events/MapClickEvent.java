/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.MapBase;
import com.vaadin.flow.component.map.configuration.Coordinate;

import elemental.json.JsonArray;

/**
 * Representing OpenLayers' @code{click} event
 */
@DomEvent("map-click")
public class MapClickEvent extends ComponentEvent<MapBase> {

    private final Coordinate coordinate;
    private final List<FeatureEventDetails> features;
    private final MouseEventDetails details;

    public MapClickEvent(Map source, boolean fromClient,
            @EventData("event.detail.coordinate") JsonArray coordinate,
            @EventData("event.detail.features.map(feature => feature.feature.id)") JsonArray featureIds,
            @EventData("event.detail.features.map(feature => feature.layer.id)") JsonArray layerIds,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button) {
        super(source, fromClient);

        this.coordinate = MapEventUtil.getCoordinate(coordinate);

        List<FeatureEventDetails> features = new ArrayList<>();
        for (int i = 0; i < featureIds.length(); i++) {
            String featureId = featureIds.getString(i);
            String layerId = layerIds.getString(i);
            FeatureEventDetails featureEventDetails = MapEventUtil
                    .getFeatureEventDetails(source.getRawConfiguration(),
                            layerId, featureId);
            features.add(featureEventDetails);
        }
        this.features = Collections.unmodifiableList(features);

        details = new MouseEventDetails();
        details.setAbsoluteX(pageX);
        details.setAbsoluteY(pageY);
        details.setButton(MouseEventDetails.MouseButton.of(button));
        details.setAltKey(altKey);
        details.setCtrlKey(ctrlKey);
        details.setMetaKey(metaKey);
        details.setShiftKey(shiftKey);
    }

    /**
     * Gets the coordinate of the click on viewport. Coordinates are returned in
     * the map's user projection, which by default is {@code EPSG:4326}, also
     * referred to as GPS coordinates. If the user projection has been changed
     * using {@link Map#setUserProjection(String)}, then coordinates must be
     * specified in that projection instead.
     *
     * @return coordinate of the click
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * List of map features at the clicked position. Can be used to distinguish
     * whether the click was on the background, or on a feature. The features
     * are sorted by their display order, meaning the top-most feature is the
     * first item in the list.
     *
     * @return the list of features at the clicked position, or an empty list
     *         otherwise
     */
    public List<FeatureEventDetails> getFeatures() {
        return features;
    }

    /**
     * Gets the click's mouse event details.
     *
     * @return mouse event details
     */
    public MouseEventDetails getMouseDetails() {
        return details;
    }
}
