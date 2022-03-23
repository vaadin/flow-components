package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
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
