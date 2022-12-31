package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Text;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/marker-feature")
public class MarkerFeaturePage extends Div {
    public MarkerFeaturePage() {
        Map map = new Map();
        map.getFeatureLayer().setId("feature-layer");

        NativeButton addDefaultMarkerFeature = new NativeButton(
                "Add default marker feature", e -> {
                    MarkerFeature feature = new MarkerFeature();
                    map.getFeatureLayer().addFeature(feature);
                });
        addDefaultMarkerFeature.setId("add-default-marker-feature");

        NativeButton addCustomMarkerFeature = new NativeButton(
                "Add custom marker feature", e -> {
                    Coordinate coordinate = new Coordinate(-73.96746522524636, 40.749310492492796); // United Nations
                    Icon icon = createCustomIcon();
                    MarkerFeature feature = new MarkerFeature(coordinate, icon);
                    map.getFeatureLayer().addFeature(feature);
                });
        addCustomMarkerFeature.setId("add-custom-marker-feature");

        NativeButton updateMarkerCoordinates = new NativeButton(
                "Update marker coordinates", e -> {
                    if (map.getFeatureLayer().getFeatures().size() > 0) {
                        MarkerFeature feature = (MarkerFeature) map
                                .getFeatureLayer().getFeatures().get(0);
                        Coordinate coordinate = new Coordinate(22.29985, 60.45234); // Vaadin HQ
                        feature.setCoordinates(coordinate);
                    }
                });
        updateMarkerCoordinates.setId("update-marker-coordinates");

        NativeButton updateMarkerIcon = new NativeButton("Update marker icon",
                e -> {
                    if (map.getFeatureLayer().getFeatures().size() > 0) {
                        MarkerFeature feature = (MarkerFeature) map
                                .getFeatureLayer().getFeatures().get(0);
                        Icon icon = createCustomIcon();
                        feature.setIcon(icon);
                    }
                });
        updateMarkerIcon.setId("update-marker-icon");

        NativeButton updateMarkerLabel = new NativeButton("Update marker label",
                e -> {
                    int number = 1;
                    for (Feature f : map.getFeatureLayer().getFeatures()) {
                        String label = String.format("Marker #%d", number++);
                        ((MarkerFeature) f).setLabel(label);

                        // same as the default values - just to show how to change the outline 
                        Text text = new Text();
                		text.setOffsetY(10);
                		text.setScale(1f);
                		text.setFill(new Fill("#000"));
                		text.setStroke(new Stroke("#fff", 1));
                		((MarkerFeature) f).getStyle().setText(text);
                    }
                });
        updateMarkerLabel.setId("update-marker-label");

        add(map);
        add(new Div(addDefaultMarkerFeature, addCustomMarkerFeature,
                updateMarkerCoordinates, updateMarkerIcon, updateMarkerLabel));
    }

    private Icon createCustomIcon() {

        Icon.Options options = new Icon.Options();
        options.setSrc("https://website.vaadin.com/hubfs/Images/favicon.ico");
        options.setColor("green");
        options.setOpacity(0.8f);
        return new Icon(options);
    }
}
