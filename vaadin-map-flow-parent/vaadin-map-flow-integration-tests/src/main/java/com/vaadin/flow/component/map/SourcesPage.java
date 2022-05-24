package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.layer.ImageLayer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.ImageWMSSource;
import com.vaadin.flow.component.map.configuration.source.TileWMSSource;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

@Route("vaadin-map/sources")
public class SourcesPage extends Div {
    public SourcesPage() {
        Map map = new Map();
        add(map);

        NativeButton setupTileWMSSource = new NativeButton(
                "Setup TileWMS source", e -> {
                    String url = "https://example.com/wms";
                    String serverType = "geoserver";
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("LAYERS", "layer1");
                    params.put("TILED", true);
                    TileWMSSource.Options options = new TileWMSSource.Options();
                    options.setUrl(url);
                    options.setServerType(serverType);
                    options.setParams(params);
                    TileWMSSource source = new TileWMSSource(options);
                    TileLayer layer = new TileLayer();
                    layer.setId("background-layer");
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });
        setupTileWMSSource.setId("setup-tile-wms-source");

        NativeButton setupXYZSource = new NativeButton("Setup XYZ source",
                e -> {
                    String url = "https://example.com/wms";
                    XYZSource.Options options = new XYZSource.Options();
                    options.setUrl(url);
                    XYZSource source = new XYZSource(options);
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
                    ImageWMSSource.Options options = new ImageWMSSource.Options();
                    options.setUrl(url);
                    options.setServerType(serverType);
                    options.setParams(params);
                    options.setCrossOrigin("custom-cross-origin");
                    options.setRatio(2);
                    ImageWMSSource source = new ImageWMSSource(options);
                    ImageLayer layer = new ImageLayer();
                    layer.setId("background-layer");
                    layer.setSource(source);
                    map.setBackgroundLayer(layer);
                });
        setupImageWMSSource.setId("setup-image-wms-source");

        add(new Div(setupTileWMSSource, setupXYZSource, setupImageWMSSource));
    }
}
