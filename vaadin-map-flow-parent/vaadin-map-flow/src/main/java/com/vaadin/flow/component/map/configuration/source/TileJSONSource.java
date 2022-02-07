package com.vaadin.flow.component.map.configuration.source;

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * Source for loading tile data in TileJSON format
 */
public class TileJSONSource extends TileImageSource {
    private final boolean jsonp;

    public TileJSONSource(Options options) {
        super(options);
        this.jsonp = options.jsonp;
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_TILE_JSON;
    }

    /**
     * The URL to the TileJSON file
     * 
     * @return the current URL
     */
    @Override
    public String getUrl() {
        return super.getUrl();
    }

    /**
     * Sets the URL to the TileJSON file
     * 
     * @param url
     *            the new URL
     */
    @Override
    public void setUrl(String url) {
        super.setUrl(url);
    }

    /**
     * Whether to use JSONP to load the TileJSON file. Can be used if the server
     * does not support CORS. Default is {@code false}.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     * 
     * @return whether to use JSONP
     */
    public boolean isJsonp() {
        return jsonp;
    }

    public static class Options extends TileImageSource.BaseOptions<Options> {
        private boolean jsonp = false;

        /**
         * @see TileJSONSource#getUrl()
         */
        @Override
        public Options setUrl(String url) {
            return super.setUrl(url);
        }

        /**
         * @see TileJSONSource#isJsonp()
         */
        public Options setJsonp(boolean jsonp) {
            this.jsonp = jsonp;
            return getThis();
        }
    }
}
