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
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

/**
 * Provides data for click events on geographic features
 */
@DomEvent("map-feature-click")
public class MapFeatureClickEvent extends ComponentEvent<Map> {

    private final Feature feature;
    private final VectorLayer layer;
    private final VectorSource vectorSource;
    private final MouseEventDetails details;

    public MapFeatureClickEvent(Map source, boolean fromClient,
            @EventData("event.detail.feature.id") String featureId,
            @EventData("event.detail.layer.id") String layerId,
            @EventData("event.detail.originalEvent.pageX") int pageX,
            @EventData("event.detail.originalEvent.pageY") int pageY,
            @EventData("event.detail.originalEvent.altKey") boolean altKey,
            @EventData("event.detail.originalEvent.ctrlKey") boolean ctrlKey,
            @EventData("event.detail.originalEvent.metaKey") boolean metaKey,
            @EventData("event.detail.originalEvent.shiftKey") boolean shiftKey,
            @EventData("event.detail.originalEvent.button") int button) {
        super(source, fromClient);

        FeatureEventDetails featureEventDetails = MapEventUtil
                .getFeatureEventDetails(source.getRawConfiguration(), layerId,
                        featureId);
        this.layer = featureEventDetails.getLayer();
        this.vectorSource = featureEventDetails.getSource();
        this.feature = featureEventDetails.getFeature();

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
     * The feature that was clicked
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
     * Gets the click's mouse event details.
     *
     * @return mouse event details
     */
    public MouseEventDetails getMouseDetails() {
        return details;
    }
}
