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

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * Map source for loading tiled images from a map service using the XYZ URL
 * format
 */
public class XYZSource extends TileImageSource {

    public XYZSource() {
        this(new Options());
    }

    public XYZSource(BaseOptions<?> options) {
        super(options);
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_XYZ;
    }

    public static abstract class BaseOptions<T extends BaseOptions<T>>
            extends TileImageSource.BaseOptions<T> {
    }

    public static class Options extends BaseOptions<Options> {
    }
}
