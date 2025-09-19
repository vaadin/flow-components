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
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.component.map.configuration.style.TextStyle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;

@Route("vaadin-map/cluster")
public class ClusterPage extends Div {
    public ClusterPage() {
        Map map = new Map();
        map.getView().setCenter(new Coordinate(0, 0));
        map.getView().setZoom(0);

        map.addLayer(createDefaultClusterLayer());
        map.addLayer(createCustomClusterLayer());

        add(map);
    }

    private ClusterLayer createDefaultClusterLayer() {
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

        return layer;
    }

    private ClusterLayer createCustomClusterLayer() {
        ClusterLayer layer = new ClusterLayer();
        layer.setId("custom-cluster-layer");

        DownloadHandler downloadHandler = DownloadHandler.forClassResource(
                getClass(), "/META-INF/resources/frontend/custom-cluster.png",
                "custom-cluster.png").inline();
        Icon.Options iconOptions = new Icon.Options();
        iconOptions.setImg(downloadHandler);
        iconOptions.setImgSize(new Icon.ImageSize(88, 88));
        iconOptions.setScale(0.5f);
        iconOptions.setAnchorOrigin(Icon.AnchorOrigin.TOP_LEFT);
        iconOptions.setAnchor(new Icon.Anchor(0.5f, 0.5f));
        Icon clusterIcon = new Icon(iconOptions);

        TextStyle textStyle = new TextStyle();
        textStyle.setFont("bold 14px sans-serif");
        textStyle.setOffset(0, 0);

        Style customStyle = new Style();
        customStyle.setImage(clusterIcon);
        customStyle.setTextStyle(textStyle);

        layer.setStyle(customStyle);

        MarkerFeature marker = new MarkerFeature(new Coordinate(-40, 0));
        layer.addFeature(marker);
        marker = new MarkerFeature(new Coordinate(-42, 2));
        layer.addFeature(marker);
        marker = new MarkerFeature(new Coordinate(-38, 2));
        layer.addFeature(marker);

        return layer;
    }
}
