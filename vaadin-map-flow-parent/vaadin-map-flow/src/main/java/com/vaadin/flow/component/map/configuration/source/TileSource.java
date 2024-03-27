/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

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
