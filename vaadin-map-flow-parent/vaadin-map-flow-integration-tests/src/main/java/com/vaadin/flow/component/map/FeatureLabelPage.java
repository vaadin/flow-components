package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/feature-label")
public class FeatureLabelPage extends Div {
    public FeatureLabelPage() {
        Map map = new Map();
        map.getFeatureLayer().setId("feature-layer");

        MarkerFeature marker = new MarkerFeature();
        marker.setLabel("Marker label");
        map.getFeatureLayer().addFeature(marker);

        NativeButton updateLabelText = new NativeButton("Update label text",
                e -> {
                    marker.setLabel("Updated label");
                });
        updateLabelText.setId("update-label-text");

        NativeButton removeLabelText = new NativeButton("Remove label text",
                e -> {
                    marker.setLabel(null);
                });
        removeLabelText.setId("remove-label-text");

        add(map);
        add(new Div(updateLabelText, removeLabelText));
    }
}
