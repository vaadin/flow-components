/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.events;

import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import elemental.json.JsonArray;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

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

    static Coordinate getCoordinate(JsonArray jsonCoordinates) {
        JsonValue xValue = jsonCoordinates.get(0);
        JsonValue yValue = jsonCoordinates.get(1);

        boolean hasNullValue = xValue.getType() == JsonType.NULL
                || yValue.getType() == JsonType.NULL;

        if (hasNullValue) {
            logger.error("Received invalid map coordinates from client. "
                    + "This can happen when using Coordinate.fromLonLat together with the default coordinate system. "
                    + "If you have upgraded to Vaadin 23.2, please replace usages of Coordinate.fromLonLat.");
        }

        double x = xValue.getType() == JsonType.NULL ? 0 : xValue.asNumber();
        double y = yValue.getType() == JsonType.NULL ? 0 : yValue.asNumber();

        return new Coordinate(x, y);
    }

    static Extent getExtent(JsonArray jsonExtend) {
        JsonValue minXValue = jsonExtend.get(0);
        JsonValue minYValue = jsonExtend.get(1);
        JsonValue maxXValue = jsonExtend.get(2);
        JsonValue maxYValue = jsonExtend.get(3);

        double minX = minXValue.getType() == JsonType.NULL ? 0
                : minXValue.asNumber();
        double minY = minYValue.getType() == JsonType.NULL ? 0
                : minYValue.asNumber();
        double maxX = maxXValue.getType() == JsonType.NULL ? 0
                : maxXValue.asNumber();
        double maxY = maxYValue.getType() == JsonType.NULL ? 0
                : maxYValue.asNumber();

        return new Extent(minX, minY, maxX, maxY);
    }
}
