/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.util.Objects;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.FeatureLayer;
import com.vaadin.flow.component.map.configuration.layer.ImageLayer;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.source.Source;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.component.map.configuration.source.XYZSource;

/**
 * Map is a component for displaying geographic maps from various sources. It
 * supports multiple layers, tiled and full image sources, adding markers, and
 * interaction through events.
 * <p>
 * Each map consists of one or more {@link Layer}s that display geographical
 * data. Each layer has a {@link Source} that provides that data. The Map
 * component provides several types of layers (for example {@link TileLayer},
 * {@link VectorLayer}, {@link ImageLayer}), as well as several types of sources
 * that can be used with each type of layer (for example {@link OSMSource},
 * {@link XYZSource}, {@link VectorSource}).
 * <p>
 * The map component comes pre-configured with a background layer, which by
 * default is a {@link TileLayer} using an {@link OSMSource}, which means that
 * it displays tiled image data from the OpenStreeMap service. The background
 * layer of the map can be replaced using {@link #setBackgroundLayer(Layer)}.
 * The component is also pre-configured with a {@link FeatureLayer}, accessible
 * with {@link #getFeatureLayer()}, that allows to quickly display geographical
 * features, such as markers (see {@link MarkerFeature}), on top of a map.
 * Custom layers can be added or removed using {@link #addLayer(Layer)} and
 * {@link #removeLayer(Layer)}.
 * <p>
 * The viewport of the map is controlled through a {@link View}, which allows
 * setting the center, zoom level and rotation. The map's view can be accessed
 * through {@link Map#getView()}.
 * <p>
 * The default projection, or coordinate system, for all coordinates passed to,
 * or returned from the public API is {@code EPSG:4326}, also referred to as GPS
 * coordinates. This is called the user projection. Internally the component
 * converts all coordinates into the projection that is used by the map's
 * {@link View}, which is referred to as the view projection. The user
 * projection can be changed using {@link #setUserProjection(String)}. Out of
 * the box, the map component has support for the {@code EPSG:4326} and
 * {@code EPSG:3857} projections. Custom coordinate projections can be defined
 * using {@link #defineProjection(String, String)}.
 */
@Tag("vaadin-map")
@NpmPackage(value = "@vaadin/map", version = "24.8.0-alpha18")
@NpmPackage(value = "proj4", version = "2.15.0")
@JsModule("@vaadin/map/src/vaadin-map.js")
@JsModule("./vaadin-map/mapConnector.js")
public class Map extends MapBase {

    private Layer backgroundLayer;
    private final FeatureLayer featureLayer;

    /**
     * Sets the projection (or coordinate system) to use for all coordinates.
     * That means that all coordinates passed to, or returned from the public
     * API, must be in this projection. Internally the coordinates will be
     * converted into the projection that is used by the map's {@link View}.
     * <p>
     * By default, the user projection is set to {@code EPSG:4326}, also known
     * as latitude / longitude, or GPS coordinates.
     * <p>
     * This setting affects all maps in the current {@link UI}, currently it is
     * not possible to configure this per map instance. This method may only be
     * invoked inside of UI threads, and will throw otherwise. This setting
     * being scoped to the current UI means that it will stay active when
     * navigating between pages using the Vaadin router, but not when doing a
     * "hard" location change, or when reloading the page. As such it is
     * recommended to apply this setting on every page that displays maps. Note
     * that when using the preserve on refresh feature, a view's constructor is
     * not called. In that case this setting can be applied in an attach
     * listener.
     * <p>
     * This method should be called before creating any maps. Changing this
     * setting does not affect existing maps, specifically the component does
     * not convert coordinates configured in an existing map into the new
     * projection. Instead, existing maps should be recreated after changing
     * this setting.
     *
     * @param projection
     *            the user projection to use for all public facing API
     */
    public static void setUserProjection(String projection) {
        UI ui = UI.getCurrent();
        if (ui == null || ui.getPage() == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        UI.getCurrent().getPage().executeJs(
                "window.Vaadin.Flow.mapConnector.setUserProjection($0)",
                projection);
    }

    /**
     * Defines a custom coordinate projection that can then be used as user
     * projection or view projection. Defining a projection requires a name,
     * which is then used to reference it when setting a user or view
     * projection, as well as a projection definition in the Well Known Text
     * (WKS) format. A handy resource for looking up WKS definitions is
     * <a href="https://epsg.io/">epsg.io</a>, which allows to search for
     * projections, get coordinates from a map, as well as transform coordinates
     * between projections.
     * <p>
     * This definition is valid for the lifetime of the current {@link UI}. This
     * method may only be invoked inside of UI threads, and will throw
     * otherwise. This definition being scoped to the current UI means that it
     * will stay active when navigating between pages using the Vaadin router,
     * but not when doing a "hard" location change, or when reloading the page.
     * As such it is recommended to apply this definition on every page that
     * displays maps. Note that when using the preserve on refresh feature, a
     * view's constructor is not called. In that case this definition can be
     * applied in an attach listener.
     * <p>
     * This method should be called before creating any maps that want to make
     * use of this projection, and before setting it as a custom user
     * projection.
     *
     * @see #setUserProjection(String)
     * @see View
     * @param projectionName
     *            the name of the projection that can be referenced when setting
     *            a user or view projection
     * @param wksDefinition
     *            the Well Known Text (WKS) definition of the projection
     */
    public static void defineProjection(String projectionName,
            String wksDefinition) {
        UI ui = UI.getCurrent();
        if (ui == null || ui.getPage() == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        UI.getCurrent().getPage().executeJs(
                "window.Vaadin.Flow.mapConnector.defineProjection($0, $1)",
                projectionName, wksDefinition);
    }

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

    /**
     * The feature layer of the map. Every new instance of a {@link Map} has a
     * pre-configured {@link FeatureLayer} for convenience, to allow quickly
     * adding geographical features without requiring to set up a layer. Note
     * that it is possible to add additional feature layers with
     * {@link #addLayer(Layer)} if splitting up features into different layers
     * is beneficial for a use-case.
     *
     * @return the feature layer of the map
     */
    public FeatureLayer getFeatureLayer() {
        return featureLayer;
    }

    /**
     * Adds a layer to the map. The layer will be appended to the list of
     * layers, meaning that it will be rendered last / on top of previously
     * added layers by default. For more fine-grained control of the layer
     * rendering order, use {@link Layer#setzIndex(Integer)}.
     *
     * @param layer
     *            the layer to be added
     */
    public void addLayer(Layer layer) {
        getConfiguration().addLayer(layer);
    }

    /**
     * Remove a layer from the map
     *
     * @param layer
     *            the layer to be removed
     */
    public void removeLayer(Layer layer) {
        getConfiguration().removeLayer(layer);
    }

    /**
     * Gets center coordinates of the map's viewport
     * <p>
     * This is a convenience method that delegates to the map's internal
     * {@link View}. See {@link #getView()} for accessing other properties of
     * the view.
     *
     * @return current center of the viewport
     */
    public Coordinate getCenter() {
        return getView().getCenter();
    }

    /**
     * Sets the center of the map's viewport. Coordinates must be specified in
     * the map's user projection, which by default is {@code EPSG:4326}, also
     * referred to as GPS coordinates. If the user projection has been changed
     * using {@link Map#setUserProjection(String)}, then coordinates must be
     * specified in that projection instead.
     * <p>
     * This is a convenience method that delegates to the map's internal
     * {@link View}. See {@link #getView()} for accessing other properties of
     * the view.
     * <p>
     * Note that the user projection is a different concept than the view
     * projection set in the map's {@link View}. The view projection affects how
     * map data is interpreted and rendered, while the user projection defines
     * the coordinate system that all coordinates passed to, or returned from
     * the public API must be in.
     *
     * @param center
     *            new center of the viewport
     */
    public void setCenter(Coordinate center) {
        getView().setCenter(center);
    }

    /**
     * Gets zoom level of the map's viewport, defaults to {@code 0}
     * <p>
     * This is a convenience method that delegates to the map's internal
     * {@link View}. See {@link #getView()} for accessing other properties of
     * the view.
     *
     * @return current zoom level
     */
    public double getZoom() {
        return getView().getZoom();
    }

    /**
     * Sets the zoom level of the map's viewport. The zoom level is a decimal
     * value that starts at {@code 0} as the most zoomed-out level, and then
     * continually increases to zoom further in. By default, the maximum zoom
     * level is currently restricted to {@code 28}. In practical terms, the
     * level of detail of the map data that a map service provides determines
     * how useful higher zoom levels are.
     * <p>
     * This is a convenience method that delegates to the map's internal
     * {@link View}. See {@link #getView()} for accessing other properties of
     * the view.
     *
     * @param zoom
     *            new zoom level
     */
    public void setZoom(double zoom) {
        getView().setZoom(zoom);
    }
}
