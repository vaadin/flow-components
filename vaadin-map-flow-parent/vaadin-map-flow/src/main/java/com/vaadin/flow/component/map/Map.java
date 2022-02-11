package com.vaadin.flow.component.map;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;

import java.util.Objects;

@Tag("vaadin-map")
@NpmPackage(value = "@vaadin/map", version = "23.0.0-beta2")
// TODO: Include non-themed module `@vaadin/map/src/vaadin-map.js` when theme
// module is ready
@JsModule("@vaadin/map/vaadin-map.js")
@JsModule("./vaadin-map/mapConnector.js")
public class Map extends MapBase {

    private Layer backgroundLayer;
    private final FeatureLayer featureLayer;

    public Map() {
        super();
        // Setup default background layer
        OSMSource source = new OSMSource();
        TileLayer backgroundLayer = new TileLayer();
        backgroundLayer.setSource(source);
        setBackgroundLayer(backgroundLayer);
        // Setup default feature layer
        featureLayer = new FeatureLayer();
        addLayer(featureLayer);
        // Simple solution for rendering the feature layer on top of custom
        // layers by default. Developers can customize the z-index if they want
        // a different rendering order.
        featureLayer.setzIndex(100);
    }

    public Configuration getRawConfiguration() {
        return getConfiguration();
    }

    /**
     * Background layer of the map. Every new instance of a {@link Map} is
     * initialized with a background layer. By default, the background layer
     * will be a {@link TileLayer} using an {@link OSMSource}, which means it
     * will display tiled map data from the official OpenStreetMap server.
     *
     * @return the background layer of the map
     */
    public Layer getBackgroundLayer() {
        return backgroundLayer;
    }

    /**
     * Sets the background layer of the map. The layer will be prepended before
     * all other layers, which means it will be rendered in the background by
     * default. The background layer is not intended to be removed, and thus can
     * not be set to null. For use-cases where you want to use a dynamic set of
     * layers, consider setting the first layer as background layer, and then
     * adding the remaining layers using {@link #addLayer(Layer)}.
     *
     * @param backgroundLayer
     *            the new background layer, not null
     */
    public void setBackgroundLayer(Layer backgroundLayer) {
        Objects.requireNonNull(backgroundLayer);
        if (this.backgroundLayer != null) {
            getConfiguration().removeLayer(this.backgroundLayer);
        }
        this.backgroundLayer = backgroundLayer;
        getConfiguration().prependLayer(backgroundLayer);
    }

    public FeatureLayer getFeatureLayer() {
        return featureLayer;
    }

    public void addLayer(Layer layer) {
        getConfiguration().addLayer(layer);
    }

    public void removeLayer(Layer layer) {
        getConfiguration().removeLayer(layer);
    }

}
