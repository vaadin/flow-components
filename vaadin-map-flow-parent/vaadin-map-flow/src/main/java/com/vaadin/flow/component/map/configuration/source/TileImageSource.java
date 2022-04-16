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
