package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/feature-drag-drop")
public class FeatureDragDropPage extends Div {
    public FeatureDragDropPage() {
        Map map = new Map();

        MarkerFeature berlin = new MarkerFeature(
                new Coordinate(1491592.169957, 6893740.925498));
        berlin.setDraggable(true);
        MarkerFeature paris = new MarkerFeature(
                new Coordinate(261260.284278, 6250950.865879));

        map.getFeatureLayer().addFeature(berlin);
        map.getFeatureLayer().addFeature(paris);

        map.addFeatureDragDropListener(event -> {
            System.out.println("Moved feature: " + "featureId="
                    + event.getFeature().getId() + "; " + "coords="
                    + ((MarkerFeature) event.getFeature()).getCoordinates());
        });

        add(map);
    }
}
