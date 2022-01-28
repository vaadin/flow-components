package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("vaadin-map/attributions")
public class AttributionsPage extends Div {

    List<String> testAttributions = List.of(
            "© <a href=\"https://map-service-1.com\">Map service 1</a>",
            "© <a href=\"https://map-service-2.com\">Map service 2</a>");

    public AttributionsPage() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");

        NativeButton setupCustomAttributions = new NativeButton(
                "Setup custom attributions", e -> {
                    OSMSource source = new OSMSource();
                    source.setAttributions(testAttributions);
                    ((TileLayer) map.getBackgroundLayer()).setSource(source);
                });
        setupCustomAttributions.setId("setup-custom-attributions");

        NativeButton changeAttributions = new NativeButton(
                "Change attributions", e -> {
                    ((TileLayer) map.getBackgroundLayer()).getSource()
                            .setAttributions(testAttributions);
                });
        changeAttributions.setId("change-attributions");

        NativeButton clearAttributions = new NativeButton("Clear attributions",
                e -> {
                    List<String> emptyAttributions = new ArrayList<>();
                    ((TileLayer) map.getBackgroundLayer()).getSource()
                            .setAttributions(emptyAttributions);
                });
        clearAttributions.setId("clear-attributions");

        NativeButton setupCollapsibleEnabled = new NativeButton(
                "Setup collapsible enabled", e -> {
                    // Default OSMSource does not allow changing collapsible
                    // settings, so use something else
                    XYZSource source = new XYZSource(new XYZSource.Options()
                            .setAttributionsCollapsible(true));
                    source.setAttributions(testAttributions);
                    ((TileLayer) map.getBackgroundLayer()).setSource(source);
                });
        setupCollapsibleEnabled.setId("setup-collapsible-enabled");

        NativeButton setupCollapsibleDisabled = new NativeButton(
                "Setup collapsible disabled", e -> {
                    // Default OSMSource does not allow changing collapsible
                    // settings, so use something else
                    XYZSource source = new XYZSource(new XYZSource.Options()
                            .setAttributionsCollapsible(false));
                    source.setAttributions(testAttributions);
                    ((TileLayer) map.getBackgroundLayer()).setSource(source);
                });
        setupCollapsibleDisabled.setId("setup-collapsible-disabled");

        add(map, new Div(setupCustomAttributions, changeAttributions,
                clearAttributions, setupCollapsibleEnabled,
                setupCollapsibleDisabled));
    }
}
