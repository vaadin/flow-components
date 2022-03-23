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

import java.util.Map;
import java.util.Objects;

/**
 * Source for loading tile data from WMS (Web Map Service) servers
 * <p>
 * See https://www.ogc.org/standards/wms
 */
public class TileWMSSource extends TileImageSource {

    private final int gutter;
    private final Map<String, Object> params;
    private final String serverType;

    public TileWMSSource(Options options) {
        super(options);
        Objects.requireNonNull(options.params,
                "WMS request parameters must not be null");
        Objects.requireNonNull(options.params.get("LAYERS"),
                "WMS request parameter LAYERS must not be null");
        this.gutter = options.gutter;
        this.params = options.params;
        this.serverType = options.serverType;
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_TILE_WMS;
    }

    /**
     * Size of the gutter around image tiles to ignore, in pixels. The default
     * value is {@code 0}, which means no gutter will be used. By setting this
     * to a non-zero value, the map will request images that are wider / taller
     * than the tile size by a value of {@code 2 x gutter}, but will ignore the
     * gutter when drawing a tile. Using a gutter allows ignoring artifacts at
     * the edges of tiles.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the current gutter value, in pixels
     */
    public int getGutter() {
        return gutter;
    }

    /**
     * The WMS request parameters for requesting images from the WMS server. At
     * least the {@code LAYERS} parameter is required. By default,
     * {@code VERSION} is {@code 1.3.0}, and {@code STYLES} is {@code ""}.
     * {@code WIDTH}, {@code HEIGHT}, {@code BBOX}, and {@code CRS} /
     * {@code SRS} will be set dynamically.
     * <p>
     * For individual parameters please refer to the documentation of the WMS
     * server as well as the <a href="https://www.ogc.org/standards/wms">WMS
     * specification</a>.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the WMS parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * The type of WMS server.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the type of WMS server
     */
    public String getServerType() {
        return serverType;
    }

    public static class Options extends TileImageSource.Options {
        private int gutter = 0;
        private Map<String, Object> params;
        private String serverType;

        /**
         * @see TileWMSSource#getGutter()
         */
        public void setGutter(int gutter) {
            this.gutter = gutter;
        }

        /**
         * @see TileWMSSource#getParams()
         */
        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        /**
         * @see TileWMSSource#getServerType()
         */
        public void setServerType(String serverType) {
            this.serverType = serverType;
        }
    }
}
