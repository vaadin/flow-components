package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/layers")
public class LayersPage extends Div {
    public LayersPage() {
        Map map = new Map();
        add(map);

        NativeButton setCustomSource = new NativeButton("Set custom source",
                e -> {
                    TileLayer backgroundLayer = (TileLayer) map
                            .getBackgroundLayer();
                    XYZSource.Options options = new XYZSource.Options();
                    options.setUrl("https://example.com");
                    backgroundLayer.setSource(new XYZSource(options));
                });
        setCustomSource.setId("set-custom-source");

        NativeButton replaceBackgroundLayer = new NativeButton(
                "Replace background layer", e -> {
                    VectorSource vectorSource = new VectorSource();
                    VectorLayer vectorLayer = new VectorLayer();
                    vectorLayer.setSource(vectorSource);
                    vectorLayer.setId("new-background-layer");
                    map.setBackgroundLayer(vectorLayer);
                });
        replaceBackgroundLayer.setId("replace-background-layer");

        FeatureLayer customLayer = new FeatureLayer();
        customLayer.setId("custom-layer");

        NativeButton addCustomLayer = new NativeButton("Add custom layer",
                e -> map.addLayer(customLayer));
        addCustomLayer.setId("add-custom-layer");
        NativeButton removeCustomLayer = new NativeButton("Remove custom layer",
                e -> map.removeLayer(customLayer));
        removeCustomLayer.setId("remove-custom-layer");

        NativeButton customizeLayerProperties = new NativeButton(
                "Customize layer properties", e -> {
                    // Test sync of some common layer props, combination does
                    // not need to make sense
                    customLayer.setVisible(false);
                    customLayer.setOpacity(0.7f);
                    customLayer.setzIndex(42);
                });
        customizeLayerProperties.setId("customize-layer-properties");

        add(new Div(setCustomSource, replaceBackgroundLayer, addCustomLayer,
                removeCustomLayer, customizeLayerProperties));
    }
}
