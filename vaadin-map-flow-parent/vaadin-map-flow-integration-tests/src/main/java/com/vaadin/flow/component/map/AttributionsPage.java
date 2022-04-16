package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("vaadin-map/attributions")
public class AttributionsPage extends Div {

    List<String> testAttributions = List.of(
            "© <a href=\"https://map-service-1.com\">Map service 1</a>",
            "© <a href=\"https://map-service-2.com\">Map service 2</a>");

    public AttributionsPage() {
        Map map = new Map();

        NativeButton setupOSMSource = new NativeButton("Setup OSM source",
                e -> {
                    ((TileLayer) map.getBackgroundLayer())
                            .setSource(new OSMSource());
                });
        setupOSMSource.setId("setup-osm-source");

        NativeButton setupXYZSource = new NativeButton("Setup XYZ source",
                e -> {
                    ((TileLayer) map.getBackgroundLayer())
                            .setSource(new XYZSource());
                });
        setupXYZSource.setId("setup-xyz-source");

        NativeButton setCustomAttributions = new NativeButton(
                "Set custom attributions", e -> {
                    ((TileLayer) map.getBackgroundLayer()).getSource()
                            .setAttributions(testAttributions);
                });
        setCustomAttributions.setId("set-custom-attributions");

        NativeButton clearAttributions = new NativeButton("Clear attributions",
                e -> {
                    ((TileLayer) map.getBackgroundLayer()).getSource()
                            .setAttributions(null);
                });
        clearAttributions.setId("clear-attributions");

        NativeButton setupCollapsibleEnabled = new NativeButton(
                "Setup collapsible enabled", e -> {
                    // Default OSMSource does not allow changing collapsible
                    // settings, so use something else
                    XYZSource.Options options = new XYZSource.Options();
                    options.setAttributionsCollapsible(true);
                    XYZSource source = new XYZSource(options);
                    source.setAttributions(testAttributions);
                    ((TileLayer) map.getBackgroundLayer()).setSource(source);
                });
        setupCollapsibleEnabled.setId("setup-collapsible-enabled");

        NativeButton setupCollapsibleDisabled = new NativeButton(
                "Setup collapsible disabled", e -> {
                    // Default OSMSource does not allow changing collapsible
                    // settings, so use something else
                    XYZSource.Options options = new XYZSource.Options();
                    options.setAttributionsCollapsible(false);
                    XYZSource source = new XYZSource(options);
                    source.setAttributions(testAttributions);
                    ((TileLayer) map.getBackgroundLayer()).setSource(source);
                });
        setupCollapsibleDisabled.setId("setup-collapsible-disabled");

        add(map, new Div(setupOSMSource, setupXYZSource, setCustomAttributions,
                clearAttributions, setupCollapsibleEnabled,
                setupCollapsibleDisabled));
    }
}
