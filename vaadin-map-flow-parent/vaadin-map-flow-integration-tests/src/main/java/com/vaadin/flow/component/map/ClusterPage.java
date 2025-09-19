/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.ClusterLayer;
import com.vaadin.flow.component.map.configuration.style.Circle;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.component.map.configuration.style.TextStyle;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/cluster")
public class ClusterPage extends Div {
    public ClusterPage() {
        Map map = new Map();
        map.getView().setCenter(new Coordinate(0, 0));
        map.getView().setZoom(0);

        ClusterLayer layer = new ClusterLayer();
        layer.setId("cluster-layer");
        layer.getSource().setDistance(50);
        layer.getSource().setMinDistance(50);

        // Cluster feature in some region
        MarkerFeature marker = new MarkerFeature(new Coordinate(0, 0));
        marker.setId("m1");
        layer.addFeature(marker);
        marker = new MarkerFeature(new Coordinate(2, 2));
        marker.setId("m2");
        layer.addFeature(marker);
        marker = new MarkerFeature(new Coordinate(-2, 2));
        marker.setId("m3");
        layer.addFeature(marker);

        // Add a separate non-clustered feature
        marker = new MarkerFeature(new Coordinate(40, 0));
        marker.setId("m4");
        marker.setText("Separate");
        layer.addFeature(marker);

        map.addLayer(layer);

        // Create cluster layer with custom styles
        ClusterLayer customLayer = new ClusterLayer();
        customLayer.setId("custom-cluster-layer");
        Style customStyle = new Style();
        Circle.Options circleOptions = new Circle.Options();
        circleOptions.setRadius(20d);
        circleOptions.setFill(new Fill("red"));
        circleOptions.setStroke(new Stroke("black", 2));
        Circle circle = new Circle(circleOptions);
        customStyle.setImage(circle);
        TextStyle textStyle = new TextStyle();
        textStyle.setFont("bold 14px sans-serif");
        textStyle.setFill("black");
        textStyle.setStroke(null);
        textStyle.setOffset(0, 0);
        customStyle.setTextStyle(textStyle);
        customLayer.setStyle(customStyle);

        marker = new MarkerFeature(new Coordinate(-40, 0));
        customLayer.addFeature(marker);
        marker = new MarkerFeature(new Coordinate(-42, 2));
        customLayer.addFeature(marker);
        marker = new MarkerFeature(new Coordinate(-38, 2));
        customLayer.addFeature(marker);

        map.addLayer(customLayer);

        add(map);
    }
}
