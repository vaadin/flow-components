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

/**
 * Abstract base class for map sources providing tiled map data from a URL
 */
public abstract class UrlTileSource extends TileSource {

    private String url;

    protected UrlTileSource(Options options) {
        super(options);
        this.url = options.url;
    }

    /**
     * @return the URL to load tile data from
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL from which to load tile data.
     *
     * @param url
     *            the new URL
     */
    public void setUrl(String url) {
        this.url = url;
        markAsDirty();
    }

    protected static abstract class Options extends TileSource.Options {
        private String url;

        /**
         * @see UrlTileSource#setUrl(String)
         */
        public void setUrl(String url) {
            this.url = url;
        }
    }
}
