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
 * Abstract base class for map sources providing tiled map data
 */
public abstract class TileSource extends Source {

    private final boolean opaque;

    protected TileSource(Options options) {
        super(options);
        this.opaque = options.opaque;
    }

    /**
     * Whether the source has an opaque background or not. A non-opaque source
     * has a transparent background, which is useful for overlay layers. Default
     * value is {@code false}.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return whether the source has an opaque background
     */
    public boolean isOpaque() {
        return opaque;
    }

    protected static abstract class Options extends Source.Options {
        private boolean opaque = false;

        /**
         * @see TileSource#isOpaque()
         */
        public void setOpaque(boolean opaque) {
            this.opaque = opaque;
        }
    }
}
