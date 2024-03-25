/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

/**
 * Defines constants for OpenLayers types, which are uses by the client-side
 * synchronization mechanism to identify which OL class to construct and
 * synchronize into.
 */
public class Constants {
    // Layers
    public static final String OL_LAYER_IMAGE = "ol/layer/Image";
    public static final String OL_LAYER_TILE = "ol/layer/Tile";
    public static final String OL_LAYER_VECTOR = "ol/layer/Vector";
    // Sources
    public static final String OL_SOURCE_XYZ = "ol/source/XYZ";
    public static final String OL_SOURCE_OSM = "ol/source/OSM";
    public static final String OL_SOURCE_VECTOR = "ol/source/Vector";
    public static final String OL_SOURCE_TILE_WMS = "ol/source/TileWMS";
    public static final String OL_SOURCE_IMAGE_WMS = "ol/source/ImageWMS";
    // Geometry
    public static final String OL_GEOMETRY_POINT = "ol/geom/Point";
    // Style
    public static final String OL_STYLE_ICON = "ol/style/Icon";
    public static final String OL_STYLE_FILL = "ol/style/Fill";
    public static final String OL_STYLE_STROKE = "ol/style/Stroke";
    public static final String OL_STYLE_STYLE = "ol/style/Style";

    public static final String OL_MAP = "ol/Map";
    public static final String OL_VIEW = "ol/View";
    public static final String OL_FEATURE = "ol/Feature";
}
