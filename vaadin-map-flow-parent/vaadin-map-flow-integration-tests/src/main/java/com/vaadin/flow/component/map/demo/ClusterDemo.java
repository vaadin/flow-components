/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.demo;

import java.util.concurrent.ThreadLocalRandom;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.ClusterLayer;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/demo/cluster")
public class ClusterDemo extends Div {
    public ClusterDemo() {
        Map map = new Map();
        map.getView().setCenter(new Coordinate(0, 0));
        map.getView().setZoom(0);

        ClusterLayer clusterLayer = new ClusterLayer();
        clusterLayer.getSource().setDistance(50);
        clusterLayer.getSource().setMinDistance(50);

        final int numMarkers = 500;
        for (int i = 0; i < numMarkers; i++) {
            double lon = ThreadLocalRandom.current().nextDouble(-60, 60);
            double lat = ThreadLocalRandom.current().nextDouble(-45, 45);
            MarkerFeature marker = new MarkerFeature(new Coordinate(lon, lat));
            clusterLayer.addFeature(marker);
        }

        map.addLayer(clusterLayer);

        add(map);
    }
}
