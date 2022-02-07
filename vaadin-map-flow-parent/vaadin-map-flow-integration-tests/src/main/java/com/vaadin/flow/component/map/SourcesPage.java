package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.ImageLayer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.ImageWMSSource;
import com.vaadin.flow.component.map.configuration.source.TileJSONSource;
import com.vaadin.flow.component.map.configuration.source.TileWMSSource;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

@Route("vaadin-map/sources")
public class SourcesPage extends Div {
    public SourcesPage() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");
        add(map);

        NativeButton setupTileJSONSource = new NativeButton(
                "Setup TileJSON source", e -> {
                    String url = "https://example.com/tilejson";
                    TileJSONSource source = new TileJSONSource(
                            new TileJSONSource.Options().setUrl(url));
                    TileLayer layer = new TileLayer();
                    layer.setId("background-layer");
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });
        setupTileJSONSource.setId("setup-tile-json-source");

        NativeButton setupTileWMSSource = new NativeButton(
                "Setup TileWMS source", e -> {
                    String url = "https://example.com/wms";
                    String serverType = "geoserver";
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("LAYERS", "layer1");
                    params.put("TILED", true);
                    TileWMSSource source = new TileWMSSource(
                            new TileWMSSource.Options().setUrl(url)
                                    .setServerType(serverType)
                                    .setParams(params));
                    TileLayer layer = new TileLayer();
                    layer.setId("background-layer");
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });
        setupTileWMSSource.setId("setup-tile-wms-source");

        NativeButton setupXYZSource = new NativeButton("Setup XYZ source",
                e -> {
                    String url = "https://example.com/wms";
                    XYZSource source = new XYZSource(
                            new XYZSource.Options().setUrl(url));
                    TileLayer layer = new TileLayer();
                    layer.setId("background-layer");
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });
        setupXYZSource.setId("setup-xyz-source");

        NativeButton setupImageWMSSource = new NativeButton(
                "Setup ImageWMS source", e -> {
                    String url = "https://example.com/wms";
                    String serverType = "geoserver";
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("LAYERS", "layer1");
                    ImageWMSSource source = new ImageWMSSource(
                            new ImageWMSSource.Options().setUrl(url)
                                    .setServerType(serverType).setParams(params)
                                    .setCrossOrigin("custom-cross-origin")
                                    .setRatio(2));
                    ImageLayer layer = new ImageLayer();
                    layer.setId("background-layer");
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });
        setupImageWMSSource.setId("setup-image-wms-source");

        add(new Div(setupTileJSONSource, setupTileWMSSource, setupXYZSource,
                setupImageWMSSource));
    }
}
