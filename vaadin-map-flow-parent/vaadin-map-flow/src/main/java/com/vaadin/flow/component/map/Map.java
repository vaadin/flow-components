package com.vaadin.flow.component.map;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;

@Tag("vaadin-map")
// TODO: Enable once released
// @NpmPackage(value = "@vaadin/map", version = "23.0.0-alpha4")
// TODO: Include non-themed module `@vaadin/map/src/vaadin-map.js` when theme module is ready
@JsModule("@vaadin/map/vaadin-map.js")
public class Map extends MapBase {

    private Layer baseLayer;

    public Map() {
        super();
        OSMSource source = new OSMSource();
        TileLayer baseLayer = new TileLayer();
        baseLayer.setSource(source);
        setBaseLayer(baseLayer);
    }

    public Configuration getRawConfiguration() {
        return getConfiguration();
    }

    public Layer getBaseLayer() {
        return baseLayer;
    }

    public void setBaseLayer(Layer baseLayer) {
        if (this.baseLayer != null) {
            getConfiguration().removeLayer(this.baseLayer);
        }
        this.baseLayer = baseLayer;
        getConfiguration().prependLayer(baseLayer);
    }

    public void addLayer(Layer layer) {
        getConfiguration().addLayer(layer);
    }

    public void removeLayer(Layer layer) {
        getConfiguration().removeLayer(layer);
    }
}
