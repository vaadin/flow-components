/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import java.util.List;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.MapBase;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Provides data for click events on a cluster of features
 */
@DomEvent("map-cluster-click")
public class MapClusterClickEvent extends ComponentEvent<MapBase> {

    private final List<Feature> features;
    private final VectorLayer layer;
    private final VectorSource vectorSource;
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

        layer = source.getRawConfiguration().getLayers().stream()
                .filter(l -> l instanceof VectorLayer && l.getId() != null
                        && l.getId().equals(layerId))
                .map(l -> (VectorLayer) l).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No vector layer with id " + layerId));
        vectorSource = (VectorSource) layer.getSource();

        List<String> featureIdList = JacksonUtils.stream(featureIds)
                .map(JsonNode::asString).toList();
        features = vectorSource.getFeatures().stream()
                .filter(feature -> featureIdList.contains(feature.getId()))
                .toList();

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
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * Gets the layer that contains the cluster's features.
     *
     * @return the layer
     */
    public VectorLayer getLayer() {
        return layer;
    }

    /**
     * Gets the source that contains the cluster's features.
     *
     * @return the source
     */
    public VectorSource getVectorSource() {
        return vectorSource;
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
