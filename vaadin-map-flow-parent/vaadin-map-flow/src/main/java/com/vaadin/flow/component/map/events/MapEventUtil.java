/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

class MapEventUtil {

    private static final Logger logger = LoggerFactory
            .getLogger(MapEventUtil.class);

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

    static Coordinate getCoordinate(ArrayNode jsonCoordinates) {
        JsonNode xValue = jsonCoordinates.get(0);
        JsonNode yValue = jsonCoordinates.get(1);

        double x = xValue.isNull() ? 0 : xValue.asDouble();
        double y = yValue.isNull() ? 0 : yValue.asDouble();

        return new Coordinate(x, y);
    }

    static Extent getExtent(ArrayNode jsonExtend) {
        JsonNode minXValue = jsonExtend.get(0);
        JsonNode minYValue = jsonExtend.get(1);
        JsonNode maxXValue = jsonExtend.get(2);
        JsonNode maxYValue = jsonExtend.get(3);

        double minX = minXValue.isNull() ? 0 : minXValue.asDouble();
        double minY = minYValue.isNull() ? 0 : minYValue.asDouble();
        double maxX = maxXValue.isNull() ? 0 : maxXValue.asDouble();
        double maxY = maxYValue.isNull() ? 0 : maxYValue.asDouble();

        return new Extent(minX, minY, maxX, maxY);
    }
}
