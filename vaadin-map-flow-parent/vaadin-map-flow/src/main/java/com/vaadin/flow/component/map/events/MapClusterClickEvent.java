/**
 * Copyright 2000-2025 Vaadin Ltd.
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

import tools.jackson.databind.node.ArrayNode;

/**
 * Provides data for click events on a cluster of features
 */
@DomEvent("map-cluster-click")
public class MapClusterClickEvent extends ComponentEvent<MapBase> {

    private final List<FeatureEventDetails> features;
    private final MouseEventDetails details;

    public MapClusterClickEvent(Map source, boolean fromClient,
            @EventData("event.detail.features.map(feature => feature.id)") ArrayNode featureIds,
            @EventData("event.detail.layer.id") String layerId,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button) {
        super(source, fromClient);

        List<FeatureEventDetails> features = new ArrayList<>();
        for (int i = 0; i < featureIds.size(); i++) {
            String featureId = featureIds.get(i).asString();
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
     * List of map features in the cluster.
     *
     * @return the list of features in the cluster
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
