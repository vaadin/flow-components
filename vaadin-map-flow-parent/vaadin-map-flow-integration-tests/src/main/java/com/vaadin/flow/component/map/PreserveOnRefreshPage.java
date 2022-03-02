package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/preserve-on-refresh")
@PreserveOnRefresh
public class PreserveOnRefreshPage extends Div {
    public PreserveOnRefreshPage() {
        Map map = new Map();
        add(map);

        // Action for changing the configuration, so we can test that the
        // changes are preserved after refresh
        NativeButton customizeMap = new NativeButton("Customize Map", e -> {
            // Add custom feature layer (makes 3 layers in total)
            map.addLayer(new FeatureLayer());
            // Modify viewport
            map.getView().setCenter(
                    new Coordinate(2482424.644689998, 8500614.173537256));
            map.getView().setZoom(14);
        });
        customizeMap.setId("customize-map");

        add(new Div(customizeMap));
    }
}
