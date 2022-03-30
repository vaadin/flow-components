package com.vaadin.flow.component.map.configuration.source;

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

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * Source for loading tiled images from a map service using the
 * <a href="https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">Slippy
 * Map</a> tile numbering scheme, also known as XYZ format.
 * <p>
 * This is commonly used by OpenStreetMap, as well as other services who have
 * adopted the OSM tile numbering scheme.
 */
public class XYZSource extends TileImageSource {

    public XYZSource() {
        this(new Options());
    }

    public XYZSource(Options options) {
        super(options);
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_XYZ;
    }

    /**
     * The URL template in XYZ format (see also
     * <a href="https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">Slippy
     * map tilenames</a>) used to load individual image tiles. The URL must
     * include the {@code x}, {@code y} or {@code -y}, and {@code z}
     * placeholders, where {@code x} and {@code y} identify the tile in the tile
     * grid for the zoom level {@code z}.
     * <p>
     * Example: {@code https://a.tile.openstreetmap.org/{z}/{x}/{y}.png}
     *
     * @return the URL template
     */
    @Override
    public String getUrl() {
        return super.getUrl();
    }

    /**
     * Sets the URL template in XYZ format.
     *
     * @param url
     *            the new URL template
     */
    @Override
    public void setUrl(String url) {
        super.setUrl(url);
    }

    public static class Options extends TileImageSource.Options {
        /**
         * @see XYZSource#getUrl()
         */
        @Override
        public void setUrl(String url) {
            super.setUrl(url);
        }
    }
}
