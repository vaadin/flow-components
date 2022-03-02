package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/feature-layer")
public class FeatureLayerPage extends Div {
    public FeatureLayerPage() {
        Map map = new Map();

        map.getFeatureLayer().setId("feature-layer");

        NativeButton addFeature = new NativeButton("Add feature", e -> {
            MarkerFeature feature = new MarkerFeature();
            map.getFeatureLayer().addFeature(feature);
        });
        addFeature.setId("add-feature");

        NativeButton removeFirstFeature = new NativeButton(
                "Remove first feature", e -> {
                    if (map.getFeatureLayer().getFeatures().size() > 0) {
                        Feature feature = map.getFeatureLayer().getFeatures()
                                .get(0);
                        map.getFeatureLayer().removeFeature(feature);
                    }
                });
        removeFirstFeature.setId("remove-first-feature");

        add(map);
        add(new Div(addFeature, removeFirstFeature));
    }
}
