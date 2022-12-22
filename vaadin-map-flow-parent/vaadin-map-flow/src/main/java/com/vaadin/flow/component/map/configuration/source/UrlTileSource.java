/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

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
