package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

import java.util.Objects;
import java.util.Optional;

class MapEventUtil {
    static FeatureEventDetails getFeatureEventDetails(
            Configuration configuration, String layerId, String featureId) {
        Optional<VectorLayer> maybeLayer = configuration.getLayers().stream()
                .filter(layer -> layer instanceof VectorLayer
                        && Objects.equals(layer.getId(), layerId))
                .findFirst().map(layer -> (VectorLayer) layer);
        Optional<VectorSource> maybeSource = maybeLayer
                .map(layer -> (VectorSource) layer.getSource());
        Optional<Feature> maybeFeature = maybeSource.flatMap(
                vectorSource -> vectorSource.getFeatures().stream().filter(
                        feature -> Objects.equals(feature.getId(), featureId))
                        .findFirst());

        return new FeatureEventDetails(maybeFeature.orElse(null),
                maybeSource.orElse(null), maybeLayer.orElse(null));
    }

    static FeatureEventDetails getFeatureEventDetails(
            Configuration configuration, String featureId) {
        for (Layer layer : configuration.getLayers()) {
            if (layer instanceof FeatureLayer) {
                FeatureLayer fl = (FeatureLayer) layer;
                Optional<Feature> feature = fl.getFeatures().stream()
                        .filter(f -> f.getId().equals(featureId)).findFirst();
                if (feature.isPresent()) {
                    return new FeatureEventDetails(feature.get(),
                            fl.getSource(), fl);
                }
            }
        }
        return null;
    }
}
