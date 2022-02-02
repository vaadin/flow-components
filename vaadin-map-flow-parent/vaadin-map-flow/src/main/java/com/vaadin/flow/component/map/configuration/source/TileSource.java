package com.vaadin.flow.component.map.configuration.source;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
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

import com.vaadin.flow.component.map.configuration.tilegrid.TileGrid;

/**
 * Abstract base class for map sources providing tiled map data
 */
public abstract class TileSource extends Source {

    private final boolean opaque;

    private final TileGrid tileGrid;

    protected TileSource(BaseOptions<?> options) {
        super(options);
        this.opaque = options.opaque;
        this.tileGrid = options.tileGrid;
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

    public TileGrid getTileGrid() {
        return tileGrid;
    }

    protected static class BaseOptions<T extends BaseOptions<T>>
            extends Source.BaseOptions<T> {
        private boolean opaque = false;
        private TileGrid tileGrid;

        /**
         * @see TileSource#isOpaque()
         */
        public T setOpaque(boolean opaque) {
            this.opaque = opaque;
            return getThis();
        }

        public T setTileGrid(TileGrid tileGrid) {
            this.tileGrid = tileGrid;
            return getThis();
        }
    }
}
