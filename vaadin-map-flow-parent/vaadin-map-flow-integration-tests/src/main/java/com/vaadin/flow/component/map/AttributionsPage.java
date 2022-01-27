package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("vaadin-map/attributions")
public class AttributionsPage extends Div {
    public AttributionsPage() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");

        NativeButton setupCustomAttributions = new NativeButton(
                "Setup custom attributions", e -> {
                    OSMSource source = new OSMSource();
                    List<String> attributions = List.of(
                            "© <a href=\"https://map-service-1.com\">Map service 1</a>",
                            "© <a href=\"https://map-service-2.com\">Map service 2</a>");
                    source.setAttributions(attributions);
                    ((TileLayer) map.getBackgroundLayer()).setSource(source);
                });
        setupCustomAttributions.setId("setup-custom-attributions");

        NativeButton changeAttributions = new NativeButton(
                "Change attributions", e -> {
                    List<String> attributions = List.of(
                            "© <a href=\"https://map-service-1.com\">Map service 1</a>",
                            "© <a href=\"https://map-service-2.com\">Map service 2</a>");
                    ((TileLayer) map.getBackgroundLayer()).getSource()
                            .setAttributions(attributions);
                });
        changeAttributions.setId("change-attributions");

        NativeButton clearAttributions = new NativeButton("Clear attributions",
                e -> {
                    List<String> attributions = new ArrayList<>();
                    ((TileLayer) map.getBackgroundLayer()).getSource()
                            .setAttributions(attributions);
                });
        clearAttributions.setId("clear-attributions");

        add(map, new Div(setupCustomAttributions, changeAttributions,
                clearAttributions));
    }
}
