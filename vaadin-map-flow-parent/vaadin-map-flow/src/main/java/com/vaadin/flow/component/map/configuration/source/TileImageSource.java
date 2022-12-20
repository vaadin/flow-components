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
 * Abstract base class for map sources providing tiled images from a URL
 */
public abstract class TileImageSource extends UrlTileSource {

    private final String crossOrigin;

    protected TileImageSource(Options options) {
        super(options);
        this.crossOrigin = options.crossOrigin;
    }

    /**
     * The {@code crossOrigin} attribute for loaded images.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the crossOrigin attribute used for loaded images
     */
    public String getCrossOrigin() {
        return crossOrigin;
    }

    protected static abstract class Options extends UrlTileSource.Options {
        private String crossOrigin;

        /**
         * @see TileImageSource#getCrossOrigin()
         */
        public void setCrossOrigin(String crossOrigin) {
            this.crossOrigin = crossOrigin;
        }
    }
}
