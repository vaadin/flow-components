package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

import java.util.Objects;
import java.util.Optional;

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

        Optional<VectorLayer> maybeLayer = source.getRawConfiguration()
                .getLayers().stream()
                .filter(layer -> layer instanceof VectorLayer
                        && Objects.equals(layer.getId(), layerId))
                .findFirst().map(layer -> (VectorLayer) layer);
        Optional<VectorSource> maybeVectorSource = maybeLayer
                .map(layer -> (VectorSource) layer.getSource());
        Optional<Feature> maybeFeature = maybeVectorSource.flatMap(
                vectorSource -> vectorSource.getFeatures().stream().filter(
                        feature -> Objects.equals(feature.getId(), featureId))
                        .findFirst());

        this.layer = maybeLayer.orElse(null);
        this.vectorSource = maybeVectorSource.orElse(null);
        this.feature = maybeFeature.orElse(null);

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
